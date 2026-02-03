package halo.corebridge.demo.domain.resume.dto;

import halo.corebridge.demo.domain.resume.entity.Resume;
import halo.corebridge.demo.domain.resume.entity.ResumeVersion;
import halo.corebridge.demo.domain.resume.enums.ResumeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeDto {

    @Getter
    public static class UpdateRequest {
        private String title;
        private String content;
        private String memo;
        private List<String> skills;
    }

    @Getter
    public static class AiResultRequest {
        private String summary;
        private String skills;
    }

    @Getter @Builder
    public static class ResumeResponse {
        private Long resumeId;
        private Long userId;
        private String title;
        private String content;
        private ResumeStatus status;
        private int currentVersion;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> skills;
        private String aiSummary;
        private List<String> aiSkills;
        private LocalDateTime analyzedAt;

        public static ResumeResponse from(Resume resume) {
            return ResumeResponse.builder()
                    .resumeId(resume.getId()).userId(resume.getUserId())
                    .title(resume.getTitle()).content(resume.getContent())
                    .status(resume.getStatus()).currentVersion(resume.getCurrentVersion())
                    .createdAt(resume.getCreatedAt()).updatedAt(resume.getUpdatedAt())
                    .skills(parseSkills(resume.getSkills()))
                    .aiSummary(resume.getAiSummary())
                    .aiSkills(parseSkills(resume.getAiSkills()))
                    .analyzedAt(resume.getAnalyzedAt())
                    .build();
        }

        private static List<String> parseSkills(String json) {
            if (json == null || json.isBlank()) return List.of();
            String cleaned = json.replaceAll("[\\[\\]\"]", "");
            return cleaned.isBlank() ? List.of() : List.of(cleaned.split(",\\s*"));
        }
    }

    @Getter @Builder
    public static class VersionResponse {
        private Long versionId;
        private Long resumeId;
        private int version;
        private String title;
        private String content;
        private String memo;
        private LocalDateTime createdAt;

        public static VersionResponse from(ResumeVersion v) {
            return VersionResponse.builder()
                    .versionId(v.getId()).resumeId(v.getResumeId())
                    .version(v.getVersion()).title(v.getTitle())
                    .content(v.getContent()).memo(v.getMemo())
                    .createdAt(v.getCreatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class VersionListResponse {
        private List<VersionResponse> versions;
        private int totalCount;
    }
}
