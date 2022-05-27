package jpabook.jpashop.api;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController // @Controller + @ResponseBody
public class MemberApiController {

    private final MemberService memberService;


    // 엔티티를 그대로 사용한 멤버 조회
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 응답을 Result 라는 객체로 하는 멤버 조회
     * @return
     */
    @GetMapping("api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();

        // memberDto로 바꿔서 리턴
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }




    /**
     * API 요청 스팩에 맞게 DTO를 별도로 생성해서 개발해야 한다.
     * DB 물리적 설계를 위한 엔티티 객체와 폼에서 컨트롤러로 넘어오는 DTO 를 같은 객체로 취급해 사용해 버리면
     * 여러 개발자가 개발을 같이 할 경우에는 큰 장애가 발생할 수 있다.
     * ex) 어떤 개발자가 Member 객체의 name 필드를 username 으로 수정하면 에러가 발생한다.
     *
     * 실무에서 로직이 조금만 복잡해져도 엔티티 객체 하나로 모든 로직을 감당할 수 없다.
     *
     * 그래서 API 를 만들때는 절대로 엔티티를 파라미터로 받지 말것!
     * 그리고 엔티티를 외부에 노출하는 행위도 옳지 않다!
     * */

    // Entity 객체를 그대로 사용했을 경우
    @PostMapping("api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        // @RequestBody : json 으로 넘어온 데이터를 Member 객체로 자동 변환
        // @Valid 가 넘어오는 Member 객체의 필드값을 검증함.
        // 여기서는 name 을 넘김.
        // 만약 name 값이 비어있다면 에러
        Long id = memberService.join(member); // member id 반환

        return new CreateMemberResponse(id);
    }

    /**
     * 회원 등록
     * @param request
     * @return
     */
    // Entity 객체를 그대로 사용하지 않고 DTO 를 별도로 정의해서 사용했을 경우
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 이름 수정
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {

        // 수정할 때는 변경감지 메소드 사용하기!
        memberService.update(id, request.getName());

        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }



    // ----------------------------------------------------------------------------

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    // ** generic raw type
    // json 배열 타입으로 반환하는 것은 유지보수성에 좋지 않기 때문에
    // 제네릭으로 배열을 Object(json 타입)로 한 번 감싸서 리턴
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    /**
     * 멤버 엔티티를 그대로 노출 x
     * 딱 필요한 필드 몇가지만 따로 dto로 정의해서 노출해야 한다.
     */
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

}
