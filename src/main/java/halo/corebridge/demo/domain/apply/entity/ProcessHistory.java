package halo.corebridge.demo.domain.apply.entity;

import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채용 프로세스 상태 변경 이력
 */
@Entity
@Table(name = "process_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessHistory {

    @Id
    private Long historyId;

    private Long processId;

    private Long applyId;

    @Enumerated(EnumType.STRING)
    private ProcessStep fromStep;

    @Enumerated(EnumType.STRING)
    private ProcessStep toStep;

    private Long changedBy;

    private String reason;

    private String note;

    private LocalDateTime createdAt;

    public static ProcessHistory create(Long historyId, Long processId, Long applyId,
                                        ProcessStep fromStep, ProcessStep toStep,
                                        Long changedBy, String reason, String note) {
        ProcessHistory history = new ProcessHistory();
        history.historyId = historyId;
        history.processId = processId;
        history.applyId = applyId;
        history.fromStep = fromStep;
        history.toStep = toStep;
        history.changedBy = changedBy;
        history.reason = reason;
        history.note = note;
        history.createdAt = LocalDateTime.now();
        return history;
    }
}
