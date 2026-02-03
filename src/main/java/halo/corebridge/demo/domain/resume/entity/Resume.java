package halo.corebridge.demo.domain.resume.entity;

import halo.corebridge.demo.common.domain.BaseTimeEntity;
import halo.corebridge.demo.domain.resume.enums.ResumeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이력서 엔티티
 * - 사용자당 1개의 이력서만 존재
 * - AI 분석 결과 저장 (데모에서는 Mock)
 * - 버전 관리는 ResumeVersion 엔티티에서 담당
 */
@Entity
@Table(
    name = "resume",
    uniqueConstraints = @UniqueConstraint(columnNames = "userId")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String title;

    @Column(columnDefinition = "CLOB")
    private String content;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;

    private int currentVersion;

    /** 사용자가 직접 입력한 보유 스킬 목록 (JSON 배열) */
    @Column(columnDefinition = "CLOB")
    private String skills;

    // AI 분석 결과 필드 (데모에서는 Mock 데이터)
    @Column(columnDefinition = "CLOB")
    private String aiSummary;

    @Column(columnDefinition = "CLOB")
    private String aiSkills;

    private LocalDateTime analyzedAt;

    // === Factory Methods ===

    public static Resume create(Long id, Long userId) {
        Resume resume = new Resume();
        resume.id = id;
        resume.userId = userId;
        resume.title = "내 이력서";
        resume.status = ResumeStatus.DRAFT;
        resume.currentVersion = 1;
        return resume;
    }

    public static Resume create(Long id, Long userId, String title, String content) {
        Resume resume = new Resume();
        resume.id = id;
        resume.userId = userId;
        resume.title = title;
        resume.content = content;
        resume.status = ResumeStatus.DRAFT;
        resume.currentVersion = 1;
        return resume;
    }

    // === Business Methods ===

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.currentVersion++;
        clearAiAnalysis();
    }

    public void updateSkills(String skills) {
        this.skills = skills;
    }

    public void restoreFromVersion(String title, String content, int newVersion) {
        this.title = title;
        this.content = content;
        this.currentVersion = newVersion;
        clearAiAnalysis();
    }

    public void markAnalyzing() {
        this.status = ResumeStatus.ANALYZING;
    }

    public void updateAiAnalysis(String summary, String skills) {
        this.aiSummary = summary;
        this.aiSkills = skills;
        this.analyzedAt = LocalDateTime.now();
        this.status = ResumeStatus.ANALYZED;
    }

    public void clearAiAnalysis() {
        this.aiSummary = null;
        this.aiSkills = null;
        this.analyzedAt = null;
        this.status = ResumeStatus.DRAFT;
    }

    public void delete() {
        this.status = ResumeStatus.DELETED;
    }
}
