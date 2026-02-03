package halo.corebridge.demo.domain.schedule.repository;

import halo.corebridge.demo.domain.schedule.entity.Schedule;
import halo.corebridge.demo.domain.schedule.enums.ScheduleStatus;
import halo.corebridge.demo.domain.schedule.enums.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 지원자용
    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    List<Schedule> findByUserIdAndStatusOrderByStartTimeAsc(Long userId, ScheduleStatus status);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId " +
            "AND s.startTime >= :start AND s.endTime <= :end " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 기업용
    List<Schedule> findByCompanyIdOrderByStartTimeAsc(Long companyId);

    @Query("SELECT s FROM Schedule s WHERE s.companyId = :companyId " +
            "AND s.startTime >= :start AND s.endTime <= :end " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findByCompanyIdAndDateRange(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Schedule> findByJobpostingIdOrderByStartTimeAsc(Long jobpostingId);

    List<Schedule> findByApplyIdOrderByStartTimeAsc(Long applyId);

    // 충돌 체크
    @Query("SELECT s FROM Schedule s WHERE s.interviewerId = :interviewerId " +
            "AND s.status IN (halo.corebridge.demo.domain.schedule.enums.ScheduleStatus.SCHEDULED, " +
            "halo.corebridge.demo.domain.schedule.enums.ScheduleStatus.IN_PROGRESS) " +
            "AND NOT (s.endTime <= :newStart OR s.startTime >= :newEnd) " +
            "AND (:excludeId IS NULL OR s.id != :excludeId)")
    List<Schedule> findInterviewerConflicts(
            @Param("interviewerId") Long interviewerId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeId") Long excludeId);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId " +
            "AND s.status IN (halo.corebridge.demo.domain.schedule.enums.ScheduleStatus.SCHEDULED, " +
            "halo.corebridge.demo.domain.schedule.enums.ScheduleStatus.IN_PROGRESS) " +
            "AND NOT (s.endTime <= :newStart OR s.startTime >= :newEnd) " +
            "AND (:excludeId IS NULL OR s.id != :excludeId)")
    List<Schedule> findApplicantConflicts(
            @Param("userId") Long userId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeId") Long excludeId);

    // 통계
    long countByUserIdAndStatus(Long userId, ScheduleStatus status);

    long countByCompanyIdAndStatus(Long companyId, ScheduleStatus status);

    long countByJobpostingId(Long jobpostingId);

    long countByCompanyIdAndType(Long companyId, ScheduleType type);
}
