package halo.corebridge.demo.domain.comment.dto;

import halo.corebridge.demo.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {

    @Getter @Builder
    public static class CreateRequest {
        private Long jobpostingId;
        private String content;
        private Long parentCommentId;
    }

    @Getter @Builder
    public static class CommentResponse {
        private Long commentId;
        private String content;
        private Long parentCommentId;
        private Long jobpostingId;
        private Long userId;
        private Boolean deleted;
        private LocalDateTime createdAt;

        public static CommentResponse from(Comment c) {
            return CommentResponse.builder()
                    .commentId(c.getCommentId()).content(c.getContent())
                    .parentCommentId(c.getParentCommentId())
                    .jobpostingId(c.getJobpostingId()).userId(c.getUserId())
                    .deleted(c.getDeleted()).createdAt(c.getCreatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class CommentPageResponse {
        private List<CommentResponse> comments;
        private Long commentCount;

        public static CommentPageResponse of(List<CommentResponse> list, Long count) {
            return CommentPageResponse.builder().comments(list).commentCount(count).build();
        }
    }
}
