package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // 컴포넌트 스캔의 대상이 됨 ->  스프링 빈으로 등록됨
// JPA 조회 성능 최적화, 데이터의 변경은 트랜잭션 안에서 실행되어야 함
// class 레벨에서 사용하면 public 메소드들은 모두 트랜잭션이 적용된다.
@Transactional(readOnly = true)
// @AllArgsConstructor // 모든 멤버변수를 대상으로 생성자를 만들어준다. (아래 생성자 코드를 적지 않아도 된다!)
@RequiredArgsConstructor // final 이 붙은 멤버변수를 대상으로 생성자를 만들어준다!
public class MemberService {

    private final MemberRepository memberRepository;

    // 세터 인젝션은 테스트는 용이하지만, 외부에서 세터를 호출해 값을 수정할 수 있기 때문에 위험하다. -> 사용x
    // 생성자 인젝션을 사용하자.
    // 테스트도 용이하며, 외부 수정도 불가능하고, 런타임까지 가지 않고
    // 컴파일에서 오류를 알려줘 의존성 빈을 미리 설정할 수 있다.
    // @Autowired // 생성자가 1개인 경우는 @Autowired 생략 가능
    /*public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }*/

    /**
     * 회원 가입
     *
     * @param member
     * @return
     */
    @Transactional
    public Long join(Member member) {

        // 중복 회원 검증
        validateDuplicateMember(member);

        memberRepository.save(member);

        return member.getId();
    }

    // 와스가 여러개인 실무에서는 API로 중복 회원을 검증해도 문제가 생길 수 있다.
    // 여러 와스에 멀티 쓰레드 환경에서 동시에 member.save() 를 호출하는 상황이 있을 수 있다.
    // 그렇게 때문에, API 에서 해당 검증을 처리한다고 해도, 최종적으로는
    // DB 에서도 유니크 제약을 설정해야 한다.
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 한명 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

}
