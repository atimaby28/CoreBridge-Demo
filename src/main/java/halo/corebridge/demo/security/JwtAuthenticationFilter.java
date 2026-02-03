package halo.corebridge.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 모놀리식 데모용 JWT 인증 필터
 *
 * MSA에서는 API Gateway가 JWT 검증 후 X-User-Id 헤더로 전달하지만,
 * 데모에서는 직접 Cookie에서 accessToken 추출 → 검증 → SecurityContext 설정
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserIdFromToken(token);
            String role = jwtProvider.getRole(token).name();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,                      // principal → @AuthenticationPrincipal Long userId
                            null,                        // credentials
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT 인증 완료: userId={}, role={}", userId, role);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 토큰 추출 우선순위:
     * 1. Cookie (accessToken)
     * 2. Authorization 헤더 (Bearer ...)
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Cookie에서 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Authorization 헤더에서 추출 (Swagger/Postman 테스트용)
        String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
