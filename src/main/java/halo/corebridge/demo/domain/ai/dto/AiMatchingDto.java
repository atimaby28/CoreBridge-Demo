package halo.corebridge.demo.domain.ai.dto;

import lombok.*;

import java.util.List;

public class AiMatchingDto {

    // ============================================
    // Request
    // ============================================

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class MatchCandidatesRequest {
        private String jdText;
        private List<String> requiredSkills;
        private Integer topK;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class MatchJobpostingsRequest {
        private String resumeText;
        private Integer topK;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class ScoreRequest {
        private String candidateId;
        private String jdText;
        private List<String> requiredSkills;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class SkillGapRequest {
        private String candidateId;
        private String jobpostingId;
    }

    // ============================================
    // Response
    // ============================================

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MatchCandidatesResponse {
        private List<MatchedCandidate> matches;
        private int totalCount;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MatchJobpostingsResponse {
        private List<MatchedJobposting> matches;
        private int totalCount;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MatchedCandidate {
        private String candidateId;
        private String userId;
        private String resumeId;
        private Double score;
        private String name;
        private List<String> skills;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MatchedJobposting {
        private String jobpostingId;
        private Double score;
        private String title;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScoreResponse {
        private String candidateId;
        private List<String> requiredSkills;
        private List<String> candidateSkills;
        private Double cosineSimilarity;
        private ScoreDetail scoreDetail;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScoreDetail {
        private Double skillScore;
        private Double similarityScore;
        private Double bonusScore;
        private Double totalScore;
        private String grade;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SkillGapResponse {
        private String candidateId;
        private String jobpostingId;
        private List<String> candidateSkills;
        private List<String> requiredSkills;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private Double matchRate;
        private Double cosineSimilarity;
    }
}
