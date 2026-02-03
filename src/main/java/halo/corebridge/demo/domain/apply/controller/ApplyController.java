package halo.corebridge.demo.domain.apply.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.apply.dto.ApplyDto;
import halo.corebridge.demo.domain.apply.dto.ProcessDto;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import halo.corebridge.demo.domain.apply.service.ApplyService;
import halo.corebridge.demo.domain.apply.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applies")
public class ApplyController {

    private final ApplyService applyService;
    private final ProcessService processService;

    // ============================================
    // 지원자 API
    // ============================================

    @PostMapping
    public BaseResponse<ApplyDto.ApplyDetailResponse> apply(@RequestBody ApplyDto.CreateRequest request) {
        return BaseResponse.success(applyService.apply(request));
    }

    @DeleteMapping("/{applyId}/users/{userId}")
    public BaseResponse<Void> cancel(@PathVariable Long applyId, @PathVariable Long userId) {
        applyService.cancel(applyId, userId);
        return BaseResponse.success();
    }

    @GetMapping("/users/{userId}")
    public BaseResponse<ApplyDto.ApplyPageResponse> getMyApplies(@PathVariable Long userId) {
        return BaseResponse.success(applyService.getMyApplies(userId));
    }

    @GetMapping("/{applyId}")
    public BaseResponse<ApplyDto.ApplyDetailResponse> read(@PathVariable Long applyId) {
        return BaseResponse.success(applyService.read(applyId));
    }

    // ============================================
    // 기업 API
    // ============================================

    @GetMapping("/jobpostings/{jobpostingId}")
    public BaseResponse<ApplyDto.ApplyPageResponse> getAppliesByJobposting(@PathVariable Long jobpostingId) {
        return BaseResponse.success(applyService.getAppliesByJobposting(jobpostingId));
    }

    @GetMapping("/jobpostings/{jobpostingId}/steps/{step}")
    public BaseResponse<ApplyDto.ApplyPageResponse> getAppliesByStep(
            @PathVariable Long jobpostingId, @PathVariable ProcessStep step) {
        return BaseResponse.success(applyService.getAppliesByStep(jobpostingId, step));
    }

    @PatchMapping("/{applyId}/memo")
    public BaseResponse<ApplyDto.ApplyDetailResponse> updateMemo(
            @PathVariable Long applyId, @RequestBody ApplyDto.UpdateMemoRequest request) {
        return BaseResponse.success(applyService.updateMemo(applyId, request));
    }

    @GetMapping("/{applyId}/history")
    public BaseResponse<List<ProcessDto.HistoryResponse>> getHistory(@PathVariable Long applyId) {
        return BaseResponse.success(processService.getHistoryByApplyId(applyId));
    }

    // ============================================
    // 통계
    // ============================================

    @GetMapping("/users/{userId}/stats")
    public BaseResponse<ProcessDto.UserStatsResponse> getUserStats(@PathVariable Long userId) {
        return BaseResponse.success(processService.getUserStats(userId));
    }

    @GetMapping("/jobpostings/{jobpostingId}/stats")
    public BaseResponse<ProcessDto.CompanyStatsResponse> getJobpostingStats(@PathVariable Long jobpostingId) {
        return BaseResponse.success(processService.getJobpostingStats(jobpostingId));
    }
}
