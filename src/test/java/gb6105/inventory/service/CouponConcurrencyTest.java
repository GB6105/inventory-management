package gb6105.inventory.service;

import gb6105.inventory.coupon.service.CouponService;
import gb6105.inventory.coupon.service.CouponServiceRedisson;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CouponConcurrencyTest {

    @Autowired
    private CouponServiceRedisson couponServiceRedisson;

    @Autowired
    private CouponService couponService; // issueCouponWithPessimisticLock 메서드를 포함

    private static final int THREAD_COUNT = 100; // 동시 실행 스레드 수
    private static final Long TEST_COUPON_ID = 1L;

    @BeforeEach
    void setUp() {
    }


    @Test
    void pessimisticLockConcurrencyTest() throws InterruptedException {

        System.out.println("--- 비관적 락 테스트 시작 ---");

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        // 고정된 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // 사용자 이메일을 다르게 하여 중복 발급 체크 로직을 피함
                    String email = String.format("user_%03d@test.com", index);
                    couponService.issueCouponWithPessimisticLock(email, TEST_COUPON_ID);
                } catch (Exception e) {
                    // 성공 또는 실패 (예: 재고 소진, 이미 발급됨) 처리
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 완료될 때까지 대기
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("비관적 락 - 총 실행 시간: " + duration + "ms");

        executorService.shutdown();

    }

    // Redisson 분산 락 (Distributed Lock) 성능 테스트

    @Test
    void redissonLockConcurrencyTest() throws InterruptedException {

        System.out.println("--- Redisson 분산 락 테스트 시작 ---");

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String email = String.format("user_%03d@test.com", index);
                    // issueCouponWithRedisson이 내부에서 issueCoupon을 호출합니다.
                    couponServiceRedisson.issueCouponWithRedisson(email, TEST_COUPON_ID);
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("Redisson 분산 락 - 총 실행 시간: " + duration + "ms");

        executorService.shutdown();

    }
}
