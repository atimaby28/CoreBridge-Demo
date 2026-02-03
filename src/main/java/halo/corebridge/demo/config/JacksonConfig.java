package halo.corebridge.demo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 직렬화 설정
 *
 * Snowflake ID(Long)가 JS Number.MAX_SAFE_INTEGER(2^53-1)를 초과하므로
 * 큰 Long 값만 String으로 직렬화합니다.
 *
 * ⚠️ ObjectMapper를 @Bean으로 직접 생성하면 Spring Boot의 자동 설정
 *    (JSR310 LocalDateTime 등)이 무시되므로, Customizer 방식을 사용합니다.
 */
@Configuration
public class JacksonConfig {

    private static final long JS_MAX_SAFE_INTEGER = 9007199254740991L;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer snowflakeIdCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("SnowflakeIdModule");
            module.addSerializer(Long.class, new SafeLongSerializer());
            module.addSerializer(Long.TYPE, new SafeLongSerializer());
            builder.modulesToInstall(module);
        };
    }

    /**
     * JS Number.MAX_SAFE_INTEGER를 초과하는 Long만 String으로 직렬화.
     * 작은 값(count, page 등)은 숫자 그대로 유지.
     */
    static class SafeLongSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else if (value > JS_MAX_SAFE_INTEGER || value < -JS_MAX_SAFE_INTEGER) {
                gen.writeString(value.toString());
            } else {
                gen.writeNumber(value);
            }
        }
    }
}
