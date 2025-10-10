package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
  @PostMapping("/{postId}/comments")
  AppApiResponse createComment(
    @PathVariable Long postId,
    @RequestBody @Valid CreateCommentRequest request) {
    return new AppApiResponse();
  }

  @GetMapping("/{postId}/comments")
  AppApiResponse getComments(@PathVariable Long postId) {
    return new AppApiResponse();
  }
}
