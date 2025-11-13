package gb6105.inventory.lockTest.facade;

import gb6105.inventory.lockTest.repository.StockRepository;
import gb6105.inventory.lockTest.service.StockService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockFacade {
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final RedissonClient redissonClient;

    public void decreaseStockWithRedis(Long id, Long quantity){
        RLock lock = redissonClient.getLock(String.format("stockId : %d", id));
        long waitTime = 10; // 획득 대기 시간
        long leaseTime = 1; // 대여 시간
        try{
            boolean available = lock.tryLock(waitTime,leaseTime, TimeUnit.SECONDS);
            if(!available){
                System.out.println("redisson lock timeout");
                throw new RuntimeException("redisson lock timeout");
            }
            // Stock 조회
            stockService.decreaseStock(id, quantity);

        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }finally{
            lock.unlock();
        }
    }
}
