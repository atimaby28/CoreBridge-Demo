package halo.corebridge.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))  // H2 Console
                .authorizeHttpRequests(auth -> auth
                        // ===== 공개 API =====
                        // 인증
                        .requestMatchers("/api/v1/users/signup", "/api/v1/users/login", "/api/v1/users/refresh").permitAll()

                        // 채용공고 조회 (비로그인 가능)
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobpostings/**").permitAll()

                        // 댓글 조회 (비로그인 가능)
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()

                        // State Machine 메타 정보
                        .requestMatchers(HttpMethod.GET, "/api/v1/processes/steps").permitAll()

                        // Mock: Read(BFF), View count, Like count, Hot (GET은 공개)
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobposting-read/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobposting-views/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/hot-jobpostings/**").permitAll()

                        // ===== 프론트엔드 & 개발도구 =====
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // ===== Admin API =====
                        .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ===== 나머지는 인증 필요 =====
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
