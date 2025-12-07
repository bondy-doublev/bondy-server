package org.example.interactionservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.AuthClient;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.dto.response.UserBasicResponse;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IShareService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShareService implements IShareService {

  PostRepository postRepo;
  AuthClient authClient;

  @Override
  @Transactional
  public PostResponse createShare(Long userId, Long originalPostId, String message, Boolean isPublic) {
    // 1. Lấy bài gốc
    Post original = postRepo.findById(originalPostId)
      .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "This post not exist"));

    if (original.getSharedFrom() != null) {
      throw new AppException(
        ErrorCode.BAD_REQUEST,
        "Cannot share a shared post"
      );
    }
    
    // 2. Tạo bài share mới
    Post sharePost = Post.builder()
      .userId(userId)
      .contentText(message)
      .mediaCount(0)
      .visibility(isPublic != null ? isPublic : Boolean.TRUE)
      .reactionCount(0L)
      .commentCount(0L)
      .shareCount(0L)
      .sharedFrom(original)
      .build();

    // tăng share_count bài gốc (nếu bạn đang dùng cột này)
    postRepo.updateShareCount(original.getId(), 1);

    sharePost = postRepo.save(sharePost);

    // 3. Build PostResponse cho bài gốc
    UserBasicResponse originalOwner = authClient.getBasicProfile(original.getUserId());
    // nếu muốn lấy taggedUsers thật, bạn có thể inject MentionRepository/TagService,
    // ở đây tạm set rỗng cho gọn:
    List<UserBasicResponse> originalTaggedUsers = List.of();

    PostResponse originalPostResponse = original.toPostResponse(
      originalOwner,
      originalTaggedUsers,
      false,   // reacted (cho current user) – share API thường chưa cần
      null     // original của original -> null, tránh đệ quy
    );

    // 4. Build PostResponse cho bài share (sharePost)
    UserBasicResponse shareOwner = authClient.getBasicProfile(userId);
    List<UserBasicResponse> shareTaggedUsers = List.of(); // chưa hỗ trợ tag trong share

    PostResponse sharePostResponse = sharePost.toPostResponse(
      shareOwner,
      shareTaggedUsers,
      false,              // reacted cho current user
      originalPostResponse // sharedFrom
    );

    return sharePostResponse;
  }

  @Override
  @Transactional
  public void deleteShare(Long userId, Long sharePostId) {
    AppException ex = new AppException(ErrorCode.BAD_REQUEST, "Delete share post fail");

    Post sharePost = postRepo.findById(sharePostId)
      .orElseThrow(() -> ex);

    // chỉ cho phép xoá bài share của chính mình
    if (!sharePost.getUserId().equals(userId)) {
      throw ex;
    }

    // nếu có bài gốc thì giảm share_count
    Post original = sharePost.getSharedFrom();
    if (original != null) {
      postRepo.updateShareCount(original.getId(), -1);
    }

    postRepo.delete(sharePost);
  }
}
