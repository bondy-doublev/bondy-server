package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.request.CreateCommentRequest;
import org.example.interactionservice.dto.response.CommentResponse;
import org.example.interactionservice.service.interfaces.ICommentService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
  ICommentService commentService;

  @PostMapping("/{postId}/comments")
  AppApiResponse createComment(
    @PathVariable @Valid @NotNull(message = "Post ID is required") Long postId,
    @RequestBody @Valid CreateCommentRequest request) {
    CommentResponse response = commentService.createComment(ContextUser.get().getUserId(), postId, request);

    return new AppApiResponse(response);
  }

  @GetMapping("/{postId}/comments")
  AppApiResponse getComments(
    @PathVariable @Valid @NotNull(message = "Post ID is required") Long postId,
    @RequestParam(required = false) Long parentId,
    @ModelAttribute @Valid PageRequestDto filter) {

    Page<CommentResponse> responses = commentService.getPostComments(postId, parentId, filter.toPageable());
    return new AppApiResponse(responses);
  }
}
