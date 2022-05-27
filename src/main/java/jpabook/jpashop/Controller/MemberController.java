package jpabook.jpashop.Controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입 폼
     * @param model
     * @return
     */
    @RequestMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm()); // 검증을 위해 빈 객체를 리턴
        return "members/createMemberForm";
    }

    /**
     * 회원 가입
     * @param form
     * @param result
     * @return
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        // 검증을 위한 javax @Valid 사용, MemberForm 에 있는 @NotEmpty 와 같은 검증 어노테이션을 기준으로 검증해줌.
        // @Valid 로 검증한 결과를 BindingResult 에 바인딩함. 안잡으면 컨트롤러에서 예외를 잡지 못하고 튕겨나감.

        if (result.hasErrors()) { // @Valid 에 만약 예외가 발생해 BindingResult 에 담겼다면
            return "members/createMemberForm"; // BindingResult를 사용하면 화면까지 에러를 가지고 갈 수 있다.
        }

        /*
        서버 사이드에서 검증을 진행한다. (@Valid)
        검증 시 만약 예외가 터지면 바인딩 리절트에 담는다. (@BindingResult)
        만약 바인딩 리절트에 예외가 담겼다면 예외를 화면에 출력한다.
        타임리프로 작성된 화면에서는 fields.hasErrors 로 예외 여부를 확인할 수 있다.
        만약 name 이라는 필드에 에러가 있다면 form-control fieldError 커스텀 html css를 적용.
        name 이라는 필드에 에러가 없다면 form-control 적용
        fields.hasErrors 로 name 필드에 에러가 있다면 name 필드의 에러 메세지를 뽑아서 에러로 출력
        */

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; // PRG
    }


    /**
     * 회원 전체 조회
     * @param model
     * @return
     */
    @GetMapping("/members")
    public String list(Model model) {

        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);

        return "/members/memberList";
    }
}
