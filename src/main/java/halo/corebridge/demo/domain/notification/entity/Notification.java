package halo.corebridge.demo.domain.notification.entity;

import halo.corebridge.demo.common.domain.BaseTimeEntity;
import halo.corebridge.demo.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_noti_user_id", columnList = "userId"),
        @Index(name = "idx_noti_user_read", columnList = "userId, isRead"),
        @Index(name = "idx_noti_created_at", columnList = "createdAt DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 500)
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    private Long relatedId;

    @Column(length = 50)
    private String relatedType;

    public void markAsRead() {
        this.isRead = true;
    }
}
