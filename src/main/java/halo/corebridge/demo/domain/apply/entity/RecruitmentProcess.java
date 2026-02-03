package halo.corebridge.demo.domain.apply.entity;

import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채용 프로세스 (지원자별 진행 상태)
 *
 * State Machine 패턴을 적용하여 상태 전이를 관리합니다.
 * Apply와 1:1 관계이며, 지원 시 함께 생성됩니다.
 */
@Entity
@Table(name = "recruitment_process")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentProcess {

    @Id
    private Long processId;

    @Column(nullable = false, unique = true)
    private Long applyId;

    @Column(nullable = false)
    private Long jobpostingId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStep currentStep;

    @Enumerated(EnumType.STRING)
    private ProcessStep previousStep;

    private LocalDateTime stepChangedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static RecruitmentProcess create(Long processId, Long applyId,
                                            Long jobpostingId, Long userId) {
        RecruitmentProcess process = new RecruitmentProcess();
        process.processId = processId;
        process.applyId = applyId;
        process.jobpostingId = jobpostingId;
        process.userId = userId;
        process.currentStep = ProcessStep.APPLIED;
        process.previousStep = null;
        process.stepChangedAt = LocalDateTime.now();
        process.createdAt = LocalDateTime.now();
        process.updatedAt = process.createdAt;
        return process;
    }

    /**
     * 상태 전이 (State Machine 핵심 메서드)
     */
    public void transition(ProcessStep nextStep) {
        if (!currentStep.canTransitionTo(nextStep)) {
            throw new IllegalStateException(
                    String.format("'%s'에서 '%s'(으)로 전이할 수 없습니다. 허용된 전이: %s",
                            currentStep.getDisplayName(),
                            nextStep.getDisplayName(),
                            currentStep.getAllowedNextSteps())
            );
        }

        this.previousStep = this.currentStep;
        this.currentStep = nextStep;
        this.stepChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return currentStep.isTerminal();
    }

    public boolean isPassed() {
        return currentStep.isPass();
    }

    public boolean isFailed() {
        return currentStep.isFail();
    }

    public boolean isInProgress() {
        return currentStep.isInProgress();
    }
}
