package org.example.interactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.client.UploadClient;
import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.entity.MediaAttachment;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.enums.MediaType;
import org.example.interactionservice.property.PropsConfig;
import org.example.interactionservice.repository.PostRepository;
import org.example.interactionservice.service.interfaces.IPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {
    PropsConfig props;
    PostRepository postRepo;

    UploadClient uploadClient;

    @Override
    public Page<Post> getNewFeed(Pageable pageable) {
        return postRepo.findAll(pageable);
    }

    @Override
    public Page<Post> getWall(Long userId, Pageable pageable) {
        return postRepo.findByUserId(userId, pageable);
    }

    @Override
    public Post createPost(Long ownerId, CreatePostRequest request) {
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

        return postRepo.save(newPost);
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
