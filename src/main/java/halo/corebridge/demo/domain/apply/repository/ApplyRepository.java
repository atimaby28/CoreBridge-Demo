package halo.corebridge.demo.domain.apply.repository;

import halo.corebridge.demo.domain.apply.entity.Apply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplyRepository extends JpaRepository<Apply, Long> {

    boolean existsByJobpostingIdAndUserId(Long jobpostingId, Long userId);

    List<Apply> findByJobpostingIdOrderByCreatedAtDesc(Long jobpostingId);

    Long countByJobpostingId(Long jobpostingId);

    List<Apply> findByUserIdOrderByCreatedAtDesc(Long userId);

    Long countByUserId(Long userId);

    Optional<Apply> findByJobpostingIdAndUserId(Long jobpostingId, Long userId);

    @Query("SELECT COUNT(a) FROM Apply a WHERE a.jobpostingId IN :jobpostingIds")
    Long countByJobpostingIdIn(@Param("jobpostingIds") List<Long> jobpostingIds);
}
