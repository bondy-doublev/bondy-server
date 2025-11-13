package org.example.interactionservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.UploadClient;
import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.request.UpdatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.entity.MediaAttachment;
import org.example.interactionservice.entity.Mention;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.enums.MediaType;
import org.example.interactionservice.property.PropsConfig;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.repository.ShareRepository;
import org.example.interactionservice.service.interfaces.IPostService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {
  PropsConfig props;
  PostRepository postRepo;
  ShareRepository shareRepo;

  UploadClient uploadClient;

  @Override
  public PostResponse createPost(Long ownerId, CreatePostRequest request) {
    if ((request.getContent() == null || request.getContent().isBlank())
      && CollectionUtils.isEmpty(request.getMediaFiles())) {
      throw new AppException(ErrorCode.BAD_REQUEST, "Post must contain text or media");
    }

    List<MultipartFile> mediaFiles = request.getMediaFiles();

    if (!CollectionUtils.isEmpty(mediaFiles)) {
      if (mediaFiles.size() > props.getPost().getMediaLimit()) {
        throw new AppException(ErrorCode.BAD_REQUEST,
          "You can upload at most %d media files".formatted(props.getPost().getMediaLimit()));
      }

      long videoCount = mediaFiles.stream().filter(this::isVideoFile).count();
      if (videoCount > props.getPost().getVideoLimit()) {
        throw new AppException(ErrorCode.BAD_REQUEST,
          "A post cannot contain more than %d video".formatted(props.getPost().getVideoLimit()));
      }
    }

    // 3. Build Post
    Post newPost = Post.builder()
      .userId(ownerId)
      .visibility(request.getIsPublic())
      .contentText(request.getContent())
      .build();

    // 4. Upload media
    if (!CollectionUtils.isEmpty(mediaFiles)) {
      List<String> mediaUrls = uploadClient.uploadLocalMultiple(mediaFiles);

      Set<MediaAttachment> mediaAttachments = mediaUrls.stream()
        .map(url -> MediaAttachment.builder()
          .post(newPost)
          .type(detectMediaType(url).name())
          .url(url)
          .build())
        .collect(Collectors.toSet());

      newPost.setMediaAttachments(mediaAttachments);
      newPost.setMediaCount(mediaAttachments.size());
    }

    if (request.getTagUserIds() != null && !request.getTagUserIds().isEmpty()) {
      List<Mention> tags = new ArrayList<>();

      for (Long id : request.getTagUserIds()) {
        if (id.equals(ownerId))
          throw new AppException(ErrorCode.BAD_REQUEST, "Can not mention yourself");

        Mention tag = Mention.builder()
          .post(newPost)
          .userId(id)
          .build();

        tags.add(tag);
      }

      newPost.setTags(new HashSet<>(tags));
    }

    return postRepo.save(newPost).toPostResponse(null, null, false);
  }

  @Override
  @Transactional
  public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
    Post post = postRepo.findById(postId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Post with id " + postId + " not found"));

    if (!post.getUserId().equals(userId)) {
      throw new AppException(ErrorCode.BAD_REQUEST, "This post not belong to user");
    }

    // 1) Update content + visibility
    if (request.getContent() != null) post.setContentText(request.getContent());
    if (request.getIsPublic() != null) post.setVisibility(request.getIsPublic());

    // 2) Xoá media cũ
    int existingCount = post.getMediaAttachments() == null ? 0 : post.getMediaAttachments().size();
    int removedCount = 0;
    if (request.getRemoveAttachmentIds() != null && !request.getRemoveAttachmentIds().isEmpty()) {
      Set<Long> ids = new HashSet<>(request.getRemoveAttachmentIds());
      boolean anyRemoved = post.getMediaAttachments().removeIf(ma -> ids.contains(ma.getId()));
      if (!anyRemoved && !ids.isEmpty()) {
        throw new AppException(ErrorCode.BAD_REQUEST, "Some attachments do not belong to this post");
      }
      removedCount = ids.size();
    }

    // 3) Chuẩn bị validate thêm media mới
    List<MultipartFile> newFiles = request.getNewMediaFiles();
    int toAddCount = (newFiles == null) ? 0 : newFiles.size();

    int afterCount = (existingCount - removedCount) + toAddCount;
    if (afterCount < 0) afterCount = 0;

    // media limit
    int mediaLimit = props.getPost().getMediaLimit();
    if (afterCount > mediaLimit) {
      throw new AppException(ErrorCode.BAD_REQUEST,
        "You can upload at most %d media files".formatted(mediaLimit));
    }

    // video limit
    int existingVideoCount = (int) post.getMediaAttachments().stream()
      .filter(ma -> MediaType.VIDEO.name().equalsIgnoreCase(ma.getType()))
      .count();

    int removedVideoCount = 0;
    if (request.getRemoveAttachmentIds() != null && !request.getRemoveAttachmentIds().isEmpty()) {
      Set<Long> ids = new HashSet<>(request.getRemoveAttachmentIds());
      removedVideoCount = (int) post.getMediaAttachments().stream()
        .filter(ma -> ids.contains(ma.getId()) && MediaType.VIDEO.name().equalsIgnoreCase(ma.getType()))
        .count();
    }

    int newVideoCount = 0;
    if (newFiles != null && !newFiles.isEmpty()) {
      newVideoCount = (int) newFiles.stream().filter(this::isVideoFile).count();
    }

    int finalVideoCount = existingVideoCount - removedVideoCount + newVideoCount;
    int videoLimit = props.getPost().getVideoLimit();
    if (finalVideoCount > videoLimit) {
      throw new AppException(ErrorCode.BAD_REQUEST,
        "A post cannot contain more than %d video".formatted(videoLimit));
    }

    // 4) Upload media mới (nếu có)
    if (newFiles != null && !newFiles.isEmpty()) {
      List<String> mediaUrls = uploadClient.uploadLocalMultiple(newFiles);

      Set<MediaAttachment> newAttachments = mediaUrls.stream()
        .map(url -> MediaAttachment.builder()
          .post(post)
          .type(detectMediaType(url).name())
          .url(url)
          .build())
        .collect(Collectors.toSet());

      if (post.getMediaAttachments() == null) {
        post.setMediaAttachments(new HashSet<>());
      }
      post.getMediaAttachments().addAll(newAttachments);
    }

    // 5) Update tags
    if (request.getTagUserIds() != null) {
      for (Long id : request.getTagUserIds()) {
        if (Objects.equals(id, userId)) {
          throw new AppException(ErrorCode.BAD_REQUEST, "Can not mention yourself");
        }
      }
      post.getTags().clear();
      if (!request.getTagUserIds().isEmpty()) {
        Set<Mention> newTags = request.getTagUserIds().stream()
          .map(uid -> Mention.builder().post(post).userId(uid).build())
          .collect(Collectors.toSet());
        post.getTags().addAll(newTags);
      }
    }

    // 6) Không cho post rỗng
    boolean noText = (post.getContentText() == null || post.getContentText().isBlank());
    boolean noMedia = (post.getMediaAttachments() == null || post.getMediaAttachments().isEmpty());
    if (noText && noMedia) {
      throw new AppException(ErrorCode.BAD_REQUEST, "Post must contain text or media");
    }

    post.setMediaCount(post.getMediaAttachments() == null ? 0 : post.getMediaAttachments().size());
    Post saved = postRepo.save(post);
    return saved.toPostResponse(null, null, false);
  }

  @Override
  public void deletePost(Long userId, Long postId) {
    Post post = postRepo.findById(postId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Post with id " + postId + " not found"));

    if (post.getUserId().equals(userId)) {
      postRepo.delete(post);
    } else {
      throw new AppException(ErrorCode.BAD_REQUEST, "This post not belong to user");
    }
  }

  private boolean isVideoFile(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType != null && contentType.startsWith("video/")) return true;

    String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
    return filename.matches(".*\\.(mp4|avi|mov|mkv|webm)$");
  }

  private MediaType detectMediaType(String url) {
    String lower = url.toLowerCase();
    if (lower.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp)$")) return MediaType.IMAGE;
    if (lower.matches(".*\\.(mp4|avi|mov|mkv|webm)$")) return MediaType.VIDEO;
    return MediaType.OTHER;
  }
}
