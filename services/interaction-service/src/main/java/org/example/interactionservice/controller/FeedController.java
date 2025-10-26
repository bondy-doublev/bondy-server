package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.dto.response.FeedItemResponse;
import org.example.interactionservice.service.interfaces.IFeedService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed")
@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedController {
  IFeedService feedService;

  @GetMapping()
  AppApiResponse getFeeds(@ModelAttribute @Valid PageRequestDto filter) {
    Page<FeedItemResponse> feeds = feedService.getFeed(filter.toPageable());

    return new AppApiResponse(feeds);
  }
}
