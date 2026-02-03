package halo.corebridge.demo.domain.apply.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.apply.dto.ProcessDto;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import halo.corebridge.demo.domain.apply.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채용 프로세스 API (State Machine)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/processes")
public class ProcessController {

    private final ProcessService processService;

    // ============================================
    // 상태 전이 (State Machine 핵심)
    // ============================================

    @PatchMapping("/{processId}/transition")
    public BaseResponse<ProcessDto.ProcessResponse> transition(
            @PathVariable Long processId, @RequestBody ProcessDto.TransitionRequest request) {
        return BaseResponse.success(processService.transition(processId, request));
    }

    @PatchMapping("/applies/{applyId}/transition")
    public BaseResponse<ProcessDto.ProcessResponse> transitionByApply(
            @PathVariable Long applyId, @RequestBody ProcessDto.TransitionRequest request) {
        return BaseResponse.success(processService.transitionByApplyId(applyId, request));
    }

    // ============================================
    // 조회
    // ============================================

    @GetMapping("/{processId}")
    public BaseResponse<ProcessDto.ProcessResponse> read(@PathVariable Long processId) {
        return BaseResponse.success(processService.read(processId));
    }

    @GetMapping("/applies/{applyId}")
    public BaseResponse<ProcessDto.ProcessResponse> readByApply(@PathVariable Long applyId) {
        return BaseResponse.success(processService.readByApplyId(applyId));
    }

    @GetMapping("/jobpostings/{jobpostingId}")
    public BaseResponse<ProcessDto.ProcessPageResponse> getByJobposting(@PathVariable Long jobpostingId) {
        return BaseResponse.success(processService.getByJobposting(jobpostingId));
    }

    @GetMapping("/jobpostings/{jobpostingId}/steps/{step}")
    public BaseResponse<ProcessDto.ProcessPageResponse> getByJobpostingAndStep(
            @PathVariable Long jobpostingId, @PathVariable ProcessStep step) {
        return BaseResponse.success(processService.getByJobpostingAndStep(jobpostingId, step));
    }

    @GetMapping("/users/{userId}")
    public BaseResponse<ProcessDto.ProcessPageResponse> getByUser(@PathVariable Long userId) {
        return BaseResponse.success(processService.getByUser(userId));
    }

    // ============================================
    // 이력
    // ============================================

    @GetMapping("/{processId}/history")
    public BaseResponse<List<ProcessDto.HistoryResponse>> getHistory(@PathVariable Long processId) {
        return BaseResponse.success(processService.getHistory(processId));
    }

    @GetMapping("/applies/{applyId}/history")
    public BaseResponse<List<ProcessDto.HistoryResponse>> getHistoryByApply(@PathVariable Long applyId) {
        return BaseResponse.success(processService.getHistoryByApplyId(applyId));
    }

    // ============================================
    // 메타
    // ============================================

    @GetMapping("/steps")
    public BaseResponse<List<ProcessDto.StepInfoResponse>> getAllSteps() {
        return BaseResponse.success(processService.getAllSteps());
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

    @PostMapping("/company/stats")
    public BaseResponse<ProcessDto.CompanyStatsResponse> getCompanyStats(@RequestBody List<Long> jobpostingIds) {
        return BaseResponse.success(processService.getCompanyStats(jobpostingIds));
    }
}
