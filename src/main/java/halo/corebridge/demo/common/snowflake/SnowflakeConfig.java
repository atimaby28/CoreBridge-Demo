package halo.corebridge.demo.common.snowflake;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Bean
    public Snowflake snowflake() {
        // 데모용: 단일 노드이므로 고정 nodeId
        return new Snowflake(1);
    }
}
