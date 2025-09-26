package org.example.notificationservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final List<String> notifications = new ArrayList<>();

    public void addNotification(String msg) {
        notifications.add(msg);
    }

    public List<String> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearAll() {
        notifications.clear();
    }
}
