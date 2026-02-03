package halo.corebridge.demo.domain.apply.entity;

import halo.corebridge.demo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지원 정보
 *
 * 채용 공고에 대한 지원 기록을 저장합니다.
 * 상태(진행 단계)는 RecruitmentProcess에서 관리합니다.
 */
@Entity
@Table(
        name = "apply",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jobpostingId", "userId"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply extends BaseTimeEntity {

    @Id
    private Long applyId;

    @Column(nullable = false)
    private Long jobpostingId;

    @Column(nullable = false)
    private Long userId;

    private Long resumeId;

    private String memo;

    @Column(columnDefinition = "CLOB")
    private String coverLetter;

    public static Apply create(Long applyId, Long jobpostingId, Long userId,
                               Long resumeId, String coverLetter) {
        Apply apply = new Apply();
        apply.applyId = applyId;
        apply.jobpostingId = jobpostingId;
        apply.userId = userId;
        apply.resumeId = resumeId;
        apply.coverLetter = coverLetter;
        apply.createdAt = LocalDateTime.now();
        apply.updatedAt = apply.createdAt;
        return apply;
    }

    public static Apply create(Long applyId, Long jobpostingId, Long userId,
                               Long resumeId) {
        return create(applyId, jobpostingId, userId, resumeId, null);
    }

    public void updateMemo(String memo) {
        this.memo = memo;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateResume(Long resumeId) {
        this.resumeId = resumeId;
        this.updatedAt = LocalDateTime.now();
    }
}
