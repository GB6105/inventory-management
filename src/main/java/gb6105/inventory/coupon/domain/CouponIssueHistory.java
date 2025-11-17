package gb6105.inventory.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon_issue_history")
@NoArgsConstructor
public class CouponIssueHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long issueId;

    @JoinColumn(name = "member_email", nullable = false) // DB 칼럼과 연관성
    private String memberEmail;

    @JoinColumn(name = "coupon_id", nullable = false)
    private Long couponId;

    private String status;

    @Getter
    public enum IssueStatus {
        SUCCESS("success"), FAIL("fail");

        private String message;
        IssueStatus(String message) {
            this.message =  message;
        }
    }

    /**
     * @param memberEmail 멤버 이메일
     * @param couponId 쿠폰 아이디
     * @param status 발급 상태
     */
    public CouponIssueHistory(String memberEmail, Long couponId, String status) {
        this.memberEmail = memberEmail;
        this.couponId = couponId;
        this.status = status;
    }

    public CouponIssueHistory(String memberEmail, Long couponId) {}

}
