package halo.corebridge.demo.security;

import halo.corebridge.demo.domain.user.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** Access Token 생성 */
    public String createAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpiration()))
                .signWith(secretKey)
                .compact();
    }

    /** Refresh Token 생성 */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration()))
                .signWith(secretKey)
                .compact();
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 토큰: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("잘못된 서명: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("빈 토큰: {}", e.getMessage());
        }
        return false;
    }

    /** Claims 추출 */
    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }

    /** userId 추출 */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /** email 추출 */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /** role 추출 */
    public UserRole getRole(String token) {
        return UserRole.valueOf(getClaims(token).get("role", String.class));
    }

    /** Refresh Token 만료 시간 (ms) */
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }
}
