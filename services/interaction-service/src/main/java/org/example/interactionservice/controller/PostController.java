package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.ApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.entity.Post;
import org.example.interactionservice.service.interfaces.IPostService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Post")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  IPostService postService;

  @GetMapping("/new-feed")
  ApiResponse getPosts(PageRequestDto dto) {
    Page<Post> posts = postService.getNewFeed(dto.toPageable());

    return new ApiResponse(posts);
  }

  @GetMapping("/wall")
  ApiResponse getWall(PageRequestDto dto) {
    Page<Post> posts = postService.getWall(ContextUser.get().getUserId(), dto.toPageable());

    return new ApiResponse(posts);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse createPost(
    @RequestPart(value = "content", required = false) String content,
    @RequestPart(value = "tagUserIds", required = false) String tagUserIdsCsv,
    @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
  ) {
    CreatePostRequest request = new CreatePostRequest();
    request.setContent(content);
    if (tagUserIdsCsv != null && !tagUserIdsCsv.isBlank()) {
      List<Long> ids = Arrays.stream(tagUserIdsCsv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::valueOf)
        .collect(Collectors.toList());
      request.setTagUserIds(ids);
    }
    request.setMediaFiles(mediaFiles);

    Post newPost = postService.createPost(ContextUser.get().getUserId(), request);
    return new ApiResponse(newPost);
  }

  @DeleteMapping
  ApiResponse deletePost(@RequestParam Long postId) {
    postService.deletePost(ContextUser.get().getUserId(), postId);

    return new ApiResponse();
  }
}
