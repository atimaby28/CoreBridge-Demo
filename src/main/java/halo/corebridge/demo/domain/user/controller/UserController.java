package halo.corebridge.demo.domain.user.controller;

import halo.corebridge.demo.common.response.BaseResponse;
import halo.corebridge.demo.domain.user.dto.UserDto;
import halo.corebridge.demo.domain.user.enums.UserRole;
import halo.corebridge.demo.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final long ACCESS_TOKEN_MAX_AGE = 30 * 60;       // 30분
    private static final long REFRESH_TOKEN_MAX_AGE = 7 * 24 * 3600; // 7일

    // ============================================
    // 인증 API (Public)
    // ============================================

    @PostMapping("/signup")
    public BaseResponse<UserDto.UserResponse> signup(@Valid @RequestBody UserDto.SignupRequest request) {
        return BaseResponse.success(userService.signup(request));
    }

    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @Valid @RequestBody UserDto.LoginRequest request,
            HttpServletResponse response) {
        UserDto.LoginResponse loginResponse = userService.login(request);
        setTokenCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
        return BaseResponse.success(loginResponse.withoutTokens());
    }

    @PostMapping("/refresh")
    public BaseResponse<UserDto.TokenResponse> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }
        UserDto.TokenResponse tokenResponse = userService.refresh(new UserDto.RefreshRequest(refreshToken));
        setTokenCookies(response, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
        return BaseResponse.success(tokenResponse.withoutTokens());
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(@AuthenticationPrincipal Long userId, HttpServletResponse response) {
        userService.logout(userId);
        clearTokenCookies(response);
        return BaseResponse.success();
    }

    // ============================================
    // 사용자 API (Authenticated)
    // ============================================

    @GetMapping("/me")
    public BaseResponse<UserDto.UserResponse> getMe(@AuthenticationPrincipal Long userId) {
        return BaseResponse.success(userService.getMe(userId));
    }

    @PutMapping("/me")
    public BaseResponse<UserDto.UserResponse> updateMe(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        return BaseResponse.success(userService.updateUser(userId, request));
    }

    @DeleteMapping("/me")
    public BaseResponse<Void> deleteMe(@AuthenticationPrincipal Long userId) {
        userService.deleteUser(userId);
        return BaseResponse.success();
    }

    @GetMapping("/{userId}")
    public BaseResponse<UserDto.UserResponse> getUser(@PathVariable Long userId) {
        return BaseResponse.success(userService.getUser(userId));
    }

    // ============================================
    // Admin API
    // ============================================

    @GetMapping("/admin/list")
    public BaseResponse<UserDto.UserPageResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return BaseResponse.success(userService.getAllUsers(page, size));
    }

    @GetMapping("/admin/list/role/{role}")
    public BaseResponse<UserDto.UserPageResponse> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return BaseResponse.success(userService.getUsersByRole(role, page, size));
    }

    @GetMapping("/admin/search")
    public BaseResponse<UserDto.UserPageResponse> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return BaseResponse.success(userService.searchUsers(keyword, page, size));
    }

    @GetMapping("/admin/{userId}")
    public BaseResponse<UserDto.AdminUserResponse> getAdminUserDetail(@PathVariable Long userId) {
        return BaseResponse.success(userService.getAdminUserDetail(userId));
    }

    @PatchMapping("/admin/{userId}/role")
    public BaseResponse<UserDto.AdminUserResponse> updateRole(
            @PathVariable Long userId, @RequestBody UserDto.RoleUpdateRequest request) {
        return BaseResponse.success(userService.updateRole(userId, request));
    }

    @PatchMapping("/admin/{userId}/status")
    public BaseResponse<UserDto.AdminUserResponse> updateStatus(
            @PathVariable Long userId, @RequestBody UserDto.StatusUpdateRequest request) {
        return BaseResponse.success(userService.updateStatus(userId, request));
    }

    @GetMapping("/admin/stats")
    public BaseResponse<UserDto.UserStatsResponse> getUserStats() {
        return BaseResponse.success(userService.getUserStats());
    }

    // ============================================
    // Cookie 유틸리티
    // ============================================

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true).secure(false).sameSite("Lax").path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE).build();
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true).secure(false).sameSite("Lax").path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE).build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie access = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
        ResponseCookie refresh = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, access.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
