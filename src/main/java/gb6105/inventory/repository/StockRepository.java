package gb6105.inventory.repository;

import gb6105.inventory.domain.Stock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {
    /**
     * @param id 조호할 Stock의 ID
     * @retrun 락이 걸린 Stock 엔티티
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // hibernate에서 select for update 구문으로 변환하여 실행
    @Query("SELECT s FROM Stock s where s.id = :id")
    // 엔티티 조회 대상 명확 + 락을 걸 대상 지정
    Optional<Stock> findByIdWithPessimisticLock(@Param("id") Long id);

}
