package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.request.UnfriendRequest;
import org.example.interactionservice.dto.response.FriendshipResponse;
import org.example.interactionservice.entity.Friendship;
import org.example.interactionservice.service.FriendshipService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friendship", description = "Manage friend requests and friends")
@RestController
@RequestMapping("/friendships")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FriendshipController {

  FriendshipService friendshipService;

  @Operation(summary = "Send friend request")
  @PostMapping("/request")
  public AppApiResponse sendFriendRequest(
    @RequestParam Long senderId,
    @RequestParam Long receiverId
  ) {
    Friendship friendship = friendshipService.sendFriendRequest(senderId, receiverId);
    return new AppApiResponse(friendship);
  }

  @Operation(summary = "Suggest friends for a user")
  @GetMapping("/suggest/{userId}")
  public AppApiResponse suggestFriends(
    @PathVariable("userId") Long userId,
    @RequestParam(defaultValue = "0") int page
  ) {
    var suggestions = friendshipService.suggestFriends(userId, page);
    return new AppApiResponse(suggestions);
  }

  @Operation(summary = "Accept friend request")
  @PostMapping("/accept")
  public AppApiResponse acceptFriendRequest(
    @RequestParam Long receiverId,
    @RequestParam Long senderId
  ) {
    Friendship friendship = friendshipService.acceptFriendRequest(receiverId, senderId);
    return new AppApiResponse(friendship);
  }

  @Operation(summary = "Reject friend request")
  @PostMapping("/reject")
  public AppApiResponse rejectFriendRequest(
    @RequestParam Long receiverId,
    @RequestParam Long senderId
  ) {
    friendshipService.rejectFriendRequest(receiverId, senderId);
    return new AppApiResponse("Rejected successfully");
  }

  @Operation(summary = "Get accepted friends of a user (paged)")
  @GetMapping("/friends/{userId}")
  public AppApiResponse getFriends(
    @PathVariable("userId") Long userId,
    @ModelAttribute @Valid PageRequestDto filter
  ) {
    Page<FriendshipResponse> friends =
      friendshipService.getFriends(userId, filter.toPageable());
    return new AppApiResponse(friends);
  }

  @Operation(summary = "Get pending friend requests to a user (paged)")
  @GetMapping("/pending/{userId}")
  public AppApiResponse getPendingRequests(
    @PathVariable("userId") Long userId,
    @ModelAttribute @Valid PageRequestDto filter
  ) {
    Page<FriendshipResponse> friendships =
      friendshipService.getPendingRequests(userId, filter.toPageable());
    return new AppApiResponse(friendships);
  }

  @Operation(summary = "Get all friend requests sent by user that are pending (paged)")
  @GetMapping("/pending-sent/{userId}")
  public AppApiResponse getPendingSentRequests(
    @PathVariable("userId") Long userId,
    @ModelAttribute @Valid PageRequestDto filter
  ) {
    Page<FriendshipResponse> pendingSent =
      friendshipService.getPendingSentRequests(userId, filter.toPageable());
    return new AppApiResponse(pendingSent);
  }

  @GetMapping("/status/{userId}")
  public AppApiResponse getFriendShipStatus(@PathVariable("userId") Long userId) {
    FriendshipResponse status =
      friendshipService.getFriendshipStatus(ContextUser.get().getUserId(), userId);
    return new AppApiResponse(status);
  }

  @PostMapping("/unfriend")
  public AppApiResponse unFriend(@RequestBody @Valid UnfriendRequest request) {
    friendshipService.unFriend(ContextUser.get().getUserId(), request.getUserId());
    return new AppApiResponse();
  }
}
