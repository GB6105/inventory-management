package gb6105.inventory.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String COUNPON_STOCK_PREFIX = "coupon:stock:";

    public void initializeStock(Long couponId, int quantity) {
        redisTemplate.opsForValue().set(COUNPON_STOCK_PREFIX + couponId, String.valueOf(quantity));
    }

    public Long decreaseStock(Long couponId) {
        return redisTemplate.opsForValue().decrement(COUNPON_STOCK_PREFIX + couponId);
    }

    public Long increaseStock(Long couponId) {
        return redisTemplate.opsForValue().increment(COUNPON_STOCK_PREFIX + couponId);
    }
}
