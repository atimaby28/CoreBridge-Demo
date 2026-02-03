package halo.corebridge.demo.domain.schedule.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.schedule.dto.ScheduleDto;
import halo.corebridge.demo.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ============================================
    // 공통
    // ============================================

    @GetMapping("/{scheduleId}")
    public BaseResponse<ScheduleDto.Response> getSchedule(@PathVariable Long scheduleId) {
        return BaseResponse.success(scheduleService.read(scheduleId));
    }

    // ============================================
    // 지원자
    // ============================================

    @GetMapping("/my")
    public BaseResponse<ScheduleDto.ListResponse> getMySchedules(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(scheduleService.getMySchedules(userId));
    }

    @GetMapping("/my/calendar")
    public BaseResponse<List<ScheduleDto.CalendarEventResponse>> getMyCalendarEvents(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return BaseResponse.success(scheduleService.getMyCalendarEvents(userId, start, end));
    }

    // ============================================
    // 기업
    // ============================================

    @PostMapping
    public BaseResponse<ScheduleDto.Response> createSchedule(
            @AuthenticationPrincipal Long companyId,
            @Valid @RequestBody ScheduleDto.CreateRequest request) {
        return BaseResponse.success(scheduleService.create(companyId, request));
    }

    @PutMapping("/{scheduleId}")
    public BaseResponse<ScheduleDto.Response> updateSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal Long companyId,
            @Valid @RequestBody ScheduleDto.UpdateRequest request) {
        return BaseResponse.success(scheduleService.update(scheduleId, companyId, request));
    }

    @PatchMapping("/{scheduleId}/status")
    public BaseResponse<ScheduleDto.Response> updateStatus(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal Long companyId,
            @Valid @RequestBody ScheduleDto.UpdateStatusRequest request) {
        return BaseResponse.success(scheduleService.updateStatus(scheduleId, companyId, request));
    }

    @DeleteMapping("/{scheduleId}")
    public BaseResponse<Void> deleteSchedule(
            @PathVariable Long scheduleId, @AuthenticationPrincipal Long companyId) {
        scheduleService.delete(scheduleId, companyId);
        return BaseResponse.success(null);
    }

    @PatchMapping("/{scheduleId}/memo")
    public BaseResponse<ScheduleDto.Response> updateMemo(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal Long companyId,
            @RequestBody ScheduleDto.UpdateMemoRequest request) {
        return BaseResponse.success(scheduleService.updateMemo(scheduleId, companyId, request));
    }

    @GetMapping("/company")
    public BaseResponse<ScheduleDto.ListResponse> getCompanySchedules(@AuthenticationPrincipal Long companyId) {
        return BaseResponse.success(scheduleService.getCompanySchedules(companyId));
    }

    @GetMapping("/company/calendar")
    public BaseResponse<List<ScheduleDto.CalendarEventResponse>> getCompanyCalendarEvents(
            @AuthenticationPrincipal Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return BaseResponse.success(scheduleService.getCompanyCalendarEvents(companyId, start, end));
    }

    @GetMapping("/jobposting/{jobpostingId}")
    public BaseResponse<List<ScheduleDto.Response>> getByJobposting(@PathVariable Long jobpostingId) {
        return BaseResponse.success(scheduleService.getSchedulesByJobposting(jobpostingId));
    }

    @GetMapping("/apply/{applyId}")
    public BaseResponse<List<ScheduleDto.Response>> getByApply(@PathVariable Long applyId) {
        return BaseResponse.success(scheduleService.getSchedulesByApply(applyId));
    }

    @GetMapping("/check-conflict")
    public BaseResponse<ScheduleDto.ConflictCheckResponse> checkConflict(
            @RequestParam Long userId,
            @RequestParam(required = false) Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeScheduleId) {
        return BaseResponse.success(
                scheduleService.checkConflicts(userId, interviewerId, startTime, endTime, excludeScheduleId));
    }
}
