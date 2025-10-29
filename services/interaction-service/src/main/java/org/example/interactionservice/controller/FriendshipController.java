package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.dto.response.FriendSuggestResponse;
import org.example.interactionservice.dto.response.FriendshipResponse;
import org.example.interactionservice.entity.Friendship;
import org.example.interactionservice.service.FriendshipService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friendship", description = "Manage friend requests and friends")
@RestController
@RequestMapping("/friendships")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FriendshipController {

  private final FriendshipService friendshipService;

  @Operation(summary = "Send friend request")
  @PostMapping("/request")
  public AppApiResponse sendFriendRequest(
    @RequestParam Long senderId,
    @RequestParam Long receiverId) {
    Friendship friendship = friendshipService.sendFriendRequest(senderId, receiverId);
    return new AppApiResponse(friendship);
  }

  @Operation(summary = "Suggest friends for a user")
  @GetMapping("/suggest/{userId}")
  public AppApiResponse suggestFriends(
    @PathVariable("userId") Long userId,
    @RequestParam(defaultValue = "0") int page) {
    List<FriendSuggestResponse> suggestions = friendshipService.suggestFriends(userId, page);
    return new AppApiResponse(suggestions);
  }


  @Operation(summary = "Accept friend request")
  @PostMapping("/accept")
  public AppApiResponse acceptFriendRequest(
    @RequestParam Long receiverId,
    @RequestParam Long senderId) {
    Friendship friendship = friendshipService.acceptFriendRequest(receiverId, senderId);
    return new AppApiResponse(friendship);
  }

  @Operation(summary = "Reject friend request")
  @PostMapping("/reject")
  public AppApiResponse rejectFriendRequest(
    @RequestParam Long receiverId,
    @RequestParam Long senderId) {
    friendshipService.rejectFriendRequest(receiverId, senderId);
    return new AppApiResponse("Rejected successfully");
  }

  @Operation(summary = "Get accepted friends of a user")
  @GetMapping("/friends/{userId}")
  public AppApiResponse getFriends(@PathVariable("userId") Long userId) {
    List<FriendshipResponse> friends = friendshipService.getFriends(userId);
    return new AppApiResponse(friends);
  }

  @Operation(summary = "Get pending friend requests to a user")
  @GetMapping("/pending/{userId}")
  public AppApiResponse getPendingRequests(@PathVariable("userId") Long userId) {
    List<FriendshipResponse> friendships = friendshipService.getPendingRequests(userId);
    return new AppApiResponse(friendships);
  }

  @Operation(summary = "Get all friend requests sent by user that are pending")
  @GetMapping("/pending-sent/{userId}")
  public AppApiResponse getPendingSentRequests(@PathVariable("userId") Long userId) {
    List<FriendshipResponse> pendingSent = friendshipService.getPendingSentRequests(userId);
    return new AppApiResponse(pendingSent);
  }
}
