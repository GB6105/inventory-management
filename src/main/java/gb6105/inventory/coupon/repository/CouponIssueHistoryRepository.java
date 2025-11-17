package gb6105.inventory.coupon.repository;

import gb6105.inventory.coupon.domain.CouponIssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueHistoryRepository extends JpaRepository<CouponIssueHistory, Long> {
    boolean existsByMemberEmailAndCouponId(String memberEmail, Long couponId);
}
