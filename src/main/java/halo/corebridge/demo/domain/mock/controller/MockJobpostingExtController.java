package halo.corebridge.demo.domain.mock.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.comment.repository.CommentRepository;
import halo.corebridge.demo.domain.jobposting.entity.Jobposting;
import halo.corebridge.demo.domain.jobposting.repository.JobpostingRepository;
import halo.corebridge.demo.domain.user.entity.User;
import halo.corebridge.demo.domain.user.repository.UserRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 채용공고 확장 기능 Mock Controller
 *
 * MSA에서는 다음 4개 서비스로 분리:
 *   - jobposting-read: BFF 패턴으로 공고+통계+닉네임 통합 조회
 *   - jobposting-view: Redis 기반 조회수 카운팅
 *   - jobposting-like: Redis 기반 좋아요 토글
 *   - jobposting-hot:  CQRS + Batch로 인기 공고 집계
 *
 * 데모에서는 인메모리 Map으로 시뮬레이션합니다.
 */
@RestController
@RequiredArgsConstructor
public class MockJobpostingExtController {

    private final JobpostingRepository jobpostingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    // 인메모리 카운터
    private final Map<Long, Long> viewCounts = new ConcurrentHashMap<>();
    private final Map<Long, Long> likeCounts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userLikes = new ConcurrentHashMap<>(); // "userId:jobpostingId" → liked

    /** DataInitializer에서 초기 조회수 세팅 */
    public void setViewCount(Long jobpostingId, long count) {
        viewCounts.put(jobpostingId, count);
    }

    /** DataInitializer에서 초기 좋아요 세팅 */
    public void setLikeCount(Long jobpostingId, long count) {
        likeCounts.put(jobpostingId, count);
    }

    // ============================================
    // Read Service (BFF 패턴 Mock)
    // ============================================

    /** 단일 공고 통합 조회 (공고 + 닉네임 + 조회수/좋아요/댓글 수) */
    @GetMapping("/api/v1/jobposting-read/{jobpostingId}")
    public BaseResponse<ReadResponse> getReadById(@PathVariable Long jobpostingId) {
        Jobposting jp = jobpostingRepository.findById(jobpostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        return BaseResponse.success(toReadResponse(jp));
    }

    /** 공고 목록 통합 조회 */
    @GetMapping("/api/v1/jobposting-read")
    public BaseResponse<Map<String, Object>> getReadList(
            @RequestParam(defaultValue = "1") Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // page는 1-based로 들어올 수 있음
        int zeroPage = Math.max(0, page - 1);
        Page<Jobposting> result = (boardId == null || boardId == 1L)
                ? jobpostingRepository.findAllByOrderByJobpostingIdDesc(PageRequest.of(zeroPage, pageSize))
                : jobpostingRepository.findByBoardIdOrderByJobpostingIdDesc(boardId, PageRequest.of(zeroPage, pageSize));

        List<ReadResponse> list = result.getContent().stream().map(this::toReadResponse).toList();
        return BaseResponse.success(Map.of(
                "jobpostings", list,
                "jobpostingCount", result.getTotalElements()));
    }

    // ============================================
    // View Service (조회수 Mock)
    // ============================================

    /** 조회수 증가 */
    @PostMapping("/api/v1/jobposting-views/jobpostings/{jobpostingId}")
    public BaseResponse<Long> increaseViewCount(@PathVariable Long jobpostingId) {
        long count = viewCounts.merge(jobpostingId, 1L, Long::sum);
        return BaseResponse.success(count);
    }

    /** 조회수 조회 */
    @GetMapping("/api/v1/jobposting-views/jobpostings/{jobpostingId}/count")
    public BaseResponse<Long> getViewCount(@PathVariable Long jobpostingId) {
        return BaseResponse.success(viewCounts.getOrDefault(jobpostingId, 0L));
    }

    // ============================================
    // Like Service (좋아요 Mock)
    // ============================================

    /** 좋아요 상태 조회 */
    @GetMapping("/api/v1/jobposting-likes/jobpostings/{jobpostingId}")
    public BaseResponse<Map<String, Object>> getLikeStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long jobpostingId) {
        String key = userId + ":" + jobpostingId;
        boolean liked = userLikes.getOrDefault(key, false);
        long count = likeCounts.getOrDefault(jobpostingId, 0L);
        return BaseResponse.success(Map.of(
                "jobpostingId", jobpostingId,
                "userId", userId,
                "liked", liked,
                "likeCount", count));
    }

    /** 좋아요 수 조회 */
    @GetMapping("/api/v1/jobposting-likes/jobpostings/{jobpostingId}/count")
    public BaseResponse<Long> getLikeCount(@PathVariable Long jobpostingId) {
        return BaseResponse.success(likeCounts.getOrDefault(jobpostingId, 0L));
    }

    /** 좋아요 */
    @PostMapping("/api/v1/jobposting-likes/jobpostings/{jobpostingId}")
    public BaseResponse<Void> like(@AuthenticationPrincipal Long userId,
                                   @PathVariable Long jobpostingId) {
        String key = userId + ":" + jobpostingId;
        if (!userLikes.getOrDefault(key, false)) {
            userLikes.put(key, true);
            likeCounts.merge(jobpostingId, 1L, Long::sum);
        }
        return BaseResponse.success();
    }

    /** 좋아요 취소 */
    @DeleteMapping("/api/v1/jobposting-likes/jobpostings/{jobpostingId}")
    public BaseResponse<Void> unlike(@AuthenticationPrincipal Long userId,
                                     @PathVariable Long jobpostingId) {
        String key = userId + ":" + jobpostingId;
        if (userLikes.getOrDefault(key, false)) {
            userLikes.put(key, false);
            likeCounts.merge(jobpostingId, 0L, (old, v) -> Math.max(0, old - 1));
        }
        return BaseResponse.success();
    }

    // ============================================
    // Hot Service (인기 공고 Mock)
    // ============================================

    /** 오늘의 인기 공고 */
    @GetMapping("/api/v1/hot-jobpostings/today")
    public BaseResponse<List<Map<String, Object>>> getHotToday() {
        return BaseResponse.success(buildHotList());
    }

    /** 특정 날짜 인기 공고 */
    @GetMapping("/api/v1/hot-jobpostings/date/{dateStr}")
    public BaseResponse<List<Map<String, Object>>> getHotByDate(@PathVariable String dateStr) {
        return BaseResponse.success(buildHotList());
    }

    // ============================================
    // 헬퍼 메서드
    // ============================================

    private ReadResponse toReadResponse(Jobposting jp) {
        String nickname = userRepository.findById(jp.getUserId())
                .map(User::getNickname).orElse("알 수 없음");

        long views = viewCounts.getOrDefault(jp.getJobpostingId(), 0L);
        long likes = likeCounts.getOrDefault(jp.getJobpostingId(), 0L);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return ReadResponse.builder()
                .jobpostingId(jp.getJobpostingId())
                .title(jp.getTitle())
                .content(jp.getContent())
                .boardId(jp.getBoardId())
                .userId(jp.getUserId())
                .nickname(nickname)
                .requiredSkills(parseSkills(jp.getRequiredSkills()))
                .preferredSkills(parseSkills(jp.getPreferredSkills()))
                .viewCount(views)
                .likeCount(likes)
                .commentCount(commentRepository.countByJobpostingId(jp.getJobpostingId()).intValue())
                .createdAt(jp.getCreatedAt() != null ? jp.getCreatedAt().format(fmt) : null)
                .updatedAt(jp.getUpdatedAt() != null ? jp.getUpdatedAt().format(fmt) : null)
                .build();
    }

    private List<String> parseSkills(String skills) {
        if (skills == null || skills.isBlank()) return List.of();
        return Arrays.stream(skills.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }



    private List<Map<String, Object>> buildHotList() {
        List<Jobposting> all = jobpostingRepository.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Jobposting jp : all) {
            long views = viewCounts.getOrDefault(jp.getJobpostingId(), 0L);
            long likes = likeCounts.getOrDefault(jp.getJobpostingId(), 0L);
            int comments = commentRepository.countByJobpostingId(jp.getJobpostingId()).intValue();
            double score = views * 0.3 + likes * 2.0 + comments * 3.0;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("jobpostingId", jp.getJobpostingId());
            m.put("title", jp.getTitle());
            m.put("boardId", jp.getBoardId());
            m.put("likeCount", likes);
            m.put("commentCount", comments);
            m.put("viewCount", views);
            m.put("score", Math.round(score * 10) / 10.0);
            list.add(m);
        }
        list.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return list;
    }

    @Getter @Builder
    static class ReadResponse {
        private Long jobpostingId;
        private String title;
        private String content;
        private Long boardId;
        private Long userId;
        private String nickname;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private long viewCount;
        private long likeCount;
        private int commentCount;
        private String createdAt;
        private String updatedAt;
    }
}
