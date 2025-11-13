package gb6105.inventory.lockTest.service;

import gb6105.inventory.lockTest.domain.Stock;
import gb6105.inventory.lockTest.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final RedissonClient redissonClient;


    @Transactional
    public void decreaseStock(Long id, Long quantity){
        // Stock 조회 -> 동시성 문제 발생 포인트
        Stock stock = stockRepository.findById(id).orElseThrow();
        // 재고 감소
        stock.decrease(quantity);
        // 갱신된 값 저장

    }

    @Transactional
    public void decreaseStockWithLock(Long id, Long quantity){
        // Stock 조회 -> 비관적 락 적용
        Stock stock = stockRepository.findByIdWithPessimisticLock(id).orElseThrow(
                () -> new RuntimeException("Stock not found")
        );
        // 재고 감소
        stock.decrease(quantity);
        // 갱신된 값 저장
    }


}
