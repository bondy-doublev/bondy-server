package org.example.interactionservice.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.entity.Share;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.repository.ShareRepository;
import org.example.interactionservice.service.interfaces.IShareService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShareService implements IShareService {
  PostRepository postRepo;
  ShareRepository shareRepo;
  EntityManager entityManager;

  @Override
  @Transactional
  public Share createShare(Long userId, Long postId) {
    if (!postRepo.existsById(postId))
      throw new AppException(ErrorCode.BAD_REQUEST, "This post not exist");

    Post post = entityManager.getReference(Post.class, postId);

    Share newShare = Share.builder()
      .userId(userId)
      .post(post)
      .build();

    postRepo.updateShareCount(postId, 1);
    return shareRepo.save(newShare);
  }

  @Override
  @Transactional
  public void deleteShare(Long userId, Long shareId) {
    AppException ex = new AppException(ErrorCode.BAD_REQUEST, "Delete share post fail");

    Share share = shareRepo.findById(shareId)
      .orElseThrow(() -> ex);

    if (!share.getUserId().equals(userId))
      throw ex;

    if (share.getPost() != null)
      postRepo.updateShareCount(share.getPost().getId(), -1);

    shareRepo.delete(share);
  }
}
