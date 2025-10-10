package org.example.interactionservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendSuggestResponse {

  Long userId;
  String fullName;
  String avatarUrl;
  Long mutualFriends;
  Boolean nearBy;
  Boolean pendingReceived;

  // Constructor từ UserBasicResponse + thông tin gợi ý
  public FriendSuggestResponse(UserBasicResponse user, long mutualFriends, boolean nearBy) {
    this.userId = user.getId();
    this.fullName = user.getFullName();
    this.avatarUrl = user.getAvatarUrl();
    this.mutualFriends = mutualFriends;
    this.nearBy = nearBy;
    this.pendingReceived = false;
  }

  public FriendSuggestResponse(UserBasicResponse user, long mutualFriends, boolean nearBy, boolean pendingReceived) {
    this.userId = user.getId();
    this.fullName = user.getFullName();
    this.avatarUrl = user.getAvatarUrl();
    this.mutualFriends = mutualFriends;
    this.nearBy = nearBy;
    this.pendingReceived = pendingReceived;
  }
}
