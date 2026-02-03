package halo.corebridge.demo.domain.ai.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.ai.dto.AiMatchingDto;
import halo.corebridge.demo.domain.ai.service.AiMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI 매칭 API (Mock)
 *
 * MSA에서는 FastAPI + Ollama + Redis Vector Search로 구현되어 있으며,
 * 데모에서는 Mock 데이터를 즉시 반환합니다.
 *
 * API 경로는 MSA 원본과 동일하게 유지하여
 * 프론트엔드 호환성을 보장합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai-matching")
@RequiredArgsConstructor
public class AiMatchingController {

    private final AiMatchingService aiMatchingService;

    /** 후보자 매칭 (회사용) */
    @PostMapping("/match")
    public BaseResponse<AiMatchingDto.MatchCandidatesResponse> matchCandidates(
            @RequestBody AiMatchingDto.MatchCandidatesRequest request) {
        return BaseResponse.success(aiMatchingService.matchCandidates(request));
    }

    /** 채용공고 매칭 (구직자용) */
    @PostMapping("/match-jobpostings")
    public BaseResponse<AiMatchingDto.MatchJobpostingsResponse> matchJobpostings(
            @RequestBody AiMatchingDto.MatchJobpostingsRequest request) {
        return BaseResponse.success(aiMatchingService.matchJobpostings(request));
    }

    /** 스코어 계산 (회사용) */
    @PostMapping("/score")
    public BaseResponse<AiMatchingDto.ScoreResponse> scoreCandidate(
            @RequestBody AiMatchingDto.ScoreRequest request) {
        return BaseResponse.success(aiMatchingService.scoreCandidate(request));
    }

    /** 스킬 갭 분석 (구직자용) */
    @PostMapping("/skill-gap")
    public BaseResponse<AiMatchingDto.SkillGapResponse> analyzeSkillGap(
            @RequestBody AiMatchingDto.SkillGapRequest request) {
        return BaseResponse.success(aiMatchingService.analyzeSkillGap(request));
    }
}
