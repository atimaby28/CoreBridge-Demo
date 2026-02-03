package halo.corebridge.demo.domain.comment.repository;

import halo.corebridge.demo.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 공고별 댓글 조회 (페이징) - MSA의 native query를 Pageable로 단순화
     */
    Page<Comment> findByJobpostingIdOrderByParentCommentIdAscCommentIdAsc(
            Long jobpostingId, Pageable pageable);

    /**
     * 특정 부모 댓글의 자식 댓글 수
     */
    Long countByJobpostingIdAndParentCommentId(Long jobpostingId, Long parentCommentId);

    /**
     * 공고별 전체 댓글 수
     */
    Long countByJobpostingId(Long jobpostingId);

    /**
     * 사용자별 댓글 목록
     */
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
