package org.example.interactionservice.scheduler.reel;

import lombok.RequiredArgsConstructor;
import org.example.interactionservice.service.ReelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cần bật @EnableScheduling ở cấu hình ứng dụng.
 */
@Component
@RequiredArgsConstructor
public class ReelExpire {

  private final ReelService reelService;

  // Chạy mỗi 15 phút
  @Scheduled(cron = "0 */15 * * * *")
  public void cleanExpiredReels() {
    reelService.expireReelsJob();
  }
}