package gb6105.inventory.coupon.controller;

import gb6105.inventory.coupon.dto.CouponIssueRequest;
import gb6105.inventory.coupon.service.CouponService;
import gb6105.inventory.coupon.service.CouponServiceRedisson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponServiceRedisson couponServiceRedisson;

    @PostMapping("/issue/lock")
    public ResponseEntity<String> issueCoupon(@RequestBody CouponIssueRequest request) {
        try {
            // 1. Service 로직 직접 호출 (DB 락 획득, 검증, 발급, 커밋까지 동기적으로 처리)
            couponService.issueCouponWithPessimisticLock(request.email(), request.couponId());

            // 2. 성공 시 HTTP 200 OK 응답
            return ResponseEntity.ok()
                    .body("{\"message\": \"쿠폰 발급에 성공했습니다.\"}");

        } catch (IllegalStateException e) {
            // 3. 발급 실패 (재고 소진, 중복 발급 등) 시 HTTP 409 Conflict 응답
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            // 4. 요청 데이터 오류 (회원/쿠폰 ID 없음) 시 HTTP 400 Bad Request 응답
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // 5. 기타 서버 오류 (DB Connection Pool 고갈, 락 타임아웃 등)
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"서버 오류가 발생했습니다.\"}");
        }
    }
    @PostMapping("/issue/redisson")
    public ResponseEntity<String> issueCouponWithRedisson(@RequestBody CouponIssueRequest request) {
        try {
            // 1. Service 로직 직접 호출 (DB 락 획득, 검증, 발급, 커밋까지 동기적으로 처리)
            couponServiceRedisson.issueCouponWithRedisson(request.email(), request.couponId());

            // 2. 성공 시 HTTP 200 OK 응답
            return ResponseEntity.ok()
                    .body("{\"message\": \"쿠폰 발급에 성공했습니다.\"}");

        } catch (IllegalStateException e) {
            // 3. 발급 실패 (재고 소진, 중복 발급 등) 시 HTTP 409 Conflict 응답
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            // 4. 요청 데이터 오류 (회원/쿠폰 ID 없음) 시 HTTP 400 Bad Request 응답
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // 5. 기타 서버 오류 (DB Connection Pool 고갈, 락 타임아웃 등)
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"서버 오류가 발생했습니다.\"}");
        }
    }
}
