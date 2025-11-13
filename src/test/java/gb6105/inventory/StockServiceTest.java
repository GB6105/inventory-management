package gb6105.inventory;


import static org.assertj.core.api.Assertions.assertThat;

import gb6105.inventory.concurrency.domain.Stock;
import gb6105.inventory.concurrency.domain.StockAtomic;
import gb6105.inventory.concurrency.domain.StockLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StockServiceTest {

    @Test
    @DisplayName("단일 스레드에서 기본적인 재고 감소 테스트")
    public void decreaseStockTest() {
        // given
        int decreaseAmount = 1;
        Stock stock1 = new Stock(1l, "stock1", 100);

        // when
        stock1.decrease(decreaseAmount);

        // then
        assertThat(stock1.getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("동시성 문제 발생 확인 : 재고 감소 테스트")
    public void decreaseMultipleStockTest() throws InterruptedException {
        // given
        int initialQuantity = 100;
        int threadNumber = 100;
        int threadDecreaseAmount = 1;
        Stock stock1 = new Stock(1l, "stock1", initialQuantity);
        Thread[] threads = new Thread[threadNumber];

        // when
        for (int i = 0; i < threadNumber; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < threadNumber; j++) {
                    stock1.decrease(threadDecreaseAmount);
                }
            });
            threads[i] = t;
            t.start();
        }
        // 스레드 종료
        for (Thread t : threads) {
            t.join();
        }
        // then
        int totalDecreaseAmount = threadNumber * threadDecreaseAmount;
        int expectedQuantity = initialQuantity - totalDecreaseAmount;
        System.out.println("최종 재고 : " + stock1.getQuantity());
        assertThat(stock1.getQuantity()).as("동시 접근으로 인한 예상치 못한 값 발생")
                .isNotEqualTo(expectedQuantity)
                .isLessThanOrEqualTo(0);

    }

    @Test
    @DisplayName("동시성 문제 발생 확인 : 재고 증가 테스트")
    public void increaseMultipleStockTest() throws InterruptedException {
        // given
        int initialQuantity = 0;
        int threadNumber = 100;
        int decreaseAmount = 100;
        Stock stock2 = new Stock(2l, "stock2", initialQuantity);

        // when
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threadNumber; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < decreaseAmount; j++) {
                    stock2.increase();
                }
            });
            threads[i] = t;
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // then
        int expectedQuantity = threadNumber * decreaseAmount;

        System.out.println("최종 재고 (증가량) : " + stock2.getQuantity());
        assertThat(stock2.getQuantity()).as("동시성 문제로 예상치 못한 수량 ")
                .isLessThanOrEqualTo(expectedQuantity);
    }

    @Test
    @DisplayName("동시성 문제 해결 Lock : 재고 증가 테스트")
    public void increaseMultipleStockTest2() throws InterruptedException {
        // given
        int initialQuantity = 0;
        int threadNumber = 100;
        int increaseAmount = 100;
        long startTime = System.currentTimeMillis();
        StockLock stockLock = new StockLock(3l, "stock3", initialQuantity);

        // when
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threadNumber; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < increaseAmount; j++) {
                    stockLock.increase();
                }
            });
            threads[i] = t;
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // then
        int expectedQuantity = threadNumber * increaseAmount;
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("최종 재고 (증가량) : " + stockLock.getQuantity());
        System.out.println("Lock 사용 시 실행 시간 " + duration + "m/sec" );
        assertThat(stockLock.getQuantity()).isEqualTo(expectedQuantity);
    }

    @Test
    @DisplayName("동시성 문제 해결 AtomicInteger : 재고 증가 테스트")
    public void increaseMultipleStockTest3() throws InterruptedException {
        // given
        int initialQuantity = 0;
        int threadNumber = 100;
        int increaseAmount = 100;
        long startTime = System.currentTimeMillis();
        StockAtomic stockLock = new StockAtomic(3l, "stock");

        // when
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threadNumber; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < increaseAmount; j++) {
                    stockLock.increase();
                }
            });
            threads[i] = t;
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // then
        int expectedQuantity = threadNumber * increaseAmount;
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("최종 재고 (증가량) : " + stockLock.getQuantity());
        System.out.println("AtomicInteger 도입 시 실행 시간 " + duration + "m/sec" );
        assertThat(stockLock.getQuantity()).isEqualTo(expectedQuantity);
    }
}
