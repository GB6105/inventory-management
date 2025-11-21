package gb6105.inventory.coupon.data;

import gb6105.inventory.coupon.domain.Coupon;
import gb6105.inventory.coupon.domain.Member;
import gb6105.inventory.coupon.repository.CouponRepository;
import gb6105.inventory.coupon.repository.MemberRepository;
import gb6105.inventory.coupon.service.RedisService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final RedisService redisService;

    public DataInitializer(MemberRepository memberRepository, CouponRepository couponRepository , RedisService redisService) {
        this.memberRepository = memberRepository;
        this.couponRepository = couponRepository;
        this.redisService = redisService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        if(couponRepository.findById(1L).isEmpty()){
            couponRepository.save(new Coupon("starbugs",10));
        }
        if(couponRepository.findById(2L).isEmpty()){
            couponRepository.save(new Coupon("twothumb",10));
        }
        if(couponRepository.findById(3L).isEmpty()){
            couponRepository.save(new Coupon("megabytecoffee",10));
        }

        redisService.initializeStock(1L,10);

        if (memberRepository.count() == 0) { // 데이터가 없을 때만 실행
            for (int i = 1; i <= 100; i++) {
                String email = String.format("user_%03d@test.com", i);
                memberRepository.save(new Member(email));
            }
            System.out.println("⭐ 100명의 테스트 회원을 생성했습니다.");
        }
    }
}
