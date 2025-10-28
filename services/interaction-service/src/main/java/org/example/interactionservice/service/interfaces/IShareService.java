package org.example.interactionservice.service.interfaces;

import jakarta.transaction.Transactional;
import org.example.interactionservice.entity.Share;

public interface IShareService {
  @Transactional
  Share createShare(Long userId, Long postId);

  @Transactional
  void deleteShare(Long userId, Long shareId);
}
