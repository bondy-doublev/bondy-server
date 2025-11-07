package org.example.notificationservice.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationCleanupJob {
  NotificationRepository notificationRepository;

  @Scheduled(cron = "0 0 2 * * *")
  public void notificationCleanup() {
    log.info("🧹 Start clean up notifications older than 60 days...");
    notificationRepository.deleteOldNotifications(LocalDateTime.now().minusDays(60));
    log.info("✅ Clean up done!");
  }
}
