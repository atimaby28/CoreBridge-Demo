package halo.corebridge.demo.domain.resume.entity;

import halo.corebridge.demo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이력서 버전 엔티티
 * - 이력서 내용의 스냅샷 저장
 * - 이전 버전으로 복원 가능
 */
@Entity
@Table(name = "resume_version", indexes = {
    @Index(name = "idx_rv_resume_id", columnList = "resumeId"),
    @Index(name = "idx_rv_version", columnList = "resumeId, version")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeVersion extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private int version;

    private String title;

    @Column(columnDefinition = "CLOB")
    private String content;

    private String memo;

    public static ResumeVersion create(Long id, Long resumeId, int version,
                                       String title, String content) {
        ResumeVersion rv = new ResumeVersion();
        rv.id = id;
        rv.resumeId = resumeId;
        rv.version = version;
        rv.title = title;
        rv.content = content;
        return rv;
    }

    public static ResumeVersion create(Long id, Long resumeId, int version,
                                       String title, String content, String memo) {
        ResumeVersion rv = create(id, resumeId, version, title, content);
        rv.memo = memo;
        return rv;
    }
}
