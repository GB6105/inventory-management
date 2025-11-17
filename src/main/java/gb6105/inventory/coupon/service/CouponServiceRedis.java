package gb6105.inventory.coupon.service;

import gb6105.inventory.coupon.domain.CouponIssueHistory.IssueStatus;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceRedis {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisStockService redisStockService;
    private final RedissonClient redissonClient;
    private final CouponService couponService;

    private static final String COUPON_ISSUE_QUEUE_KEY = "coupon:issue:queue";
    private static final String COUPON_LOCK_PREFIX = "lock:coupon:";

    private final AtomicBoolean isWorkerRunning = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void startQueueWorker() {
        // 별도의 스레드를 생성하여 무한 루프를 백그라운드에서 실행합니다.
        if (isWorkerRunning.compareAndSet(false, true)) {
            new Thread(this::processCouponQueue).start();
            System.out.println("✅ Coupon Queue Worker가 백그라운드에서 시작되었습니다.");
        } else {
            System.out.println("⚠️ Coupon Queue Worker는 이미 실행 중입니다. 중복 실행을 막았습니다.");
        }
    }

    // 큐 모니터링: 별도의 스레드/태스크로 실행되어야 함.
    public void processCouponQueue() {
        while (true) {
            try {
                // BLPOP (Blocking Left Pop): 큐에 메시지가 들어올 때까지 대기
                String message = redisTemplate.opsForList()
                        .leftPop(COUPON_ISSUE_QUEUE_KEY, 10, TimeUnit.SECONDS);
                if (message != null) {
                    String[] parts = message.split(":");
                    String email = parts[0];
                    Long couponId = Long.parseLong(parts[1]);

                    // 순서대로 가져온 요청을 처리합니다.
                    issueCouponSequentially(email, couponId);
                }
            } catch (Exception e) {
                // 로그 기록
            }
        }
    }

    // 순서대로 처리되는 핵심 로직 (락 필요 없음: DECR이 원자적)
    private void issueCouponSequentially(String email, Long couponId) {

        String lockName = COUPON_LOCK_PREFIX + couponId;
        RLock lock = redissonClient.getLock(lockName);
        long waitTime = 1; // 락 획득 대기 시간 (짧게 설정)
        long leastTime = 3; // 락 유지 시간 (트랜잭션 시간보다 길게 설정)
        TimeUnit time = TimeUnit.SECONDS;

        try {
            // 1. 락 획득 시도 (Redisson Lock)
            boolean isLocked = lock.tryLock(waitTime, leastTime, time);

            if (!isLocked) {
                // 락 획득 실패 (동시에 여러 워커가 실행되거나, 이전 락이 만료 안됐을 때)
                couponService.saveIssueResult(email, couponId, IssueStatus.FAIL.getMessage());
                System.out.println("락 획득 실패: " + email);
                return;
            }

            // 2. 락 획득 성공: DB 트랜잭션 실행
            // CouponService의 issueCoupon 메서드 호출 (DB 재고 확인 및 감소)
            couponService.issueCoupon(email, couponId);
            System.out.println("✅ 순서 처리 성공: " + email + " | 락 해제");

        } catch (IllegalStateException | IllegalArgumentException e) {
            // DB 로직에서 발생한 오류 (재고 소진, 중복 발급)는 이미 DB에 FAIL 기록됨
            System.out.println("처리 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            // 인터럽트 발생 시 실패 기록 (선택적)
            couponService.saveIssueResult(email, couponId, IssueStatus.FAIL.getMessage());
            System.out.println("락 획득 중 인터럽트");
        } finally {
            // 3. 락 해제 (필수)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
