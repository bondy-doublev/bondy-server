package org.example.interactionservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.interactionservice.entity.Friendship;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipResponse {

  Long id;               // ID của bản ghi Friendship
  Long senderId;         // Người gửi request
  Long receiverId;       // Người nhận request
  Friendship.Status status;
  LocalDateTime requestedAt;
  LocalDateTime respondedAt;

  UserBasicResponse senderInfo;   // thông tin user gửi request
  UserBasicResponse receiverInfo; // thông tin user nhận request (nếu cần)

  // Constructor tiện lợi để build từ entity + user info
  public FriendshipResponse(Friendship friendship,
                            UserBasicResponse senderInfo,
                            UserBasicResponse receiverInfo) {
    this.id = friendship.getId();
    this.senderId = friendship.getUserId();
    this.receiverId = friendship.getFriendId();
    this.status = friendship.getStatus();
    this.requestedAt = friendship.getRequestedAt();
    this.respondedAt = friendship.getRespondedAt();
    this.senderInfo = senderInfo;
    this.receiverInfo = receiverInfo;
  }
}
