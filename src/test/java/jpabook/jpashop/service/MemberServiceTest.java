package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class) // JUnit 실행 시 Spring 도 사용해서 테스트
@SpringBootTest // Spring Boot를 띄운 상태에서 테스트(없으면 의존성 주입이 안되므로 @Autowired 모두 실패함)
// @Transactional 의 기본 전략은 롤백이다. 커밋 x
// -> 즉, JPA 사용 시 트랜잭션 커밋이 되지 않기 때문에 영속성 컨텍스트에서만 쿼리가 날라가고 실제 DB 까지는 쿼리가 전달되지 않는다.
@Transactional // crud 기능 테스트를 위해 트랜잭셔널 적용 (적용해야 롤백 가능, 테스트니까 롤백해야함!)
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
//    @Rollback(false) // @Transactional 의 기본 전략인 롤백 true 말고 false 적용해야 실제 db에 쿼리가 날라가는 것을 볼 수 있음.
    public void 회원가입() {

        // given (~이 주어졌을 때 == 조건)
        Member member = new Member();
        member.setName("kim");

        // when (~으로 하면 == 상황)
        Long savedId = memberService.join(member);

        // then (~의 결과가 나와? == 결과)
        em.flush(); // DB 쿼리 날림! (영속성 컨텍스트와 실제 DB의 싱크를 맞춤, 이후 @Transactional 기본 전략인 롤백이 적용되므로 DB에는 다시 데이터가 롤백됨)
        assertEquals(member, memberRepository.findOne(savedId));
    }

    /**
     * member2를 join하면 예외가 발생한다.
     * 예외를 잡아주지 않으면 와스까지 나가버림
     * try catch 로 예외가 발생하면 return 해서 예외를 여기서 잡아주자.
     */
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() {

        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);
        memberService.join(member2);
        /*try {
            memberService.join(member2);
            // 같은 이름의 회원을 넣음 -> 예외가 발생해야 한다!!!!
        } catch (IllegalStateException e) {
            return;
            // member2를 join하면 예외가 발생한다.
            // 예외를 잡아주지 않으면 와스까지 나가버림
            // try catch 로 예외가 발생하면 return 해서 예외를 여기서 잡아주자.
        }*/

        // then
        fail("예외가 발생해야 한다.");
    }


}