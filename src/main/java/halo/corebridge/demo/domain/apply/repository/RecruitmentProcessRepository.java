package halo.corebridge.demo.domain.apply.repository;

import halo.corebridge.demo.domain.apply.entity.RecruitmentProcess;
import halo.corebridge.demo.domain.apply.enums.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecruitmentProcessRepository extends JpaRepository<RecruitmentProcess, Long> {

    Optional<RecruitmentProcess> findByApplyId(Long applyId);

    List<RecruitmentProcess> findByJobpostingIdOrderByCreatedAtDesc(Long jobpostingId);

    Long countByJobpostingId(Long jobpostingId);

    List<RecruitmentProcess> findByJobpostingIdAndCurrentStepOrderByStepChangedAtDesc(
            Long jobpostingId, ProcessStep step);

    List<RecruitmentProcess> findByUserIdOrderByCreatedAtDesc(Long userId);

    Long countByUserId(Long userId);

    List<RecruitmentProcess> findByCurrentStepOrderByStepChangedAtAsc(ProcessStep step);

    Long countByUserIdAndCurrentStep(Long userId, ProcessStep step);

    @Query("SELECT COUNT(p) FROM RecruitmentProcess p WHERE p.userId = :userId AND p.currentStep IN :steps")
    Long countByUserIdAndCurrentStepIn(@Param("userId") Long userId, @Param("steps") List<ProcessStep> steps);

    Long countByJobpostingIdAndCurrentStep(Long jobpostingId, ProcessStep step);

    @Query("SELECT COUNT(p) FROM RecruitmentProcess p WHERE p.jobpostingId = :jobpostingId AND p.currentStep IN :steps")
    Long countByJobpostingIdAndCurrentStepIn(@Param("jobpostingId") Long jobpostingId, @Param("steps") List<ProcessStep> steps);

    // 다건 공고 통합 통계
    @Query("SELECT COUNT(p) FROM RecruitmentProcess p WHERE p.jobpostingId IN :ids")
    Long countByJobpostingIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(p) FROM RecruitmentProcess p WHERE p.jobpostingId IN :ids AND p.currentStep = :step")
    Long countByJobpostingIdInAndCurrentStep(@Param("ids") List<Long> ids, @Param("step") ProcessStep step);

    @Query("SELECT COUNT(p) FROM RecruitmentProcess p WHERE p.jobpostingId IN :ids AND p.currentStep IN :steps")
    Long countByJobpostingIdInAndCurrentStepIn(@Param("ids") List<Long> ids, @Param("steps") List<ProcessStep> steps);
}
