package org.example.notificationservice.controller;

import org.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Thêm notification
    @GetMapping("/notify")
    public String notify(@RequestParam("msg") String msg) {
        notificationService.addNotification(msg);
        return "Notification added: " + msg;
    }


    // Lấy tất cả notification
    @GetMapping("/notifications")
    public List<String> list() {
        return notificationService.getAllNotifications();
    }

    // Xóa tất cả notification
    @DeleteMapping("/notifications")
    public String clear() {
        notificationService.clearAll();
        return "All notifications cleared";
    }
}
