package halo.corebridge.demo.domain.mock.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Audit Log Mock Controller
 *
 * MSA에서는 admin-audit 서비스가 Kafka로 수집한 감사 로그를 제공하지만,
 * 데모에서는 시뮬레이션 데이터를 반환합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/audits")
public class MockAuditController {

    private static final List<MockAudit> MOCK_AUDITS = generateMockAudits();

    /** 페이징 조회 */
    @GetMapping
    public BaseResponse<Map<String, Object>> getAuditsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int start = page * size;
        int end = Math.min(start + size, MOCK_AUDITS.size());
        List<MockAudit> content = start < MOCK_AUDITS.size()
                ? MOCK_AUDITS.subList(start, end) : List.of();
        return BaseResponse.success(Map.of(
                "audits", content,
                "totalCount", MOCK_AUDITS.size(),
                "page", page, "size", size,
                "totalPages", (MOCK_AUDITS.size() + size - 1) / size,
                "hasNext", end < MOCK_AUDITS.size(),
                "hasPrevious", page > 0));
    }

    /** 최근 로그 */
    @GetMapping("/recent")
    public BaseResponse<Map<String, Object>> getRecentAudits(
            @RequestParam(defaultValue = "100") int size) {
        List<MockAudit> content = MOCK_AUDITS.subList(0, Math.min(size, MOCK_AUDITS.size()));
        return BaseResponse.success(Map.of(
                "audits", content, "totalCount", content.size(),
                "page", 0, "size", size, "totalPages", 1,
                "hasNext", false, "hasPrevious", false));
    }

    /** 상세 조회 */
    @GetMapping("/{auditId}")
    public BaseResponse<MockAudit> getAuditById(@PathVariable Long auditId) {
        return BaseResponse.success(MOCK_AUDITS.stream()
                .filter(a -> a.getAuditId().equals(auditId)).findFirst()
                .orElse(MOCK_AUDITS.get(0)));
    }

    /** 사용자별 조회 */
    @GetMapping("/users/{userId}")
    public BaseResponse<List<MockAudit>> getByUser(@PathVariable Long userId) {
        return BaseResponse.success(MOCK_AUDITS.stream()
                .filter(a -> userId.equals(a.getUserId())).limit(20).toList());
    }

    /** 서비스별 조회 */
    @GetMapping("/services/{serviceName}")
    public BaseResponse<List<MockAudit>> getByService(@PathVariable String serviceName) {
        return BaseResponse.success(MOCK_AUDITS.stream()
                .filter(a -> serviceName.equals(a.getServiceName())).limit(20).toList());
    }

    /** 이벤트 타입별 조회 */
    @GetMapping("/events/{eventType}")
    public BaseResponse<List<MockAudit>> getByEventType(@PathVariable String eventType) {
        return BaseResponse.success(MOCK_AUDITS.stream()
                .filter(a -> eventType.equals(a.getEventType())).limit(20).toList());
    }

    /** 기간별 조회 */
    @GetMapping("/range")
    public BaseResponse<List<MockAudit>> getByDateRange(
            @RequestParam String startDate, @RequestParam String endDate) {
        return BaseResponse.success(MOCK_AUDITS.subList(0, Math.min(50, MOCK_AUDITS.size())));
    }

    /** 에러 로그 */
    @GetMapping("/errors")
    public BaseResponse<List<MockAudit>> getErrorAudits(
            @RequestParam(defaultValue = "50") int size) {
        return BaseResponse.success(MOCK_AUDITS.stream()
                .filter(a -> a.getHttpStatus() >= 400).limit(size).toList());
    }

    /** 통계 */
    @GetMapping("/stats")
    public BaseResponse<Map<String, Object>> getAuditStats() {
        long errorCount = MOCK_AUDITS.stream().filter(a -> a.getHttpStatus() >= 400).count();
        double avgTime = MOCK_AUDITS.stream().mapToLong(MockAudit::getExecutionTime).average().orElse(0);
        long uniqueUsers = MOCK_AUDITS.stream().map(MockAudit::getUserId).filter(java.util.Objects::nonNull).distinct().count();

        return BaseResponse.success(Map.of(
                "totalRequests", MOCK_AUDITS.size(),
                "errorCount", errorCount,
                "uniqueUsers", uniqueUsers,
                "avgExecutionTime", Math.round(avgTime * 10.0) / 10.0,
                "mostActiveService", "jobposting-service",
                "mostFrequentEvent", "JOBPOSTING_READ"));
    }

    // ============================================
    // Mock Data
    // ============================================

    @Getter @Builder
    static class MockAudit {
        private Long auditId;
        private Long userId;
        private String userEmail;
        private String serviceName;
        private String eventType;
        private String eventTypeName;
        private String httpMethod;
        private String requestUri;
        private String clientIp;
        private String userAgent;
        private int httpStatus;
        private long executionTime;
        private String requestBody;
        private String errorMessage;
        private String createdAt;
    }

    private static List<MockAudit> generateMockAudits() {
        String[] services = {"user-service", "jobposting-service", "apply-service",
                "resume-service", "notification-service", "schedule-service", "ai-service"};
        String[] events = {"LOGIN", "JOBPOSTING_READ", "APPLICATION_CREATE",
                "RESUME_UPDATE", "API_REQUEST", "JOBPOSTING_CREATE", "SCHEDULE_CREATE",
                "LOGIN_FAILED", "RESUME_CREATE", "APPLICATION_STATUS_CHANGE"};
        String[] eventNames = {"로그인", "공고 조회", "지원", "이력서 수정",
                "API 요청", "공고 등록", "일정 등록",
                "로그인 실패", "이력서 등록", "지원 상태 변경"};
        String[] methods = {"POST", "GET", "POST", "PUT", "GET", "POST", "POST",
                "POST", "POST", "PATCH"};
        String[] uris = {"/api/v1/users/login", "/api/v1/jobpostings/1",
                "/api/v1/applies", "/api/v1/resumes/me",
                "/api/v1/notifications/unread-count",
                "/api/v1/jobpostings", "/api/v1/schedules",
                "/api/v1/users/login", "/api/v1/resumes", "/api/v1/processes/1/transition"};

        // 7명 사용자 (null = 미인증 요청)
        String[] emails = {null, "user@demo.com", "company@demo.com", "admin@demo.com",
                "kim@demo.com", "lee@demo.com", "park@demo.com", "choi@demo.com"};
        String[] nicknames = {null, "양승우", "테크컴퍼니", "관리자",
                "김민수", "이서연", "박지훈", "최유진"};
        String[] ips = {"192.168.1.10", "10.0.0.15", "172.16.0.5", "192.168.0.1",
                "10.0.1.22", "10.0.1.33", "172.16.1.8", "10.0.2.44"};

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return IntStream.range(0, 150).mapToObj(i -> {
            int idx = i % events.length;
            int userIdx = i % emails.length;

            // 상태: 대부분 200, 일부 에러
            int status;
            String errorMsg = null;
            if (i % 30 == 0) { status = 500; errorMsg = "Internal Server Error: DB connection timeout"; }
            else if (i % 20 == 0) { status = 401; errorMsg = "Unauthorized: Invalid or expired token"; }
            else if (i % 15 == 0) { status = 400; errorMsg = "Bad Request: Validation failed"; }
            else if (i % 25 == 0) { status = 404; errorMsg = "Not Found: Resource does not exist"; }
            else if (idx == 7) { status = 401; errorMsg = "Login failed: Invalid credentials"; }
            else status = (idx == 2 || idx == 5 || idx == 8) ? 201 : 200;

            // 응답시간: 일반 5~50ms, 에러 200~500ms, AI 100~300ms
            long execTime = (status >= 400) ? 200 + (i * 7L) % 300
                    : services[i % services.length].equals("ai-service") ? 100 + (i * 11L) % 200
                    : 5 + (i * 3L) % 50;

            return MockAudit.builder()
                    .auditId((long) (10000 + i))
                    .userId(userIdx == 0 ? null : (long) userIdx)
                    .userEmail(emails[userIdx])
                    .serviceName(services[i % services.length])
                    .eventType(events[idx])
                    .eventTypeName(eventNames[idx])
                    .httpMethod(methods[idx])
                    .requestUri(uris[idx])
                    .clientIp(ips[userIdx])
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .httpStatus(status)
                    .executionTime(execTime)
                    .requestBody(status >= 400 ? null : "{}")
                    .errorMessage(errorMsg)
                    .createdAt(now.minusMinutes(i * 3L).format(fmt))
                    .build();
        }).toList();
    }
}
