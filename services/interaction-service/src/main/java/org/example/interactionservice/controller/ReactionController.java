package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.response.ReactionResponse;
import org.example.interactionservice.service.interfaces.IReactionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reaction")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionController {
  IReactionService reactionService;

  @PutMapping("/{postId}/reaction")
  public AppApiResponse reaction(@PathVariable Long postId) {
    ReactionResponse reaction = reactionService.reaction(ContextUser.get().getUserId(), postId);

    return new AppApiResponse(reaction);
  }
}
