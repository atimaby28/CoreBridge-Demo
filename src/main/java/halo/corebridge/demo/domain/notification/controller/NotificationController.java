package halo.corebridge.demo.domain.notification.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.notification.dto.NotificationDto;
import halo.corebridge.demo.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public BaseResponse<Page<NotificationDto.Response>> getMyNotifications(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return BaseResponse.success(notificationService.getMyNotifications(userId, pageable));
    }

    @GetMapping("/unread")
    public BaseResponse<Page<NotificationDto.Response>> getUnreadNotifications(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return BaseResponse.success(notificationService.getUnreadNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    public BaseResponse<NotificationDto.UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        return BaseResponse.success(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/recent")
    public BaseResponse<List<NotificationDto.Response>> getRecentNotifications(
            @AuthenticationPrincipal Long userId) {
        return BaseResponse.success(notificationService.getRecentNotifications(userId));
    }

    @GetMapping("/{id}")
    public BaseResponse<NotificationDto.Response> getNotification(
            @AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return BaseResponse.success(notificationService.getNotification(userId, id));
    }

    @PatchMapping("/{id}/read")
    public BaseResponse<Boolean> markAsRead(
            @AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return BaseResponse.success(notificationService.markAsRead(userId, id));
    }

    @PatchMapping("/read-all")
    public BaseResponse<Integer> markAllAsRead(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(notificationService.markAllAsRead(userId));
    }
}
