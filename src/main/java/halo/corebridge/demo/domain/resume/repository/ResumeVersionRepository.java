package halo.corebridge.demo.domain.resume.repository;

import halo.corebridge.demo.domain.resume.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByResumeIdOrderByVersionDesc(Long resumeId);

    Optional<ResumeVersion> findByResumeIdAndVersion(Long resumeId, int version);

    Optional<ResumeVersion> findTopByResumeIdOrderByVersionDesc(Long resumeId);
}
