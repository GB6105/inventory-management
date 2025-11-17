package gb6105.inventory.coupon.service;

import gb6105.inventory.coupon.repository.CouponIssueHistoryRepository;
import gb6105.inventory.coupon.repository.CouponRepository;
import gb6105.inventory.coupon.repository.MemberRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CouponServiceRedisson {
    private final CouponService couponService;
    private final RedissonClient redissonClient;

    private static final String COUPON_LOCK_PREFIX = "lock:coupon";

    public void issueCouponWithRedisson(String email, Long couponId){
        RLock lock = redissonClient.getLock(COUPON_LOCK_PREFIX + couponId);
        long waitTime = 3l;
        long leastTime = 1l;
        TimeUnit time = TimeUnit.SECONDS;

        try{
            boolean isLocked = lock.tryLock(waitTime,leastTime,time);
            if(!isLocked){
                throw new IllegalStateException("락 획득 실패 : 서버 혼잡");
            }
            couponService.issueCoupon(email, couponId);

        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }finally{
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
