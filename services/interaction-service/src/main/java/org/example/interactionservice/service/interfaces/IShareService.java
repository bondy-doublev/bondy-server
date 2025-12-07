package org.example.interactionservice.service.interfaces;

import org.example.interactionservice.dto.response.PostResponse;

public interface IShareService {

  PostResponse createShare(Long userId, Long originalPostId, String message, Boolean isPublic);

  void deleteShare(Long userId, Long sharePostId);
}
