package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.interactionservice.dto.request.CreateReelRequest;
import org.example.interactionservice.dto.request.UpdateReelVisibilityRequest;
import org.example.interactionservice.dto.response.ReelResponse;
import org.example.interactionservice.service.ReelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Reels (short ephemeral videos).
 */
@RestController
@RequestMapping("/reels")
@RequiredArgsConstructor
@Tag(name = "Reels", description = "Reel management APIs (ephemeral short videos)")
public class ReelController {

  private final ReelService reelService;

  @Operation(
    summary = "Create a new reel",
    description = """
      Create a new reel with video info and visibility.
      Rules:
      - If visibilityType = CUSTOM then customAllowedUserIds must be provided.
      - TTL defaults to 24 hours if not provided.
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      description = "Reel creation payload",
      content = @Content(schema = @Schema(implementation = CreateReelRequest.class))
    ),
    responses = {
      @ApiResponse(responseCode = "200", description = "Reel created successfully",
        content = @Content(schema = @Schema(implementation = ReelResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid parameters"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Server error")
    }
  )
  @PostMapping
  public ResponseEntity<ReelResponse> create(@RequestBody CreateReelRequest request) {
    return ResponseEntity.ok(reelService.createReel(request));
  }

  @Operation(
    summary = "Delete a reel (soft delete)",
    description = "Marks the reel as deleted. Only the owner can delete.",
    parameters = {
      @Parameter(name = "reelId", description = "Reel ID", required = true),
      @Parameter(name = "requesterId", description = "Requester (must be the owner)", required = true)
    },
    responses = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "401", description = "No permission"),
      @ApiResponse(responseCode = "404", description = "Reel not found")
    }
  )
  @DeleteMapping("/{reelId}")
  public ResponseEntity<Void> delete(@PathVariable Long reelId,
                                     @RequestParam Long requesterId) {
    reelService.deleteReel(reelId, requesterId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
    summary = "Update reel visibility",
    description = """
      Updates the reel visibility type.
      When switching to CUSTOM you must provide customAllowedUserIds.
      Existing custom list will be replaced.
      """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      description = "Visibility update payload",
      content = @Content(schema = @Schema(implementation = UpdateReelVisibilityRequest.class))
    ),
    responses = {
      @ApiResponse(responseCode = "200", description = "Updated successfully",
        content = @Content(schema = @Schema(implementation = ReelResponse.class))),
      @ApiResponse(responseCode = "401", description = "No permission"),
      @ApiResponse(responseCode = "404", description = "Reel not found")
    }
  )
  @PutMapping("/visibility")
  public ResponseEntity<ReelResponse> updateVisibility(@RequestBody UpdateReelVisibilityRequest request) {
    return ResponseEntity.ok(reelService.updateVisibility(request));
  }

  @Operation(
    summary = "Get currently alive (non-expired) reels visible to viewer",
    description = """
      Returns reels that are still alive (not expired, not deleted) and visible to the viewer.
      Logic:
      - If ownerId is provided: only reels from that owner (must be self or a friend).
      - If ownerId is absent: reels from all friends plus own reels.
      Visibility rules:
      - PUBLIC: visible to friends.
      - PRIVATE: only owner.
      - CUSTOM: friends explicitly listed.
      """,
    parameters = {
      @Parameter(name = "viewerId", description = "Viewer's user ID", required = true),
      @Parameter(name = "ownerId", description = "Filter by a specific owner (optional)", required = false)
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "List of visible reels",
        content = @Content(schema = @Schema(implementation = ReelResponse.class)))
    }
  )
  @GetMapping("/visible")
  public ResponseEntity<List<ReelResponse>> getVisibleReels(@RequestParam Long viewerId,
                                                            @RequestParam(required = false) Long ownerId) {
    return ResponseEntity.ok(reelService.getVisibleReels(viewerId, ownerId));
  }

  @Operation(
    summary = "Increment reel view count",
    description = """
      Increases the reel's view counter.
      Each call adds +1 (no deduplication).
      Should be called when a viewer watches the reel.
      """,
    parameters = {
      @Parameter(name = "reelId", description = "Reel ID", required = true),
      @Parameter(name = "viewerId", description = "Viewer user ID", required = true)
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "View recorded"),
      @ApiResponse(responseCode = "404", description = "Reel not found or expired")
    }
  )
  @PostMapping("/{reelId}/view")
  public ResponseEntity<Void> markViewed(@PathVariable Long reelId,
                                         @RequestParam Long viewerId) {
    reelService.incrementViewCount(reelId, viewerId);
    return ResponseEntity.ok().build();
  }

  @Operation(
    summary = "Run reel expiration job (debug)",
    description = "Marks expired reels as deleted. Intended for debugging/manual triggering.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Number of reels expired")
    }
  )
  @PostMapping("/expire-run")
  public ResponseEntity<String> expireJob() {
    int count = reelService.expireReelsJob();
    return ResponseEntity.ok("Expired reels: " + count);
  }

  @Operation(
    summary = "Mark reel as read (per-user)",
    description = """
      Creates an idempotent record that the viewer has read the reel.
      Different from view count (which increments every call).
      """,
    parameters = {
      @Parameter(name = "reelId", description = "Reel ID", required = true),
      @Parameter(name = "viewerId", description = "Reader user ID", required = true)
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "Marked as read"),
      @ApiResponse(responseCode = "401", description = "No permission to view"),
      @ApiResponse(responseCode = "404", description = "Reel not found")
    }
  )
  @PostMapping("/{reelId}/read")
  public ResponseEntity<Void> markRead(@PathVariable Long reelId,
                                       @RequestParam Long viewerId) {
    reelService.markRead(reelId, viewerId);
    return ResponseEntity.ok().build();
  }

  @Operation(
    summary = "Get all viewable reels ignoring expiration",
    description = """
      Returns all non-deleted reels the requester can view, ignoring expiration time.
      - If ownerId provided: only that owner's reels (must be self or friend).
      - Else: all friends' reels plus own reels.
      Visibility evaluation same as /visible but without expiry filtering.
      """,
    parameters = {
      @Parameter(name = "requesterId", description = "Requester (viewer) user ID", required = true),
      @Parameter(name = "ownerId", description = "Optional owner filter", required = false)
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "List of reels",
        content = @Content(schema = @Schema(implementation = ReelResponse.class)))
    }
  )
  @GetMapping("/all")
  public ResponseEntity<List<ReelResponse>> getAllReels(@RequestParam Long requesterId,
                                                        @RequestParam(required = false) Long ownerId) {
    return ResponseEntity.ok(reelService.getAllReels(requesterId, ownerId));
  }
}