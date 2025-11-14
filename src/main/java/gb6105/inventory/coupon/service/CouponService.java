package gb6105.inventory.coupon.service;


import gb6105.inventory.coupon.domain.Coupon;
import gb6105.inventory.coupon.domain.CouponIssueHistory;
import gb6105.inventory.coupon.domain.CouponIssueHistory.IssueStatus;
import gb6105.inventory.coupon.domain.Member;
import gb6105.inventory.coupon.repository.CouponRepository;
import gb6105.inventory.coupon.repository.CouponIssueHistoryRepository;
import gb6105.inventory.coupon.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueHistoryRepository historyRepository;

    @Transactional
    public void issueCoupon(String email, Long couponId) {

        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(String.format("회원을 찾을 수 없습니다. %s", email)));

        // 락 없이 일반 조회
        // Redisson 락이 이미 획득된 상태에서 실행됩니다.
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("쿠폰 정보를 찾을 수 없습니다. ID: %d", couponId)));

        // 쿠폰 발급 이력 조회
        long existCouponCount = historyRepository.countByMemberAndCouponAndStatus(member, coupon, IssueStatus.SUCCESS);
        if (existCouponCount > 0) {
            saveIssueResult(member, coupon, IssueStatus.FAIL);
            throw new IllegalStateException("이미 쿠폰을 발급받았습니다.");
        }

        // 재고 확인 및 감소
        if (coupon.getTotal_quantity() > 0) {
            // 쿠폰 수량 감소
            coupon.decreaseQuantity();
            // 발급 이력 저장
            saveIssueResult(member, coupon, IssueStatus.SUCCESS);
            System.out.println("쿠폰 발급 성공" + member.getMemberId());
        } else {
            saveIssueResult(member, coupon, IssueStatus.FAIL);
            System.out.println("쿠폰 발급 실패");
            throw new IllegalArgumentException("쿠폰 재고가 소진되었습니다.");
        }
    }

    @Transactional
    public void issueCouponWithPessimisticLock(String email, Long couponId) {
        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(String.format("회원을 찾을 수 없습니다. %s", email)));

        // 비관적 락을 사용
        Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보를 찾을수 없습니다."));

        // 쿠폰 발급 이력 조회
        long existCouponCount = historyRepository.countByMemberAndCouponAndStatus(member, coupon, IssueStatus.SUCCESS);

        if (existCouponCount > 0) {
            throw new IllegalArgumentException("이미 쿠폰을 발급받았습니다.");
        }

        // 재고 확인 및 감소
        if (coupon.getTotal_quantity() > 0) {
            // 쿠폰 수량 감소
            coupon.decreaseQuantity();
            // 발급 이력 저장
            saveIssueResult(member, coupon, IssueStatus.SUCCESS);
            System.out.println("쿠폰 발급 성공" + member.getMemberId());
        } else {
            saveIssueResult(member, coupon, IssueStatus.FAIL);
            System.out.println("쿠폰 발급 실패");
            throw new IllegalArgumentException("쿠폰 재고가 소진되었습니다.");
        }

    }

    private void saveIssueResult(Member member, Coupon coupon, CouponIssueHistory.IssueStatus status) {
        CouponIssueHistory history = new CouponIssueHistory(member, coupon, status);
        historyRepository.save(history);
    }

}
