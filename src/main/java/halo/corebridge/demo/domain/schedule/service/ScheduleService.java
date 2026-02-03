package halo.corebridge.demo.domain.schedule.service;

import halo.corebridge.demo.common.exception.BaseException;
import halo.corebridge.demo.common.response.BaseResponseStatus;
import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.notification.dto.NotificationDto;
import halo.corebridge.demo.domain.notification.enums.NotificationType;
import halo.corebridge.demo.domain.notification.service.NotificationService;
import halo.corebridge.demo.domain.schedule.dto.ScheduleDto;
import halo.corebridge.demo.domain.schedule.entity.Schedule;
import halo.corebridge.demo.domain.schedule.enums.ScheduleStatus;
import halo.corebridge.demo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final Snowflake snowflake;
    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Transactional
    public ScheduleDto.Response create(Long companyId, ScheduleDto.CreateRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        ScheduleDto.ConflictCheckResponse conflict = checkConflicts(
                request.getUserId(), request.getInterviewerId(),
                request.getStartTime(), request.getEndTime(), null);
        if (conflict.isHasConflict()) {
            throw new BaseException(BaseResponseStatus.SCHEDULE_CONFLICT);
        }

        Schedule schedule = Schedule.builder()
                .id(snowflake.nextId())
                .applyId(request.getApplyId()).jobpostingId(request.getJobpostingId())
                .userId(request.getUserId()).companyId(companyId)
                .type(request.getType()).title(request.getTitle())
                .description(request.getDescription()).location(request.getLocation())
                .startTime(request.getStartTime()).endTime(request.getEndTime())
                .interviewerId(request.getInterviewerId())
                .interviewerName(request.getInterviewerName())
                .status(ScheduleStatus.SCHEDULED).build();

        scheduleRepository.save(schedule);
        log.info("일정 생성: id={}, type={}", schedule.getId(), schedule.getType());

        sendScheduleNotification(schedule.getUserId(), "일정이 등록되었습니다: " + schedule.getTitle(), schedule.getId());
        return ScheduleDto.Response.from(schedule);
    }

    @Transactional(readOnly = true)
    public ScheduleDto.Response read(Long scheduleId) {
        return ScheduleDto.Response.from(findById(scheduleId));
    }

    @Transactional
    public ScheduleDto.Response update(Long scheduleId, Long companyId, ScheduleDto.UpdateRequest request) {
        Schedule schedule = findById(scheduleId);
        validateCompanyOwner(schedule, companyId);
        validateTimeRange(request.getStartTime(), request.getEndTime());

        ScheduleDto.ConflictCheckResponse conflict = checkConflicts(
                schedule.getUserId(), request.getInterviewerId(),
                request.getStartTime(), request.getEndTime(), scheduleId);
        if (conflict.isHasConflict()) {
            throw new BaseException(BaseResponseStatus.SCHEDULE_CONFLICT);
        }

        schedule.update(request.getTitle(), request.getDescription(), request.getLocation(),
                request.getStartTime(), request.getEndTime(),
                request.getInterviewerId(), request.getInterviewerName());
        log.info("일정 수정: id={}", scheduleId);

        sendScheduleNotification(schedule.getUserId(), "일정이 변경되었습니다: " + schedule.getTitle(), scheduleId);
        return ScheduleDto.Response.from(schedule);
    }

    @Transactional
    public ScheduleDto.Response updateStatus(Long scheduleId, Long companyId, ScheduleDto.UpdateStatusRequest request) {
        Schedule schedule = findById(scheduleId);
        validateCompanyOwner(schedule, companyId);
        schedule.updateStatus(request.getStatus());
        log.info("일정 상태 변경: id={}, status={}", scheduleId, request.getStatus());

        if (request.getStatus() == ScheduleStatus.CANCELLED) {
            sendScheduleNotification(schedule.getUserId(), "일정이 취소되었습니다: " + schedule.getTitle(), scheduleId);
        }
        return ScheduleDto.Response.from(schedule);
    }

    @Transactional
    public void delete(Long scheduleId, Long companyId) {
        Schedule schedule = findById(scheduleId);
        validateCompanyOwner(schedule, companyId);
        sendScheduleNotification(schedule.getUserId(), "일정이 삭제되었습니다: " + schedule.getTitle(), scheduleId);
        scheduleRepository.delete(schedule);
        log.info("일정 삭제: id={}", scheduleId);
    }

    @Transactional
    public ScheduleDto.Response updateMemo(Long scheduleId, Long companyId, ScheduleDto.UpdateMemoRequest request) {
        Schedule schedule = findById(scheduleId);
        validateCompanyOwner(schedule, companyId);
        schedule.updateMemo(request.getMemo());
        return ScheduleDto.Response.from(schedule);
    }

    // 지원자용
    @Transactional(readOnly = true)
    public ScheduleDto.ListResponse getMySchedules(Long userId) {
        List<ScheduleDto.Response> list = scheduleRepository.findByUserIdOrderByStartTimeAsc(userId).stream()
                .map(ScheduleDto.Response::from).toList();
        long upcoming = scheduleRepository.countByUserIdAndStatus(userId, ScheduleStatus.SCHEDULED);
        long completed = scheduleRepository.countByUserIdAndStatus(userId, ScheduleStatus.COMPLETED);
        return ScheduleDto.ListResponse.of(list, upcoming, completed);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto.CalendarEventResponse> getMyCalendarEvents(Long userId, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByUserIdAndDateRange(userId, start, end).stream()
                .map(ScheduleDto.CalendarEventResponse::from).toList();
    }

    // 기업용
    @Transactional(readOnly = true)
    public ScheduleDto.ListResponse getCompanySchedules(Long companyId) {
        List<ScheduleDto.Response> list = scheduleRepository.findByCompanyIdOrderByStartTimeAsc(companyId).stream()
                .map(ScheduleDto.Response::from).toList();
        long upcoming = scheduleRepository.countByCompanyIdAndStatus(companyId, ScheduleStatus.SCHEDULED);
        long completed = scheduleRepository.countByCompanyIdAndStatus(companyId, ScheduleStatus.COMPLETED);
        return ScheduleDto.ListResponse.of(list, upcoming, completed);
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto.CalendarEventResponse> getCompanyCalendarEvents(Long companyId, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByCompanyIdAndDateRange(companyId, start, end).stream()
                .map(ScheduleDto.CalendarEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto.Response> getSchedulesByJobposting(Long jobpostingId) {
        return scheduleRepository.findByJobpostingIdOrderByStartTimeAsc(jobpostingId).stream()
                .map(ScheduleDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto.Response> getSchedulesByApply(Long applyId) {
        return scheduleRepository.findByApplyIdOrderByStartTimeAsc(applyId).stream()
                .map(ScheduleDto.Response::from).toList();
    }

    // 충돌 체크
    @Transactional(readOnly = true)
    public ScheduleDto.ConflictCheckResponse checkConflicts(Long userId, Long interviewerId,
                                                            LocalDateTime start, LocalDateTime end, Long excludeId) {
        List<ScheduleDto.ConflictCheckResponse.ConflictDetail> conflicts = new ArrayList<>();

        scheduleRepository.findApplicantConflicts(userId, start, end, excludeId).forEach(c ->
                conflicts.add(ScheduleDto.ConflictCheckResponse.ConflictDetail.builder()
                        .type("APPLICANT").scheduleId(c.getId()).title(c.getTitle())
                        .startTime(c.getStartTime()).endTime(c.getEndTime())
                        .message("지원자가 해당 시간에 다른 일정이 있습니다").build()));

        if (interviewerId != null) {
            scheduleRepository.findInterviewerConflicts(interviewerId, start, end, excludeId).forEach(c ->
                    conflicts.add(ScheduleDto.ConflictCheckResponse.ConflictDetail.builder()
                            .type("INTERVIEWER").scheduleId(c.getId()).title(c.getTitle())
                            .startTime(c.getStartTime()).endTime(c.getEndTime())
                            .message("면접관이 해당 시간에 다른 일정이 있습니다").build()));
        }

        return ScheduleDto.ConflictCheckResponse.builder()
                .hasConflict(!conflicts.isEmpty()).conflicts(conflicts).build();
    }

    private Schedule findById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.SCHEDULE_NOT_FOUND));
    }

    private void validateCompanyOwner(Schedule schedule, Long companyId) {
        if (!schedule.getCompanyId().equals(companyId)) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new BaseException(BaseResponseStatus.INVALID_TIME_RANGE);
        }
    }

    private void sendScheduleNotification(Long userId, String message, Long scheduleId) {
        try {
            notificationService.create(NotificationDto.CreateRequest.builder()
                    .userId(userId).type(NotificationType.SCHEDULE)
                    .title("일정 알림").message(message)
                    .link("/schedules/" + scheduleId)
                    .relatedId(scheduleId).relatedType("SCHEDULE").build());
        } catch (Exception e) {
            // 알림 실패 무시
        }
    }
}
