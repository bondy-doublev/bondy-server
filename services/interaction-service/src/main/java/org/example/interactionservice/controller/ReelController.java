package org.example.interactionservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.interactionservice.dto.request.CreateReelRequest;
import org.example.interactionservice.dto.request.UpdateReelVisibilityRequest;
import org.example.interactionservice.dto.response.ReelResponse;
import org.example.interactionservice.service.ReelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ví dụ REST controller. Bạn có thể chỉnh path /api/v1 tùy theo project.
 */
@RestController
@RequestMapping("/reels")
@RequiredArgsConstructor
public class ReelController {

  private final ReelService reelService;

  @PostMapping
  public ResponseEntity<ReelResponse> create(@RequestBody CreateReelRequest request) {
    return ResponseEntity.ok(reelService.createReel(request));
  }

  @DeleteMapping("/{reelId}")
  public ResponseEntity<Void> delete(@PathVariable Long reelId,
                                     @RequestParam Long requesterId) {
    reelService.deleteReel(reelId, requesterId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/visibility")
  public ResponseEntity<ReelResponse> updateVisibility(@RequestBody UpdateReelVisibilityRequest request) {
    return ResponseEntity.ok(reelService.updateVisibility(request));
  }

  @GetMapping("/visible")
  public ResponseEntity<List<ReelResponse>> getVisibleReels(@RequestParam Long viewerId,
                                                            @RequestParam(required = false) Long ownerId) {
    return ResponseEntity.ok(reelService.getVisibleReels(viewerId, ownerId));
  }

  @PostMapping("/{reelId}/view")
  public ResponseEntity<Void> markViewed(@PathVariable Long reelId,
                                         @RequestParam Long viewerId) {
    reelService.incrementViewCount(reelId, viewerId);
    return ResponseEntity.ok().build();
  }

  // Endpoint manual expire (debug)
  @PostMapping("/expire-run")
  public ResponseEntity<String> expireJob() {
    int count = reelService.expireReelsJob();
    return ResponseEntity.ok("Expired reels: " + count);
  }
}