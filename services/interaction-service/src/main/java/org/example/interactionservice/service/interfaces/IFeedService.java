package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IFeedService {
  Page<PostResponse> getFeed(Pageable pageable);
}
