package halo.corebridge.demo.domain.comment.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.comment.dto.CommentDto;
import halo.corebridge.demo.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public BaseResponse<CommentDto.CommentResponse> read(@PathVariable Long commentId) {
        return BaseResponse.success(commentService.read(commentId));
    }

    @PostMapping
    public BaseResponse<CommentDto.CommentResponse> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody CommentDto.CreateRequest request) {
        return BaseResponse.success(commentService.create(request, userId));
    }

    @DeleteMapping("/{commentId}")
    public BaseResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        commentService.delete(commentId, userId);
        return BaseResponse.success();
    }

    @GetMapping
    public BaseResponse<CommentDto.CommentPageResponse> readAll(
            @RequestParam Long jobpostingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.success(commentService.readAll(jobpostingId, page, size));
    }
}
