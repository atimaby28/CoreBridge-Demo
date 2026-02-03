package halo.corebridge.demo.domain.apply.repository;

import halo.corebridge.demo.domain.apply.entity.ProcessHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessHistoryRepository extends JpaRepository<ProcessHistory, Long> {

    List<ProcessHistory> findByProcessIdOrderByCreatedAtDesc(Long processId);

    List<ProcessHistory> findByApplyIdOrderByCreatedAtDesc(Long applyId);

    Long countByProcessId(Long processId);
}
