package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.entity.Share;
import org.example.interactionservice.service.interfaces.IShareService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Share")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShareController {
  IShareService shareService;

  @PostMapping("/{postId}/share")
  AppApiResponse createShare(@PathVariable Long postId) {
    Share share = shareService.createShare(ContextUser.get().getUserId(), postId);

    return new AppApiResponse(share);
  }

  @DeleteMapping("/{postId}/share")
  AppApiResponse deleteShare(@PathVariable Long postId) {
    shareService.deleteShare(ContextUser.get().getUserId(), postId);

    return new AppApiResponse("Delete share post successfully");
  }
}
