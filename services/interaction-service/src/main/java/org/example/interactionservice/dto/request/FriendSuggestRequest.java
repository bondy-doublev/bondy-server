package org.example.interactionservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.interactionservice.dto.response.UserBasicResponse;

@Data
@AllArgsConstructor
public class FriendSuggestRequest {
  private UserBasicResponse user;
  private long mutualFriends;
  private boolean nearBy;
}
