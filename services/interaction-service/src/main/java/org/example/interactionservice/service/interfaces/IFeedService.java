package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.response.FeedItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IFeedService {
  Page<FeedItemResponse> getFeed(Pageable pageable);
}
