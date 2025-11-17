package gb6105.inventory.coupon.service;


import gb6105.inventory.coupon.domain.Coupon;
import gb6105.inventory.coupon.domain.CouponIssueHistory;
import gb6105.inventory.coupon.domain.CouponIssueHistory.IssueStatus;
import gb6105.inventory.coupon.repository.CouponRepository;
import gb6105.inventory.coupon.repository.CouponIssueHistoryRepository;
import gb6105.inventory.coupon.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueHistoryRepository historyRepository;

    @Transactional
    // lock을 사용하지 않은 DB 메서드 -> 동시성 이슈 발생 확인 용
    public void issueCoupon(String email, Long id) {

        // 회원 정보 조회
        String memberEmail = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(String.format("회원을 찾을 수 없습니다. %s", email)))
                .getEmail();

        // 쿠폰 정보 조회
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("쿠폰 정보를 찾을 수 없습니다. ID: %d", id)));

        Long couponId = coupon.getId();

        // 쿠폰 발급 이력 조회
        boolean isIssued = historyRepository.existsByMemberEmailAndCouponId(email,couponId);

        if(isIssued){
            System.out.println("이미 발급 받은 기록이 있습니다.");
            throw new RuntimeException("이미 쿠폰을 발급 받았습니다.");
        }

        // 쿠폰 수량 감소
        coupon.decreaseQuantity();
        // 발급 이력 저장
        saveIssueResult(memberEmail, couponId, IssueStatus.SUCCESS.getMessage());
        System.out.println("쿠폰 발급 성공 " + memberEmail);
    }

    @Transactional
    public void issueCouponWithPessimisticLock(String email, Long id) {
        // 회원 정보 조회
        String memberEmail = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(String.format("회원을 찾을 수 없습니다. %s", email)))
                .getEmail();

        // 비관적 락을 사용
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보를 찾을수 없습니다."));

        Long couponId = coupon.getId();

        // 쿠폰 발급 이력 조회
        boolean isIssued = historyRepository.existsByMemberEmailAndCouponId(email,couponId);

        if(isIssued){
            throw new IllegalStateException("이미 쿠폰을 발급 받았습니다.");
        }

        // 쿠폰 수량 감소
        coupon.decreaseQuantity();
        // 발급 이력 저장
        saveIssueResult(memberEmail, couponId, IssueStatus.SUCCESS.getMessage());
        System.out.println("쿠폰 발급 성공 " + memberEmail);
    }

    @Transactional
    //
    public void issueCouponCore(String email, Long id) {

        // 회원 정보 조회
        String memberEmail = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(String.format("회원을 찾을 수 없습니다. %s", email)))
                .getEmail();

        // 쿠폰 정보 조회
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("쿠폰 정보를 찾을 수 없습니다. ID: %d", id)));

        Long couponId = coupon.getId();

        // 쿠폰 수량 감소
        coupon.decreaseQuantity();
        // 발급 이력 저장
        saveIssueResult(memberEmail, couponId, IssueStatus.SUCCESS.getMessage());
        System.out.println("쿠폰 발급 성공 " + memberEmail);
    }

    public void saveIssueResult(String memberId, Long couponId, String status) {
        CouponIssueHistory history = new CouponIssueHistory(memberId, couponId, status);
        historyRepository.saveAndFlush(history);
    }

}
