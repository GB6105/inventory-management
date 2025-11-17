package gb6105.inventory.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String name;

    private int quantity;

    /**
     * @param name 쿠폰 이름
     * @param quantity 총 수량
     */
    public Coupon(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public void decreaseQuantity() {
        if(this.quantity <= 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        quantity--;
    }

}
