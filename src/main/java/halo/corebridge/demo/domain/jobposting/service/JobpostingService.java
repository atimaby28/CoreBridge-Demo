package halo.corebridge.demo.domain.jobposting.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.jobposting.dto.JobpostingDto;
import halo.corebridge.demo.domain.jobposting.entity.Jobposting;
import halo.corebridge.demo.domain.jobposting.repository.JobpostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobpostingService {

    private final Snowflake snowflake;
    private final JobpostingRepository jobpostingRepository;

    @Transactional
    public JobpostingDto.JobpostingResponse create(Long userId, JobpostingDto.CreateRequest request) {
        Jobposting jobposting = jobpostingRepository.save(
                Jobposting.create(
                        snowflake.nextId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBoardId(),
                        userId,
                        toJson(request.getRequiredSkills()),
                        toJson(request.getPreferredSkills())
                )
        );
        log.info("채용공고 생성: jobpostingId={}, userId={}", jobposting.getJobpostingId(), userId);
        return JobpostingDto.JobpostingResponse.from(jobposting);
    }

    @Transactional
    public JobpostingDto.JobpostingResponse update(Long userId, Long jobpostingId,
                                                   JobpostingDto.UpdateRequest request) {
        Jobposting jobposting = jobpostingRepository.findById(jobpostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다: " + jobpostingId));
        validateOwner(jobposting, userId);

        jobposting.update(request.getTitle(), request.getContent(),
                toJson(request.getRequiredSkills()), toJson(request.getPreferredSkills()));
        log.info("채용공고 수정: jobpostingId={}, userId={}", jobpostingId, userId);
        return JobpostingDto.JobpostingResponse.from(jobposting);
    }

    @Transactional
    public void delete(Long userId, Long jobpostingId) {
        Jobposting jobposting = jobpostingRepository.findById(jobpostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다: " + jobpostingId));
        validateOwner(jobposting, userId);
        jobpostingRepository.deleteById(jobpostingId);
        log.info("채용공고 삭제: jobpostingId={}, userId={}", jobpostingId, userId);
    }

    @Transactional(readOnly = true)
    public JobpostingDto.JobpostingResponse read(Long jobpostingId) {
        return JobpostingDto.JobpostingResponse.from(
                jobpostingRepository.findById(jobpostingId)
                        .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다: " + jobpostingId))
        );
    }

    @Transactional(readOnly = true)
    public JobpostingDto.JobpostingPageResponse readAll(Long boardId, int page, int size) {
        Page<Jobposting> result;
        if (boardId == null || boardId == 1L) {
            result = jobpostingRepository.findAllByOrderByJobpostingIdDesc(PageRequest.of(page, size));
        } else {
            result = jobpostingRepository.findByBoardIdOrderByJobpostingIdDesc(boardId, PageRequest.of(page, size));
        }
        List<JobpostingDto.JobpostingResponse> list = result.getContent().stream()
                .map(JobpostingDto.JobpostingResponse::from).toList();
        return JobpostingDto.JobpostingPageResponse.of(list, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public JobpostingDto.JobpostingListResponse readByWriter(Long writerId) {
        return JobpostingDto.JobpostingListResponse.of(
                jobpostingRepository.findByUserIdOrderByCreatedAtDesc(writerId).stream()
                        .map(JobpostingDto.JobpostingResponse::from).toList()
        );
    }

    private void validateOwner(Jobposting jobposting, Long userId) {
        if (!jobposting.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 채용공고만 수정/삭제할 수 있습니다");
        }
    }

    private String toJson(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return "[\"" + String.join("\",\"", skills) + "\"]";
    }
}
