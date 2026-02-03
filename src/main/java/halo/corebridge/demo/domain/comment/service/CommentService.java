package halo.corebridge.demo.domain.comment.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.comment.dto.CommentDto;
import halo.corebridge.demo.domain.comment.entity.Comment;
import halo.corebridge.demo.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentDto.CommentResponse create(CommentDto.CreateRequest request, Long userId) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(snowflake.nextId(), request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getJobpostingId(), userId));
        return CommentDto.CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public CommentDto.CommentResponse read(Long commentId) {
        return CommentDto.CommentResponse.from(
                commentRepository.findById(commentId)
                        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다.")));
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (!comment.getUserId().equals(userId)) {
                        throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
                    }
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        deleteRecursively(comment);
                    }
                });
    }

    @Transactional(readOnly = true)
    public CommentDto.CommentPageResponse readAll(Long jobpostingId, int page, int size) {
        Page<Comment> result = commentRepository
                .findByJobpostingIdOrderByParentCommentIdAscCommentIdAsc(
                        jobpostingId, PageRequest.of(page, size));
        return CommentDto.CommentPageResponse.of(
                result.getContent().stream().map(CommentDto.CommentResponse::from).toList(),
                result.getTotalElements());
    }

    private Comment findParent(CommentDto.CreateRequest request) {
        Long parentId = request.getParentCommentId();
        if (parentId == null) return null;
        return commentRepository.findById(parentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 부모 댓글입니다."));
    }

    private boolean hasChildren(Comment comment) {
        Long count = commentRepository.countByJobpostingIdAndParentCommentId(
                comment.getJobpostingId(), comment.getCommentId());
        return count != null && count > 0;
    }

    private void deleteRecursively(Comment comment) {
        commentRepository.delete(comment);
        if (!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::deleteRecursively);
        }
    }
}
