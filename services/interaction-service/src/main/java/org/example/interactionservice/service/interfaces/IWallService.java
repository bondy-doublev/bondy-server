package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.response.PostResponse;
import org.example.interactionservice.entity.MediaAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWallService {
  Page<PostResponse> getWallFeed(Long userId, Pageable pageable);

  List<MediaAttachment> getWallMedia(Long userId, Pageable pageable);
}
