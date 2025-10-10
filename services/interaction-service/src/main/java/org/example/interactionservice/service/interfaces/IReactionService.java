package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.response.ReactionResponse;

public interface IReactionService {
  ReactionResponse reaction(Long userId, Long postId);
}
