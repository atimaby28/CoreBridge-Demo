package halo.corebridge.demo.domain.schedule.dto;

import halo.corebridge.demo.domain.schedule.entity.Schedule;
import halo.corebridge.demo.domain.schedule.enums.ScheduleStatus;
import halo.corebridge.demo.domain.schedule.enums.ScheduleType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleDto {

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotNull private Long applyId;
        @NotNull private Long jobpostingId;
        @NotNull private Long userId;
        @NotNull private ScheduleType type;
        @NotBlank private String title;
        private String description;
        private String location;
        @NotNull @Future private LocalDateTime startTime;
        @NotNull @Future private LocalDateTime endTime;
        private Long interviewerId;
        private String interviewerName;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        @NotBlank private String title;
        private String description;
        private String location;
        @NotNull private LocalDateTime startTime;
        @NotNull private LocalDateTime endTime;
        private Long interviewerId;
        private String interviewerName;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull private ScheduleStatus status;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateMemoRequest {
        private String memo;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long applyId;
        private Long jobpostingId;
        private Long userId;
        private Long companyId;
        private ScheduleType type;
        private String typeDescription;
        private String title;
        private String description;
        private String location;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long interviewerId;
        private String interviewerName;
        private ScheduleStatus status;
        private String statusDescription;
        private String memo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Schedule s) {
            return Response.builder()
                    .id(s.getId()).applyId(s.getApplyId())
                    .jobpostingId(s.getJobpostingId()).userId(s.getUserId())
                    .companyId(s.getCompanyId()).type(s.getType())
                    .typeDescription(s.getType().getDescription())
                    .title(s.getTitle()).description(s.getDescription())
                    .location(s.getLocation())
                    .startTime(s.getStartTime()).endTime(s.getEndTime())
                    .interviewerId(s.getInterviewerId())
                    .interviewerName(s.getInterviewerName())
                    .status(s.getStatus())
                    .statusDescription(s.getStatus().getDescription())
                    .memo(s.getMemo())
                    .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
                    .build();
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CalendarEventResponse {
        private String id;
        private String title;
        private String start;
        private String end;
        private String color;
        private String backgroundColor;
        private String borderColor;
        private String textColor;
        private boolean allDay;
        private ExtendedProps extendedProps;

        @Getter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class ExtendedProps {
            private Long scheduleId;
            private Long applyId;
            private Long jobpostingId;
            private Long userId;
            private ScheduleType type;
            private String typeDescription;
            private ScheduleStatus status;
            private String statusDescription;
            private String location;
            private String description;
        }

        public static CalendarEventResponse from(Schedule s) {
            String color = getColorByType(s.getType());
            return CalendarEventResponse.builder()
                    .id(String.valueOf(s.getId())).title(s.getTitle())
                    .start(s.getStartTime().toString()).end(s.getEndTime().toString())
                    .color(color).backgroundColor(color).borderColor(color).textColor("#ffffff")
                    .allDay(false)
                    .extendedProps(ExtendedProps.builder()
                            .scheduleId(s.getId()).applyId(s.getApplyId())
                            .jobpostingId(s.getJobpostingId()).userId(s.getUserId())
                            .type(s.getType()).typeDescription(s.getType().getDescription())
                            .status(s.getStatus()).statusDescription(s.getStatus().getDescription())
                            .location(s.getLocation()).description(s.getDescription())
                            .build())
                    .build();
        }

        private static String getColorByType(ScheduleType type) {
            return switch (type) {
                case CODING_TEST -> "#8B5CF6";
                case INTERVIEW_1 -> "#3B82F6";
                case INTERVIEW_2 -> "#10B981";
                case FINAL_INTERVIEW -> "#F59E0B";
                case ORIENTATION -> "#EC4899";
                case OTHER -> "#6B7280";
            };
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConflictCheckResponse {
        private boolean hasConflict;
        private List<ConflictDetail> conflicts;

        @Getter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class ConflictDetail {
            private String type;
            private Long scheduleId;
            private String title;
            private LocalDateTime startTime;
            private LocalDateTime endTime;
            private String message;
        }
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ListResponse {
        private List<Response> schedules;
        private long totalCount;
        private long upcomingCount;
        private long completedCount;

        public static ListResponse of(List<Response> list, long upcoming, long completed) {
            return ListResponse.builder()
                    .schedules(list).totalCount(list.size())
                    .upcomingCount(upcoming).completedCount(completed).build();
        }
    }
}
