package halo.corebridge.demo.domain.resume.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.resume.dto.ResumeDto;
import halo.corebridge.demo.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/me")
    public BaseResponse<ResumeDto.ResumeResponse> getMyResume(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(resumeService.getOrCreate(userId));
    }

    @PutMapping("/me")
    public BaseResponse<ResumeDto.ResumeResponse> updateMyResume(
            @AuthenticationPrincipal Long userId, @RequestBody ResumeDto.UpdateRequest request) {
        return BaseResponse.success(resumeService.update(userId, request));
    }

    @GetMapping("/me/versions")
    public BaseResponse<ResumeDto.VersionListResponse> getVersions(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(resumeService.getVersions(userId));
    }

    @GetMapping("/me/versions/{version}")
    public BaseResponse<ResumeDto.VersionResponse> getVersion(
            @AuthenticationPrincipal Long userId, @PathVariable int version) {
        return BaseResponse.success(resumeService.getVersion(userId, version));
    }

    @PostMapping("/me/versions/{version}/restore")
    public BaseResponse<ResumeDto.ResumeResponse> restoreVersion(
            @AuthenticationPrincipal Long userId, @PathVariable int version) {
        return BaseResponse.success(resumeService.restoreVersion(userId, version));
    }

    @PostMapping("/me/analyze")
    public BaseResponse<ResumeDto.ResumeResponse> requestAnalysis(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(resumeService.requestAnalysis(userId));
    }

    /** AI 분석 결과 콜백 (내부용) */
    @PostMapping("/{resumeId}/ai-result")
    public BaseResponse<ResumeDto.ResumeResponse> updateAiResult(
            @PathVariable Long resumeId, @RequestBody ResumeDto.AiResultRequest request) {
        return BaseResponse.success(resumeService.updateAiResult(resumeId, request));
    }

    /** userId로 이력서 조회 */
    @GetMapping("/by-user/{userId}")
    public BaseResponse<ResumeDto.ResumeResponse> getByUserId(@PathVariable Long userId) {
        return BaseResponse.success(resumeService.getOrCreate(userId));
    }
}
