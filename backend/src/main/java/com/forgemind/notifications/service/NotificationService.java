package com.forgemind.notifications.service;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.notifications.dto.NotificationResponse;
import com.forgemind.notifications.dto.UnreadCountResponse;
import com.forgemind.notifications.entity.Notification;
import com.forgemind.notifications.mapper.NotificationMapper;
import com.forgemind.notifications.repository.NotificationRepository;
import com.forgemind.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        return new UnreadCountResponse(notificationRepository.countByUser_IdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void clearAll(UUID userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void createNotification(User user, String title, String message, String actionUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }
}