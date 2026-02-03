package halo.corebridge.demo.domain.user.service;

import halo.corebridge.demo.common.snowflake.Snowflake;
import halo.corebridge.demo.domain.user.dto.UserDto;
import halo.corebridge.demo.domain.user.entity.RefreshToken;
import halo.corebridge.demo.domain.user.entity.User;
import halo.corebridge.demo.domain.user.enums.UserRole;
import halo.corebridge.demo.domain.user.enums.UserStatus;
import halo.corebridge.demo.domain.user.repository.RefreshTokenRepository;
import halo.corebridge.demo.domain.user.repository.UserRepository;
import halo.corebridge.demo.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final Snowflake snowflake;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // ============================================
    // 인증 API
    // ============================================

    @Transactional
    public UserDto.UserResponse signup(UserDto.SignupRequest request) {
        if (!request.isAllowedRole()) {
            throw new IllegalArgumentException("관리자 계정은 직접 가입할 수 없습니다");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserRole role = request.getRole() != null ? request.getRole() : UserRole.ROLE_USER;

        User user = User.create(snowflake.nextId(), request.getEmail(),
                request.getNickname(), encodedPassword, role);
        userRepository.save(user);

        log.info("회원가입 완료: userId={}, email={}, role={}", user.getUserId(), user.getEmail(), role);
        return UserDto.UserResponse.from(user);
    }

    @Transactional
    public UserDto.LoginResponse login(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
        if (!user.isEnabled()) {
            throw new IllegalStateException("비활성화된 계정입니다");
        }

        user.recordLogin();

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        saveOrUpdateRefreshToken(user.getUserId(), refreshToken);

        log.info("로그인 성공: userId={}, email={}", user.getUserId(), user.getEmail());
        return UserDto.LoginResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public UserDto.TokenResponse refresh(UserDto.RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰을 찾을 수 없습니다"));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다");
        }

        Long userId = storedToken.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        storedToken.updateToken(newRefreshToken, jwtProvider.getRefreshTokenExpiration());

        log.info("토큰 갱신 완료: userId={}", userId);
        return UserDto.TokenResponse.builder()
                .accessToken(newAccessToken).refreshToken(newRefreshToken).build();
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("로그아웃 완료: userId={}", userId);
    }

    // ============================================
    // 사용자 API
    // ============================================

    @Transactional(readOnly = true)
    public UserDto.UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return UserDto.UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return UserDto.UserResponse.from(user);
    }

    @Transactional
    public UserDto.UserResponse updateUser(Long userId, UserDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (request.getNickname() != null) user.updateProfile(request.getNickname());
        if (request.getPassword() != null) user.updatePassword(passwordEncoder.encode(request.getPassword()));

        log.info("회원 정보 수정: userId={}", userId);
        return UserDto.UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.markDeleted();
        refreshTokenRepository.deleteByUserId(userId);
        log.info("회원 탈퇴: userId={}", userId);
    }

    // ============================================
    // Admin API
    // ============================================

    @Transactional(readOnly = true)
    public UserDto.UserPageResponse getAllUsers(int page, int size) {
        Page<User> userPage = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        List<UserDto.AdminUserResponse> users = userPage.getContent().stream()
                .map(UserDto.AdminUserResponse::from).toList();
        return UserDto.UserPageResponse.of(users, userPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public UserDto.UserPageResponse getUsersByRole(UserRole role, int page, int size) {
        Page<User> userPage = userRepository.findByRoleOrderByCreatedAtDesc(role, PageRequest.of(page, size));
        List<UserDto.AdminUserResponse> users = userPage.getContent().stream()
                .map(UserDto.AdminUserResponse::from).toList();
        return UserDto.UserPageResponse.of(users, userPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public UserDto.UserPageResponse searchUsers(String keyword, int page, int size) {
        Page<User> userPage = userRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        List<UserDto.AdminUserResponse> users = userPage.getContent().stream()
                .map(UserDto.AdminUserResponse::from).toList();
        return UserDto.UserPageResponse.of(users, userPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public UserDto.AdminUserResponse getAdminUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return UserDto.AdminUserResponse.from(user);
    }

    @Transactional
    public UserDto.AdminUserResponse updateRole(Long userId, UserDto.RoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.changeRole(request.getRole());
        log.info("Role 변경: userId={}, newRole={}", userId, request.getRole());
        return UserDto.AdminUserResponse.from(user);
    }

    @Transactional
    public UserDto.AdminUserResponse updateStatus(Long userId, UserDto.StatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.changeStatus(request.getStatus());
        if (request.getStatus() == UserStatus.BLOCKED) {
            refreshTokenRepository.deleteByUserId(userId);
        }
        log.info("Status 변경: userId={}, newStatus={}", userId, request.getStatus());
        return UserDto.AdminUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserDto.UserStatsResponse getUserStats() {
        return UserDto.UserStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .adminCount(userRepository.countByRole(UserRole.ROLE_ADMIN))
                .companyCount(userRepository.countByRole(UserRole.ROLE_COMPANY))
                .userCount(userRepository.countByRole(UserRole.ROLE_USER))
                .build();
    }

    // ============================================
    // Private
    // ============================================

    private void saveOrUpdateRefreshToken(Long userId, String token) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.updateToken(token, jwtProvider.getRefreshTokenExpiration()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.create(token, userId, jwtProvider.getRefreshTokenExpiration())
                        )
                );
    }
}
