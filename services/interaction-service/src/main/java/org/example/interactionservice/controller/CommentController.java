package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment")
@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
  @PostMapping("/posts/{postId}/comments")
  AppApiResponse createComment(@PathVariable String postId) {
    return new AppApiResponse();
  }

  @GetMapping("/posts/{postId}/comments")
  AppApiResponse getComments(@PathVariable String postId) {
    return new AppApiResponse();
  }
}
