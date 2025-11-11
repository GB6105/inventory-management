package gb6105.inventory.Controller;

import gb6105.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    @PostMapping({"/stockId}/decrease"})
    public ResponseEntity<Void> decreaseStock(@PathVariable Long stockId, @RequestParam Long quantity) {
        try{
            stockService.decreaseStock(stockId, quantity);
            return ResponseEntity.ok().build();
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
    }

}
