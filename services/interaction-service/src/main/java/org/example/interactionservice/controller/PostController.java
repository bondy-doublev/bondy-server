package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.service.interfaces.IPostService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Post")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  IPostService postService;

  @GetMapping("/new-feed")
  AppApiResponse getPosts(@ModelAttribute @Valid PageRequestDto filter) {
    Page<PostResponse> posts = postService.getNewFeed(filter.toPageable());

    return new AppApiResponse(posts);
  }

  @GetMapping("/wall")
  AppApiResponse getWall(@ModelAttribute @Valid PageRequestDto filter) {
    Page<PostResponse> posts = postService.getWall(ContextUser.get().getUserId(), filter.toPageable());

    return new AppApiResponse(posts);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  AppApiResponse createPost(
    @RequestParam(value = "content", required = false) String content,
    @RequestParam(value = "tagUserIds", required = false) List<Long> tagUserIds,
    @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
  ) {
    CreatePostRequest request = new CreatePostRequest();
    request.setContent(content);
    request.setTagUserIds(tagUserIds);
    request.setMediaFiles(mediaFiles);

    PostResponse newPost = postService.createPost(ContextUser.get().getUserId(), request);
    return new AppApiResponse(newPost);
  }
}
