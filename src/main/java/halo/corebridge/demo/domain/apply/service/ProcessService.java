package halo.corebridge.demo.domain.apply.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.apply.dto.ProcessDto;
import halo.corebridge.demo.domain.apply.entity.ProcessHistory;
import halo.corebridge.demo.domain.apply.entity.RecruitmentProcess;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import halo.corebridge.demo.domain.apply.repository.ProcessHistoryRepository;
import halo.corebridge.demo.domain.apply.repository.RecruitmentProcessRepository;
import halo.corebridge.demo.domain.notification.dto.NotificationDto;
import halo.corebridge.demo.domain.notification.enums.NotificationType;
import halo.corebridge.demo.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 채용 프로세스 서비스 (State Machine)
 */
@Service
@RequiredArgsConstructor
public class ProcessService {

    private final Snowflake snowflake;
    private final RecruitmentProcessRepository processRepository;
    private final ProcessHistoryRepository historyRepository;
    private final NotificationService notificationService;

    // ============================================
    // 프로세스 생성
    // ============================================

    @Transactional
    public RecruitmentProcess createProcess(Long applyId, Long jobpostingId, Long userId) {
        RecruitmentProcess process = RecruitmentProcess.create(
                snowflake.nextId(), applyId, jobpostingId, userId);
        processRepository.save(process);

        historyRepository.save(ProcessHistory.create(
                snowflake.nextId(), process.getProcessId(), applyId,
                null, ProcessStep.APPLIED, null, "지원 완료", null));

        return process;
    }

    // ============================================
    // 상태 전이 (State Machine 핵심)
    // ============================================

    @Transactional
    public ProcessDto.ProcessResponse transition(Long processId, ProcessDto.TransitionRequest request) {
        RecruitmentProcess process = processRepository.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("프로세스를 찾을 수 없습니다: " + processId));

        ProcessStep fromStep = process.getCurrentStep();
        ProcessStep toStep = request.getNextStep();

        // State Machine: 전이 규칙 검증
        process.transition(toStep);

        historyRepository.save(ProcessHistory.create(
                snowflake.nextId(), processId, process.getApplyId(),
                fromStep, toStep, request.getChangedBy(), request.getReason(), request.getNote()));

        // 내부 알림 전송 (MSA에서는 Feign Client 호출이었음)
        sendProcessNotification(process.getUserId(), toStep, process.getApplyId(), process.getJobpostingId());

        return ProcessDto.ProcessResponse.from(process);
    }

    @Transactional
    public ProcessDto.ProcessResponse transitionByApplyId(Long applyId, ProcessDto.TransitionRequest request) {
        RecruitmentProcess process = processRepository.findByApplyId(applyId)
                .orElseThrow(() -> new IllegalArgumentException("프로세스를 찾을 수 없습니다 (applyId): " + applyId));
        return transition(process.getProcessId(), request);
    }

    // ============================================
    // 조회
    // ============================================

    @Transactional(readOnly = true)
    public ProcessDto.ProcessResponse read(Long processId) {
        return ProcessDto.ProcessResponse.from(processRepository.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("프로세스를 찾을 수 없습니다: " + processId)));
    }

    @Transactional(readOnly = true)
    public ProcessDto.ProcessResponse readByApplyId(Long applyId) {
        return ProcessDto.ProcessResponse.from(processRepository.findByApplyId(applyId)
                .orElseThrow(() -> new IllegalArgumentException("프로세스를 찾을 수 없습니다 (applyId): " + applyId)));
    }

    @Transactional(readOnly = true)
    public ProcessDto.ProcessPageResponse getByJobposting(Long jobpostingId) {
        List<ProcessDto.ProcessResponse> list = processRepository
                .findByJobpostingIdOrderByCreatedAtDesc(jobpostingId).stream()
                .map(ProcessDto.ProcessResponse::from).toList();
        return ProcessDto.ProcessPageResponse.of(list, processRepository.countByJobpostingId(jobpostingId));
    }

    @Transactional(readOnly = true)
    public ProcessDto.ProcessPageResponse getByJobpostingAndStep(Long jobpostingId, ProcessStep step) {
        List<ProcessDto.ProcessResponse> list = processRepository
                .findByJobpostingIdAndCurrentStepOrderByStepChangedAtDesc(jobpostingId, step).stream()
                .map(ProcessDto.ProcessResponse::from).toList();
        return ProcessDto.ProcessPageResponse.of(list, (long) list.size());
    }

    @Transactional(readOnly = true)
    public ProcessDto.ProcessPageResponse getByUser(Long userId) {
        List<ProcessDto.ProcessResponse> list = processRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProcessDto.ProcessResponse::from).toList();
        return ProcessDto.ProcessPageResponse.of(list, processRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<ProcessDto.HistoryResponse> getHistory(Long processId) {
        return historyRepository.findByProcessIdOrderByCreatedAtDesc(processId).stream()
                .map(ProcessDto.HistoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessDto.HistoryResponse> getHistoryByApplyId(Long applyId) {
        return historyRepository.findByApplyIdOrderByCreatedAtDesc(applyId).stream()
                .map(ProcessDto.HistoryResponse::from).toList();
    }

    public List<ProcessDto.StepInfoResponse> getAllSteps() {
        return Arrays.stream(ProcessStep.values())
                .map(ProcessDto.StepInfoResponse::from).toList();
    }

    // ============================================
    // 통계
    // ============================================

    @Transactional(readOnly = true)
    public ProcessDto.UserStatsResponse getUserStats(Long userId) {
        Long total = processRepository.countByUserId(userId);
        List<ProcessStep> pendingSteps = List.of(
                ProcessStep.APPLIED, ProcessStep.DOCUMENT_REVIEW, ProcessStep.DOCUMENT_PASS,
                ProcessStep.CODING_TEST, ProcessStep.CODING_PASS,
                ProcessStep.INTERVIEW_1, ProcessStep.INTERVIEW_1_PASS,
                ProcessStep.INTERVIEW_2, ProcessStep.INTERVIEW_2_PASS, ProcessStep.FINAL_REVIEW);
        Long pending = processRepository.countByUserIdAndCurrentStepIn(userId, pendingSteps);
        Long passed = processRepository.countByUserIdAndCurrentStep(userId, ProcessStep.FINAL_PASS);
        List<ProcessStep> failSteps = List.of(ProcessStep.DOCUMENT_FAIL, ProcessStep.CODING_FAIL,
                ProcessStep.INTERVIEW_1_FAIL, ProcessStep.INTERVIEW_2_FAIL, ProcessStep.FINAL_FAIL);
        Long failed = processRepository.countByUserIdAndCurrentStepIn(userId, failSteps);
        return ProcessDto.UserStatsResponse.of(total, pending, passed, failed);
    }

    @Transactional(readOnly = true)
    public ProcessDto.CompanyStatsResponse getJobpostingStats(Long jobpostingId) {
        Long total = processRepository.countByJobpostingId(jobpostingId);
        Long pending = processRepository.countByJobpostingIdAndCurrentStepIn(jobpostingId,
                List.of(ProcessStep.APPLIED, ProcessStep.DOCUMENT_REVIEW));
        List<ProcessStep> interviewSteps = List.of(ProcessStep.DOCUMENT_PASS, ProcessStep.CODING_TEST,
                ProcessStep.CODING_PASS, ProcessStep.INTERVIEW_1, ProcessStep.INTERVIEW_1_PASS,
                ProcessStep.INTERVIEW_2, ProcessStep.INTERVIEW_2_PASS, ProcessStep.FINAL_REVIEW);
        Long interviewing = processRepository.countByJobpostingIdAndCurrentStepIn(jobpostingId, interviewSteps);
        Long passed = processRepository.countByJobpostingIdAndCurrentStep(jobpostingId, ProcessStep.FINAL_PASS);
        List<ProcessStep> failSteps = List.of(ProcessStep.DOCUMENT_FAIL, ProcessStep.CODING_FAIL,
                ProcessStep.INTERVIEW_1_FAIL, ProcessStep.INTERVIEW_2_FAIL, ProcessStep.FINAL_FAIL);
        Long failed = processRepository.countByJobpostingIdAndCurrentStepIn(jobpostingId, failSteps);
        return ProcessDto.CompanyStatsResponse.of(total, pending, interviewing, passed, failed);
    }

    /**
     * 여러 공고의 통합 통계
     */
    @Transactional(readOnly = true)
    public ProcessDto.CompanyStatsResponse getCompanyStats(List<Long> jobpostingIds) {
        if (jobpostingIds == null || jobpostingIds.isEmpty()) {
            return ProcessDto.CompanyStatsResponse.of(0L, 0L, 0L, 0L, 0L);
        }
        Long total = processRepository.countByJobpostingIdIn(jobpostingIds);
        Long pending = processRepository.countByJobpostingIdInAndCurrentStepIn(jobpostingIds,
                List.of(ProcessStep.APPLIED, ProcessStep.DOCUMENT_REVIEW));
        List<ProcessStep> interviewSteps = List.of(ProcessStep.DOCUMENT_PASS, ProcessStep.CODING_TEST,
                ProcessStep.CODING_PASS, ProcessStep.INTERVIEW_1, ProcessStep.INTERVIEW_1_PASS,
                ProcessStep.INTERVIEW_2, ProcessStep.INTERVIEW_2_PASS, ProcessStep.FINAL_REVIEW);
        Long interviewing = processRepository.countByJobpostingIdInAndCurrentStepIn(jobpostingIds, interviewSteps);
        Long passed = processRepository.countByJobpostingIdInAndCurrentStep(jobpostingIds, ProcessStep.FINAL_PASS);
        List<ProcessStep> failSteps = List.of(ProcessStep.DOCUMENT_FAIL, ProcessStep.CODING_FAIL,
                ProcessStep.INTERVIEW_1_FAIL, ProcessStep.INTERVIEW_2_FAIL, ProcessStep.FINAL_FAIL);
        Long failed = processRepository.countByJobpostingIdInAndCurrentStepIn(jobpostingIds, failSteps);
        return ProcessDto.CompanyStatsResponse.of(total, pending, interviewing, passed, failed);
    }

    // ============================================
    // Private
    // ============================================

    private void sendProcessNotification(Long userId, ProcessStep step, Long applyId, Long jobpostingId) {
        try {
            notificationService.create(NotificationDto.CreateRequest.builder()
                    .userId(userId)
                    .type(NotificationType.PROCESS_UPDATE)
                    .title("채용 프로세스 업데이트")
                    .message("지원 상태가 [" + step.getDisplayName() + "](으)로 변경되었습니다.")
                    .link("/applies/" + applyId)
                    .relatedId(applyId)
                    .relatedType("APPLY")
                    .build());
        } catch (Exception e) {
            // 알림 실패가 비즈니스 로직에 영향을 주지 않도록 예외 무시
        }
    }
}
