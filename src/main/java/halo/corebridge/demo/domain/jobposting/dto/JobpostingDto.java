package halo.corebridge.demo.domain.jobposting.dto;

import halo.corebridge.demo.domain.jobposting.entity.Jobposting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class JobpostingDto {

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 2, max = 100)
        private String title;
        @NotBlank(message = "내용은 필수입니다")
        private String content;
        @NotNull(message = "게시판 ID는 필수입니다")
        private Long boardId;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        @Size(min = 2, max = 100)
        private String title;
        private String content;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
    }

    @Getter @Builder
    public static class JobpostingResponse {
        private Long jobpostingId;
        private String title;
        private String content;
        private Long boardId;
        private Long userId;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static JobpostingResponse from(Jobposting jp) {
            return JobpostingResponse.builder()
                    .jobpostingId(jp.getJobpostingId())
                    .title(jp.getTitle()).content(jp.getContent())
                    .boardId(jp.getBoardId()).userId(jp.getUserId())
                    .requiredSkills(parseSkills(jp.getRequiredSkills()))
                    .preferredSkills(parseSkills(jp.getPreferredSkills()))
                    .createdAt(jp.getCreatedAt()).updatedAt(jp.getUpdatedAt())
                    .build();
        }

        private static List<String> parseSkills(String skillsJson) {
            if (skillsJson == null || skillsJson.isBlank()) return List.of();
            String cleaned = skillsJson.replaceAll("[\\[\\]\"]", "");
            return cleaned.isBlank() ? List.of() : List.of(cleaned.split(",\\s*"));
        }
    }

    @Getter @Builder
    public static class JobpostingPageResponse {
        private List<JobpostingResponse> jobpostings;
        private Long jobpostingCount;

        public static JobpostingPageResponse of(List<JobpostingResponse> list, Long count) {
            return JobpostingPageResponse.builder().jobpostings(list).jobpostingCount(count).build();
        }
    }

    @Getter @Builder
    public static class JobpostingListResponse {
        private List<JobpostingResponse> jobpostings;

        public static JobpostingListResponse of(List<JobpostingResponse> list) {
            return JobpostingListResponse.builder().jobpostings(list).build();
        }
    }
}
