package halo.corebridge.demo.domain.jobposting.repository;

import halo.corebridge.demo.domain.jobposting.entity.Jobposting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobpostingRepository extends JpaRepository<Jobposting, Long> {

    /**
     * 게시판별 공고 목록 (페이징)
     * MSA 원본의 native query를 Spring Data Pageable로 단순화
     */
    Page<Jobposting> findByBoardIdOrderByJobpostingIdDesc(Long boardId, Pageable pageable);

    /**
     * 전체 공고 목록 (페이징)
     */
    Page<Jobposting> findAllByOrderByJobpostingIdDesc(Pageable pageable);

    /**
     * 게시판별 공고 수
     */
    Long countByBoardId(Long boardId);

    /**
     * 사용자별 공고 목록 (기업이 작성한 공고)
     */
    List<Jobposting> findByUserIdOrderByCreatedAtDesc(Long userId);
}
