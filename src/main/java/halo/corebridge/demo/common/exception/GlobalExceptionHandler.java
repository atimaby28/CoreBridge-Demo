package halo.corebridge.demo.common.exception;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.common.response.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        log.warn("BaseException: {}", e.getMessage());
        return ResponseEntity
                .status(mapToHttpStatus(e.getStatus()))
                .body(BaseResponse.failure(e.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.failure(BaseResponseStatus.INVALID_REQUEST.getCode(), errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.failure(BaseResponseStatus.INVALID_REQUEST.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.failure(BaseResponseStatus.INVALID_REQUEST.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.failure(BaseResponseStatus.INTERNAL_SERVER_ERROR));
    }

    private HttpStatus mapToHttpStatus(BaseResponseStatus status) {
        return switch (status) {
            case UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, NOT_JOBPOSTING_OWNER, ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND, USER_NOT_FOUND, JOBPOSTING_NOT_FOUND,
                 APPLICATION_NOT_FOUND, PROCESS_NOT_FOUND,
                 RESUME_NOT_FOUND, SCHEDULE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_REQUEST, INVALID_PASSWORD, DUPLICATE_EMAIL,
                 USER_ALREADY_EXISTS, ALREADY_APPLIED,
                 INVALID_STATUS_TRANSITION, SCHEDULE_CONFLICT,
                 INVALID_TIME_RANGE, CANNOT_CANCEL_IN_PROGRESS -> HttpStatus.BAD_REQUEST;
            case JOBPOSTING_CLOSED, APPLICATION_CLOSED -> HttpStatus.GONE;
            case PROCESS_ALREADY_COMPLETED -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
