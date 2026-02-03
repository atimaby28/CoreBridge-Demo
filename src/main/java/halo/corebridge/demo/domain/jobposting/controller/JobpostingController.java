package halo.corebridge.demo.domain.jobposting.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.jobposting.dto.JobpostingDto;
import halo.corebridge.demo.domain.jobposting.service.JobpostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobpostings")
public class JobpostingController {

    private final JobpostingService jobpostingService;

    // ============================================
    // 조회 API (Public)
    // ============================================

    @GetMapping("/{jobpostingId}")
    public BaseResponse<JobpostingDto.JobpostingResponse> read(@PathVariable Long jobpostingId) {
        return BaseResponse.success(jobpostingService.read(jobpostingId));
    }

    @GetMapping
    public BaseResponse<JobpostingDto.JobpostingPageResponse> readAll(
            @RequestParam(value = "boardId", defaultValue = "1") Long boardId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return BaseResponse.success(jobpostingService.readAll(boardId, page, size));
    }

    @GetMapping("/writers/{writerId}")
    public BaseResponse<JobpostingDto.JobpostingListResponse> readByWriter(@PathVariable Long writerId) {
        return BaseResponse.success(jobpostingService.readByWriter(writerId));
    }

    // ============================================
    // CUD API (Authenticated)
    // ============================================

    @PostMapping
    public BaseResponse<JobpostingDto.JobpostingResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody JobpostingDto.CreateRequest request) {
        return BaseResponse.success(jobpostingService.create(userId, request));
    }

    @PutMapping("/{jobpostingId}")
    public BaseResponse<JobpostingDto.JobpostingResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long jobpostingId,
            @Valid @RequestBody JobpostingDto.UpdateRequest request) {
        return BaseResponse.success(jobpostingService.update(userId, jobpostingId, request));
    }

    @DeleteMapping("/{jobpostingId}")
    public BaseResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long jobpostingId) {
        jobpostingService.delete(userId, jobpostingId);
        return BaseResponse.success();
    }

    @GetMapping("/me")
    public BaseResponse<JobpostingDto.JobpostingListResponse> getMyJobpostings(
            @AuthenticationPrincipal Long userId) {
        return BaseResponse.success(jobpostingService.readByWriter(userId));
    }
}
