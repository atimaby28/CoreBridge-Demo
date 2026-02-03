package halo.corebridge.demo.domain.apply.service;

import halo.corebridge.demo.common.exception.BaseException;
import halo.corebridge.demo.common.response.BaseResponseStatus;
import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.apply.dto.ApplyDto;
import halo.corebridge.demo.domain.apply.dto.ProcessDto;
import halo.corebridge.demo.domain.apply.entity.Apply;
import halo.corebridge.demo.domain.apply.entity.RecruitmentProcess;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import halo.corebridge.demo.domain.apply.repository.ApplyRepository;
import halo.corebridge.demo.domain.apply.repository.RecruitmentProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final Snowflake snowflake;
    private final ApplyRepository applyRepository;
    private final RecruitmentProcessRepository processRepository;
    private final ProcessService processService;

    @Transactional
    public ApplyDto.ApplyDetailResponse apply(ApplyDto.CreateRequest request) {
        if (applyRepository.existsByJobpostingIdAndUserId(
                request.getJobpostingId(), request.getUserId())) {
            throw new BaseException(BaseResponseStatus.ALREADY_APPLIED);
        }

        Apply apply = Apply.create(snowflake.nextId(), request.getJobpostingId(),
                request.getUserId(), request.getResumeId(), request.getCoverLetter());
        applyRepository.save(apply);

        RecruitmentProcess process = processService.createProcess(
                apply.getApplyId(), apply.getJobpostingId(), apply.getUserId());

        return ApplyDto.ApplyDetailResponse.from(apply, process);
    }

    @Transactional
    public void cancel(Long applyId, Long userId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.APPLICATION_NOT_FOUND));
        if (!apply.getUserId().equals(userId)) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }

        RecruitmentProcess process = processRepository.findByApplyId(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROCESS_NOT_FOUND));
        if (process.getCurrentStep() != ProcessStep.APPLIED) {
            throw new BaseException(BaseResponseStatus.CANNOT_CANCEL_IN_PROGRESS);
        }

        processRepository.delete(process);
        applyRepository.delete(apply);
    }

    @Transactional(readOnly = true)
    public ApplyDto.ApplyPageResponse getMyApplies(Long userId) {
        List<Apply> applies = applyRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ApplyDto.ApplyDetailResponse> responses = applies.stream()
                .map(apply -> processRepository.findByApplyId(apply.getApplyId())
                        .map(p -> ApplyDto.ApplyDetailResponse.from(apply, p)).orElse(null))
                .filter(r -> r != null).toList();
        return ApplyDto.ApplyPageResponse.of(responses, applyRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    public ApplyDto.ApplyDetailResponse read(Long applyId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.APPLICATION_NOT_FOUND));
        RecruitmentProcess process = processRepository.findByApplyId(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROCESS_NOT_FOUND));
        return ApplyDto.ApplyDetailResponse.from(apply, process);
    }

    @Transactional(readOnly = true)
    public ApplyDto.ApplyPageResponse getAppliesByJobposting(Long jobpostingId) {
        List<Apply> applies = applyRepository.findByJobpostingIdOrderByCreatedAtDesc(jobpostingId);
        List<ApplyDto.ApplyDetailResponse> responses = applies.stream()
                .map(apply -> processRepository.findByApplyId(apply.getApplyId())
                        .map(p -> ApplyDto.ApplyDetailResponse.from(apply, p)).orElse(null))
                .filter(r -> r != null).toList();
        return ApplyDto.ApplyPageResponse.of(responses, applyRepository.countByJobpostingId(jobpostingId));
    }

    @Transactional(readOnly = true)
    public ApplyDto.ApplyPageResponse getAppliesByStep(Long jobpostingId, ProcessStep step) {
        List<RecruitmentProcess> processes = processRepository
                .findByJobpostingIdAndCurrentStepOrderByStepChangedAtDesc(jobpostingId, step);
        List<ApplyDto.ApplyDetailResponse> responses = processes.stream()
                .map(process -> applyRepository.findById(process.getApplyId())
                        .map(a -> ApplyDto.ApplyDetailResponse.from(a, process)).orElse(null))
                .filter(r -> r != null).toList();
        return ApplyDto.ApplyPageResponse.of(responses, (long) responses.size());
    }

    @Transactional
    public ApplyDto.ApplyDetailResponse updateMemo(Long applyId, ApplyDto.UpdateMemoRequest request) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.APPLICATION_NOT_FOUND));
        apply.updateMemo(request.getMemo());
        RecruitmentProcess process = processRepository.findByApplyId(applyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROCESS_NOT_FOUND));
        return ApplyDto.ApplyDetailResponse.from(apply, process);
    }

    @Transactional(readOnly = true)
    public ProcessDto.UserStatsResponse getUserStats(Long userId) {
        return processService.getUserStats(userId);
    }

    @Transactional(readOnly = true)
    public ProcessDto.CompanyStatsResponse getJobpostingStats(Long jobpostingId) {
        return processService.getJobpostingStats(jobpostingId);
    }
}
