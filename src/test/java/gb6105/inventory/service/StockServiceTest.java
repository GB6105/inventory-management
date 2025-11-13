package gb6105.inventory.service;

import static org.junit.jupiter.api.Assertions.*;

import gb6105.inventory.lockTest.domain.Stock;
import gb6105.inventory.lockTest.repository.StockRepository;
import gb6105.inventory.lockTest.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        stockRepository.saveAndFlush(new Stock(1l, 100l));
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소(){
        stockService.decreaseStock(1l,1l);

        Stock stock = stockRepository.findById(1l).orElseThrow();

        assertEquals(stock.getQuantity(),99);
    }
}