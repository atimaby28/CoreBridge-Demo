package halo.corebridge.demo.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final T result;

    private BaseResponse(BaseResponseStatus status, T result) {
        this.isSuccess = status.isSuccess();
        this.code = status.getCode();
        this.message = status.getMessage();
        this.result = result;
    }

    private BaseResponse(boolean isSuccess, int code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> BaseResponse<T> success(T result) {
        return new BaseResponse<>(BaseResponseStatus.SUCCESS, result);
    }

    public static BaseResponse<Void> success() {
        return new BaseResponse<>(BaseResponseStatus.SUCCESS, null);
    }

    public static <T> BaseResponse<T> success(String message, T result) {
        return new BaseResponse<>(true, 1000, message, result);
    }

    public static BaseResponse<Void> failure(BaseResponseStatus status) {
        return new BaseResponse<>(status, null);
    }

    public static BaseResponse<Void> failure(int code, String message) {
        return new BaseResponse<>(false, code, message, null);
    }

    public static <T> BaseResponse<T> failure(BaseResponseStatus status, T result) {
        return new BaseResponse<>(status, result);
    }
}
