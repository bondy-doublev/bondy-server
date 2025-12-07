package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.request.ShareCreateRequest;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.service.interfaces.IShareService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share")
@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShareController {

  IShareService shareService;

  @PostMapping("/posts/{postId}/share")
  AppApiResponse createShare(
    @PathVariable Long postId,
    @RequestBody(required = false) ShareCreateRequest request
  ) {
    Long userId = ContextUser.get().getUserId();

    String message = request != null ? request.getMessage() : null;
    Boolean isPublic = request != null ? request.getIsPublic() : null;

    PostResponse shareResponse = shareService.createShare(userId, postId, message, isPublic);

    return new AppApiResponse(shareResponse);
  }

  @DeleteMapping("/shares/{shareId}")
  AppApiResponse deleteShare(@PathVariable Long shareId) {
    Long userId = ContextUser.get().getUserId();

    shareService.deleteShare(userId, shareId);

    return new AppApiResponse("Delete share post successfully");
  }
}
