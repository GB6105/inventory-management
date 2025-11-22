package gb6105.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    private static final String AWS_IP_ADDRESS = "3.38.114.206"

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 엔드포인트에 대해 CORS를 적용
                        .allowedOrigins("http://127.0.0.1:5173", "http://localhost:5173",
                                "http://" + AWS_IP_ADDRESS + ":5173") // 허용할 출처
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 메서드
                        .allowedHeaders("*") // 모든 헤더를 허용
                        .allowCredentials(true) // 자격 증명을 허용
                        .maxAge(3600); // Preflight 결과를 1시간 동안 캐시
            }
        };
    }
}