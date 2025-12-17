package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.service.interfaces.IWallService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wall")
@RestController
@RequestMapping("/wall")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WallController {
  IWallService wallService;

  @GetMapping("/{userId}/feeds")
  AppApiResponse getWall(@PathVariable Long userId, @ModelAttribute @Valid PageRequestDto filter) {
    Page<PostResponse> feeds = wallService.getWallFeed(userId, filter.toPageable());

    return new AppApiResponse(feeds);
  }

//  @GetMapping("/{userId}/medias")
//  AppApiResponse getWallMedia(@PathVariable Long userId, @ModelAttribute @Valid PageRequestDto filter) {
//    List<MediaAttachment> medias = wallService.getWallMedia(userId, filter.toPageable());
//
//    return new AppApiResponse(medias);
//  }
}
