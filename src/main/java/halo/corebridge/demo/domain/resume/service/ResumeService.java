package halo.corebridge.demo.domain.resume.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.resume.dto.ResumeDto;
import halo.corebridge.demo.domain.resume.entity.Resume;
import halo.corebridge.demo.domain.resume.entity.ResumeVersion;
import halo.corebridge.demo.domain.resume.repository.ResumeRepository;
import halo.corebridge.demo.domain.resume.repository.ResumeVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeService {

    private final Snowflake snowflake;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;

    @Transactional
    public ResumeDto.ResumeResponse getOrCreate(Long userId) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("사용자 {}의 이력서 생성", userId);
                    return resumeRepository.save(Resume.create(snowflake.nextId(), userId));
                });
        return ResumeDto.ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDto.ResumeResponse get(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        return ResumeDto.ResumeResponse.from(resume);
    }

    @Transactional
    public ResumeDto.ResumeResponse update(Long userId, ResumeDto.UpdateRequest request) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        // 현재 내용을 버전 스냅샷으로 저장
        if (resume.getContent() != null) {
            versionRepository.save(ResumeVersion.create(
                    snowflake.nextId(), resume.getId(), resume.getCurrentVersion(),
                    resume.getTitle(), resume.getContent(), request.getMemo()));
            log.info("이력서 버전 {} 저장됨", resume.getCurrentVersion());
        }

        resume.update(request.getTitle(), request.getContent());
        if (request.getSkills() != null) {
            resume.updateSkills(toJson(request.getSkills()));
        }
        resumeRepository.save(resume);

        // AI 분석은 데모에서 Mock (Step 6에서 구현)
        log.info("이력서 업데이트: resumeId={}, userId={}", resume.getId(), userId);
        return ResumeDto.ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDto.VersionListResponse getVersions(Long userId) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        List<ResumeDto.VersionResponse> responses = versionRepository
                .findByResumeIdOrderByVersionDesc(resume.getId()).stream()
                .map(ResumeDto.VersionResponse::from).toList();
        return ResumeDto.VersionListResponse.builder()
                .versions(responses).totalCount(responses.size()).build();
    }

    @Transactional(readOnly = true)
    public ResumeDto.VersionResponse getVersion(Long userId, int version) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        ResumeVersion rv = versionRepository.findByResumeIdAndVersion(resume.getId(), version)
                .orElseThrow(() -> new IllegalArgumentException("버전을 찾을 수 없습니다."));
        return ResumeDto.VersionResponse.from(rv);
    }

    @Transactional
    public ResumeDto.ResumeResponse restoreVersion(Long userId, int version) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        ResumeVersion target = versionRepository.findByResumeIdAndVersion(resume.getId(), version)
                .orElseThrow(() -> new IllegalArgumentException("버전을 찾을 수 없습니다."));

        // 현재 내용 백업
        versionRepository.save(ResumeVersion.create(
                snowflake.nextId(), resume.getId(), resume.getCurrentVersion(),
                resume.getTitle(), resume.getContent(),
                "버전 " + version + "으로 복원 전 백업"));

        resume.restoreFromVersion(target.getTitle(), target.getContent(), resume.getCurrentVersion() + 1);
        resumeRepository.save(resume);

        log.info("이력서를 버전 {}에서 복원, 새 버전: {}", version, resume.getCurrentVersion());
        return ResumeDto.ResumeResponse.from(resume);
    }

    /**
     * AI 분석 요청 (데모: Mock 처리)
     */
    @Transactional
    public ResumeDto.ResumeResponse requestAnalysis(Long userId) {
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        if (resume.getContent() == null || resume.getContent().isBlank()) {
            throw new IllegalStateException("이력서 내용이 없습니다.");
        }

        resume.markAnalyzing();
        resumeRepository.save(resume);

        // Mock: 즉시 분석 완료 처리
        resume.updateAiAnalysis(
                "[Mock] 이력서 분석 완료. 백엔드 개발 역량이 우수합니다.",
                "[\"Java\",\"Spring Boot\",\"JPA\",\"MSA\"]"
        );
        resumeRepository.save(resume);

        log.info("AI 분석 Mock 완료: resumeId={}", resume.getId());
        return ResumeDto.ResumeResponse.from(resume);
    }

    @Transactional
    public ResumeDto.ResumeResponse updateAiResult(Long resumeId, ResumeDto.AiResultRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        resume.updateAiAnalysis(request.getSummary(), request.getSkills());
        resumeRepository.save(resume);
        log.info("AI 분석 결과 저장: resumeId={}", resumeId);
        return ResumeDto.ResumeResponse.from(resume);
    }

    private String toJson(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return "[\"" + String.join("\",\"", skills) + "\"]";
    }
}
