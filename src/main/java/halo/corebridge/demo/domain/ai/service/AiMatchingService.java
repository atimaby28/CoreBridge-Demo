package halo.corebridge.demo.domain.ai.service;

import halo.corebridge.demo.domain.ai.dto.AiMatchingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * AI Matching Mock Service
 *
 * MSA에서는 FastAPI + Ollama + Redis Vector Search를 통해
 * 코사인 유사도 기반 매칭을 수행하지만,
 * 데모에서는 시뮬레이션 데이터를 즉시 반환합니다.
 *
 * 실제 MSA 아키텍처:
 *   ApplyService → AiMatchingClient(Feign) → FastAPI → Ollama(임베딩) → Redis(벡터 검색)
 *
 * 데모 Mock:
 *   ApplyService → AiMatchingService (즉시 Mock 응답)
 */
@Slf4j
@Service
public class AiMatchingService {

    private static final List<List<String>> MOCK_SKILL_SETS = List.of(
            List.of("Java", "Spring Boot", "JPA", "MSA"),
            List.of("Python", "FastAPI", "Django", "ML"),
            List.of("React", "TypeScript", "Next.js", "Node.js"),
            List.of("Kotlin", "Spring", "Redis", "Kafka"),
            List.of("Go", "gRPC", "Docker", "Kubernetes")
    );

    private static final List<String> MOCK_NAMES = List.of(
            "김개발", "이백엔", "박프론", "최클라", "정데브"
    );

    private static final List<String> MOCK_JOB_TITLES = List.of(
            "[네이버] 백엔드 개발자 (Spring/Kotlin)",
            "[카카오] 서버 개발자 (Java/MSA)",
            "[쿠팡] 풀스택 개발자 (React/Node)",
            "[라인] 플랫폼 엔지니어 (Go/K8s)",
            "[토스] 서버 개발자 (Kotlin/Spring)"
    );

    /**
     * 후보자 매칭 (회사용)
     * MSA: Redis Vector Search로 JD와 이력서 벡터 코사인 유사도 비교
     * Mock: topK만큼 시뮬레이션 후보자 반환 (스코어 내림차순)
     */
    public AiMatchingDto.MatchCandidatesResponse matchCandidates(AiMatchingDto.MatchCandidatesRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        topK = Math.min(topK, 5);

        log.info("[AI Mock] 후보자 매칭 요청: topK={}", topK);

        List<AiMatchingDto.MatchedCandidate> matches = IntStream.range(0, topK)
                .mapToObj(i -> AiMatchingDto.MatchedCandidate.builder()
                        .candidateId(String.valueOf(100 + i))
                        .userId(String.valueOf(100 + i))
                        .resumeId(String.valueOf(200 + i))
                        .score(0.95 - (i * 0.08))
                        .name(MOCK_NAMES.get(i % MOCK_NAMES.size()))
                        .skills(MOCK_SKILL_SETS.get(i % MOCK_SKILL_SETS.size()))
                        .build())
                .toList();

        return AiMatchingDto.MatchCandidatesResponse.builder()
                .matches(matches).totalCount(matches.size()).build();
    }

    /**
     * 채용공고 매칭 (구직자용)
     * MSA: 이력서 임베딩 → Redis에서 유사 JD 검색
     * Mock: topK만큼 시뮬레이션 채용공고 반환
     */
    public AiMatchingDto.MatchJobpostingsResponse matchJobpostings(AiMatchingDto.MatchJobpostingsRequest request) {
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        topK = Math.min(topK, 5);

        log.info("[AI Mock] 채용공고 매칭 요청: topK={}", topK);

        List<AiMatchingDto.MatchedJobposting> matches = IntStream.range(0, topK)
                .mapToObj(i -> AiMatchingDto.MatchedJobposting.builder()
                        .jobpostingId(String.valueOf(300 + i))
                        .score(0.92 - (i * 0.07))
                        .title(MOCK_JOB_TITLES.get(i % MOCK_JOB_TITLES.size()))
                        .build())
                .toList();

        return AiMatchingDto.MatchJobpostingsResponse.builder()
                .matches(matches).totalCount(matches.size()).build();
    }

    /**
     * 스코어 계산 (회사용)
     * MSA: Ollama 임베딩 → 코사인 유사도 + 스킬 매칭 점수
     * Mock: 시뮬레이션 점수 반환
     */
    public AiMatchingDto.ScoreResponse scoreCandidate(AiMatchingDto.ScoreRequest request) {
        log.info("[AI Mock] 스코어 계산: candidateId={}", request.getCandidateId());

        List<String> requiredSkills = request.getRequiredSkills() != null
                ? request.getRequiredSkills()
                : List.of("Java", "Spring Boot");

        List<String> candidateSkills = List.of("Java", "Spring Boot", "JPA", "Docker");

        return AiMatchingDto.ScoreResponse.builder()
                .candidateId(request.getCandidateId())
                .requiredSkills(requiredSkills)
                .candidateSkills(candidateSkills)
                .cosineSimilarity(0.87)
                .scoreDetail(AiMatchingDto.ScoreDetail.builder()
                        .skillScore(85.0)
                        .similarityScore(87.0)
                        .bonusScore(5.0)
                        .totalScore(88.5)
                        .grade("A")
                        .build())
                .build();
    }

    /**
     * 스킬 갭 분석 (구직자용)
     * MSA: 후보자 스킬 vs JD 스킬 비교 + 코사인 유사도
     * Mock: 시뮬레이션 갭 분석 반환
     */
    public AiMatchingDto.SkillGapResponse analyzeSkillGap(AiMatchingDto.SkillGapRequest request) {
        log.info("[AI Mock] 스킬 갭 분석: candidateId={}, jobpostingId={}",
                request.getCandidateId(), request.getJobpostingId());

        List<String> candidateSkills = List.of("Java", "Spring Boot", "JPA", "Docker");
        List<String> requiredSkills = List.of("Java", "Spring Boot", "Kubernetes", "Kafka", "Redis");
        List<String> matched = List.of("Java", "Spring Boot");
        List<String> missing = List.of("Kubernetes", "Kafka", "Redis");

        return AiMatchingDto.SkillGapResponse.builder()
                .candidateId(request.getCandidateId())
                .jobpostingId(request.getJobpostingId())
                .candidateSkills(candidateSkills)
                .requiredSkills(requiredSkills)
                .matchedSkills(matched)
                .missingSkills(missing)
                .matchRate(0.4)
                .cosineSimilarity(0.72)
                .build();
    }
}
