package halo.corebridge.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA (Single Page Application) 라우팅 지원
 *
 * Vue Router의 History Mode에서 /auth/login, /dashboard 등
 * 프론트엔드 라우트로 직접 접근 시 index.html을 반환합니다.
 *
 * API 경로(/api/**), H2 Console(/h2-console/**), Actuator(/actuator/**)는
 * 백엔드에서 처리합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        // 실제 파일이 존재하면 해당 파일 반환 (JS, CSS, 이미지 등)
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // 존재하지 않으면 index.html 반환 (SPA 라우팅)
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
