package halo.corebridge.demo.domain.notification.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.notification.dto.NotificationDto;
import halo.corebridge.demo.domain.notification.entity.Notification;
import halo.corebridge.demo.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final Snowflake snowflake;
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDto.CreateResponse create(NotificationDto.CreateRequest request) {
        try {
            Notification notification = request.toEntity(snowflake.nextId());
            Notification saved = notificationRepository.save(notification);
            log.info("알림 생성: userId={}, type={}, id={}",
                    request.getUserId(), request.getType(), saved.getId());
            return NotificationDto.CreateResponse.success(saved.getId());
        } catch (Exception e) {
            log.error("알림 생성 실패: userId={}, error={}", request.getUserId(), e.getMessage());
            return NotificationDto.CreateResponse.fail(e.getMessage());
        }
    }

    public Page<NotificationDto.Response> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDto.Response::from);
    }

    public Page<NotificationDto.Response> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDto.Response::from);
    }

    public NotificationDto.UnreadCountResponse getUnreadCount(Long userId) {
        return NotificationDto.UnreadCountResponse.of(
                notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    public List<NotificationDto.Response> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto.Response::from).toList();
    }

    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(n -> { n.markAsRead(); return true; })
                .orElse(false);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("모든 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    public NotificationDto.Response getNotification(Long userId, Long notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(NotificationDto.Response::from).orElse(null);
    }
}
