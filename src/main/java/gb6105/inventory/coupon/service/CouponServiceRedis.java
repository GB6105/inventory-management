package gb6105.inventory.coupon.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class CouponServiceRedis {

    // DB 트랜잭션 및 발급 로직을 가진 서비스
    private final CouponService couponService;
    private final RedissonClient redissonClient;

    private static final String ISSUED_MEMBER_SET_PREFIX = "coupon:issued:";
    private static final String COUPON_LOCK_PREFIX = "lock:coupon";

    @Transactional
    public void issueCouponWithRedis(String email, Long couponId) {
        String key = ISSUED_MEMBER_SET_PREFIX + couponId;
        RSet<String> issuedMembers = redissonClient.getSet(key);

        if(issuedMembers.contains(email)) {
            System.out.println("이미 쿠폰을 발급 받았습니다.");
            throw new IllegalStateException("이미 쿠폰을 발급 받았습니다.");
        }
        // 순수 쿠폰 발급 로직 호출

        RLock lock = redissonClient.getLock(COUPON_LOCK_PREFIX + couponId);
        long waitTime = 3l;
        long leastTime = 1l;
        TimeUnit time = TimeUnit.SECONDS;

        try{
            boolean isLocked = lock.tryLock(waitTime,leastTime,time);
            if(!isLocked){
                throw new IllegalStateException("락 획득 실패 : 서버 혼잡");
            }
            couponService.issueCouponCore(email, couponId);

        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }finally{
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        issuedMembers.add(email);
    }
}
