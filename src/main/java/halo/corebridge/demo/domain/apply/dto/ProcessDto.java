package halo.corebridge.demo.domain.apply.dto;

import halo.corebridge.demo.domain.apply.entity.ProcessHistory;
import halo.corebridge.demo.domain.apply.entity.RecruitmentProcess;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ProcessDto {

    @Getter @Builder
    public static class TransitionRequest {
        private ProcessStep nextStep;
        private Long changedBy;
        private String reason;
        private String note;
    }

    @Getter @Builder
    public static class ProcessResponse {
        private Long processId;
        private Long applyId;
        private Long jobpostingId;
        private Long userId;
        private ProcessStep currentStep;
        private String currentStepName;
        private ProcessStep previousStep;
        private String previousStepName;
        private Set<String> allowedNextSteps;
        private boolean completed;
        private boolean passed;
        private boolean failed;
        private LocalDateTime stepChangedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ProcessResponse from(RecruitmentProcess p) {
            return ProcessResponse.builder()
                    .processId(p.getProcessId()).applyId(p.getApplyId())
                    .jobpostingId(p.getJobpostingId()).userId(p.getUserId())
                    .currentStep(p.getCurrentStep())
                    .currentStepName(p.getCurrentStep().getDisplayName())
                    .previousStep(p.getPreviousStep())
                    .previousStepName(p.getPreviousStep() != null
                            ? p.getPreviousStep().getDisplayName() : null)
                    .allowedNextSteps(p.getCurrentStep().getAllowedNextSteps())
                    .completed(p.isCompleted()).passed(p.isPassed()).failed(p.isFailed())
                    .stepChangedAt(p.getStepChangedAt())
                    .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class ProcessPageResponse {
        private List<ProcessResponse> processes;
        private Long processCount;

        public static ProcessPageResponse of(List<ProcessResponse> list, Long count) {
            return ProcessPageResponse.builder().processes(list).processCount(count).build();
        }
    }

    @Getter @Builder
    public static class HistoryResponse {
        private Long historyId;
        private Long processId;
        private Long applyId;
        private ProcessStep fromStep;
        private String fromStepName;
        private ProcessStep toStep;
        private String toStepName;
        private Long changedBy;
        private String reason;
        private String note;
        private LocalDateTime createdAt;

        public static HistoryResponse from(ProcessHistory h) {
            return HistoryResponse.builder()
                    .historyId(h.getHistoryId()).processId(h.getProcessId()).applyId(h.getApplyId())
                    .fromStep(h.getFromStep())
                    .fromStepName(h.getFromStep() != null ? h.getFromStep().getDisplayName() : null)
                    .toStep(h.getToStep())
                    .toStepName(h.getToStep().getDisplayName())
                    .changedBy(h.getChangedBy())
                    .reason(h.getReason()).note(h.getNote())
                    .createdAt(h.getCreatedAt())
                    .build();
        }
    }

    @Getter @Builder
    public static class StepInfoResponse {
        private ProcessStep step;
        private String displayName;
        private Set<String> allowedNextSteps;
        private boolean terminal;

        public static StepInfoResponse from(ProcessStep step) {
            return StepInfoResponse.builder()
                    .step(step).displayName(step.getDisplayName())
                    .allowedNextSteps(step.getAllowedNextSteps())
                    .terminal(step.isTerminal())
                    .build();
        }
    }

    @Getter @Builder
    public static class UserStatsResponse {
        private Long totalProcesses;
        private Long pendingProcesses;
        private Long passedProcesses;
        private Long failedProcesses;
        private Double passRate;

        public static UserStatsResponse of(Long total, Long pending, Long passed, Long failed) {
            double rate = (passed + failed > 0)
                    ? Math.round((double) passed / (passed + failed) * 1000) / 10.0 : 0.0;
            return UserStatsResponse.builder()
                    .totalProcesses(total).pendingProcesses(pending)
                    .passedProcesses(passed).failedProcesses(failed).passRate(rate)
                    .build();
        }
    }

    @Getter @Builder
    public static class CompanyStatsResponse {
        private Long totalApplicants;
        private Long pendingApplicants;
        private Long interviewingApplicants;
        private Long passedApplicants;
        private Long failedApplicants;
        private Double passRate;

        public static CompanyStatsResponse of(Long total, Long pending, Long interviewing,
                                              Long passed, Long failed) {
            double rate = (passed + failed > 0)
                    ? Math.round((double) passed / (passed + failed) * 1000) / 10.0 : 0.0;
            return CompanyStatsResponse.builder()
                    .totalApplicants(total).pendingApplicants(pending)
                    .interviewingApplicants(interviewing)
                    .passedApplicants(passed).failedApplicants(failed).passRate(rate)
                    .build();
        }
    }
}
