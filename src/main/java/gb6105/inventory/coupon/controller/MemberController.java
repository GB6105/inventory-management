package gb6105.inventory.coupon.controller;

import gb6105.inventory.coupon.dto.MemberRequestDTO;
import gb6105.inventory.coupon.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberRequestDTO request) {
        memberService.registerMember(request.email());
        return ResponseEntity.ok("등록이 완료되었습니다. 반갑습니다.");
    }
}
