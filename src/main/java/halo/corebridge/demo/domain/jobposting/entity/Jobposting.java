package halo.corebridge.demo.domain.jobposting.entity;

import halo.corebridge.demo.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobposting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Jobposting extends BaseTimeEntity {

    @Id
    private Long jobpostingId;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "CLOB")
    private String content;

    private Long boardId;
    private Long userId;

    /** 필수 스킬 (JSON 배열: ["Java", "Spring", ...]) */
    @Column(columnDefinition = "CLOB")
    private String requiredSkills;

    /** 우대 스킬 (JSON 배열: ["Kafka", "Kubernetes", ...]) */
    @Column(columnDefinition = "CLOB")
    private String preferredSkills;

    public static Jobposting create(Long jobpostingId, String title, String content,
                                    Long boardId, Long userId) {
        Jobposting jobposting = new Jobposting();
        jobposting.jobpostingId = jobpostingId;
        jobposting.title = title;
        jobposting.content = content;
        jobposting.boardId = boardId;
        jobposting.userId = userId;
        jobposting.createdAt = LocalDateTime.now();
        jobposting.updatedAt = jobposting.createdAt;
        return jobposting;
    }

    public static Jobposting create(Long jobpostingId, String title, String content,
                                    Long boardId, Long userId,
                                    String requiredSkills, String preferredSkills) {
        Jobposting jobposting = create(jobpostingId, title, content, boardId, userId);
        jobposting.requiredSkills = requiredSkills;
        jobposting.preferredSkills = preferredSkills;
        return jobposting;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content,
                       String requiredSkills, String preferredSkills) {
        this.title = title;
        this.content = content;
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
        this.updatedAt = LocalDateTime.now();
    }
}
