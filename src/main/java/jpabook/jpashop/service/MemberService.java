package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // 컴포넌트 스캔의 대상이 됨 ->  스프링 빈으로 등록됨
@Transactional // 데이터의 변경은 트랜잭션 안에서 실행되어야 함, class 레벨에서 사용하면 public 메소드들은 모두 트랜잭션이 적용된다.
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 회원 가입
     *
     * @param member
     * @return
     */
    public Long join(Member member) {

        // 중복 회원 검증
        validateDuplicateMember(member);

        memberRepository.save(member);

        return member.getId();
    }

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
