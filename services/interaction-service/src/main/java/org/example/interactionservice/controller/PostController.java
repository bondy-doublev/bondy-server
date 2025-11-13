package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.request.CreatePostRequest;
import org.example.interactionservice.dto.request.UpdatePostRequest;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.service.interfaces.IPostService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  IPostService postService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  AppApiResponse createPost(@ModelAttribute @Valid CreatePostRequest request) {
    PostResponse newPost = postService.createPost(ContextUser.get().getUserId(), request);
    return new AppApiResponse(newPost);
  }

  @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  AppApiResponse updatePost(
    @PathVariable Long postId,
    @Valid @ModelAttribute UpdatePostRequest request
  ) {
    PostResponse updated = postService.updatePost(ContextUser.get().getUserId(), postId, request);
    return new AppApiResponse(updated);
  }

  @DeleteMapping("/{postId}")
  AppApiResponse deletePost(@PathVariable Long postId) {
    postService.deletePost(ContextUser.get().getUserId(), postId);

    return new AppApiResponse();
  }
}
