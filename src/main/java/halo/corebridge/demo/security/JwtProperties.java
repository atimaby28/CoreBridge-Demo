package halo.corebridge.demo.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "corebridge-demo-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private long accessTokenExpiration = 1800000;   // 30분 (ms)
    private long refreshTokenExpiration = 604800000; // 7일 (ms)
}
