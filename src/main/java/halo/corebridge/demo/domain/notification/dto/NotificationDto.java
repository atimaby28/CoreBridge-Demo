package halo.corebridge.demo.domain.notification.dto;

import halo.corebridge.demo.domain.notification.entity.Notification;
import halo.corebridge.demo.domain.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class NotificationDto {

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotNull private Long userId;
        @NotNull private NotificationType type;
        @NotBlank @Size(max = 100) private String title;
        @NotBlank @Size(max = 500) private String message;
        @Size(max = 500) private String link;
        private Long relatedId;
        private String relatedType;

        public Notification toEntity(Long id) {
            return Notification.builder()
                    .id(id).userId(userId).type(type)
                    .title(title).message(message).link(link)
                    .relatedId(relatedId).relatedType(relatedType)
                    .isRead(false).build();
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private NotificationType type;
        private String typeDescription;
        private String title;
        private String message;
        private String link;
        private boolean isRead;
        private Long relatedId;
        private String relatedType;
        private LocalDateTime createdAt;

        public static Response from(Notification n) {
            return Response.builder()
                    .id(n.getId()).type(n.getType())
                    .typeDescription(n.getType().getDescription())
                    .title(n.getTitle()).message(n.getMessage())
                    .link(n.getLink()).isRead(n.isRead())
                    .relatedId(n.getRelatedId()).relatedType(n.getRelatedType())
                    .createdAt(n.getCreatedAt())
                    .build();
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UnreadCountResponse {
        private long count;
        public static UnreadCountResponse of(long count) { return new UnreadCountResponse(count); }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateResponse {
        private Long id;
        private boolean success;
        private String message;

        public static CreateResponse success(Long id) {
            return CreateResponse.builder().id(id).success(true).message("알림이 생성되었습니다").build();
        }
        public static CreateResponse fail(String reason) {
            return CreateResponse.builder().success(false).message(reason).build();
        }
    }
}
