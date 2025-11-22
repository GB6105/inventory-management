package gb6105.inventory.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@DependsOn("redisConnectionFactory")
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    private static final String REDISSON_HOST_PREFIX = "redis://";

//    @Bean
//    public RedissonClient redissonClient() {
//        RedissonClient redisson;
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
//        redisson = Redisson.create(config);
//        return redisson;
//    }

    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redisson;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort)
                .setPassword(redisPassword);
        redisson = Redisson.create(config);
        return redisson;
    }

    private final RedisConnectionFactory connectionFactory;

    // @RequiredArgsConstructor를 사용하지 않았으므로 생성자로 주입합니다.
    public RedissonConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * 애플리케이션 시작 후 Redis의 모든 DB를 비웁니다. (테스트/개발 환경에 유용)
     */
    @PostConstruct
    public void onStartupFlushAll() {
        System.out.println("[Redis Config] 애플리케이션 시작 시 Redis FLUSHALL 명령을 실행합니다.");
        try {
            connectionFactory.getConnection().flushAll();
            System.out.println("✅ [Redis Config] Redis FLUSHALL 완료 (시작).");
        } catch (Exception e) {
            System.err.println("❌ [Redis Config] Redis FLUSHALL 실패 (시작): " + e.getMessage());
        }
    }

    /**
     * 애플리케이션 종료 직전 Redis의 모든 DB를 비웁니다.
     */
    @PreDestroy
    public void onShutdownFlushAll() {
        System.out.println("[Redis Config] 애플리케이션 종료 직전 Redis FLUSHALL 명령을 실행합니다.");
        try {
            connectionFactory.getConnection().flushAll();
            System.out.println("✅ [Redis Config] Redis FLUSHALL 완료 (종료).");
        } catch (Exception e) {
            System.err.println("❌ [Redis Config] Redis FLUSHALL 실패 (종료): " + e.getMessage());
        }
    }
}
