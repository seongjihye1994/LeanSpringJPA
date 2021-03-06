package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository // 자동으로 스프링 빈으로 등록
@RequiredArgsConstructor // final이 붙은 멤버변수를 대상으로 자동으로 생성자를 만들어줌! (생성자로 객체를 생성함과 동시에 의존성 주입까지!)
public class MemberRepository {

    /**
     * 이 어노테이션이 있으면 JPA의 엔티티 매니저가 스프링이 생성한 엔티티 매니저를 자동으로 주입해준다.
     * 스프링이 엔티티 매니저를 만들어서 주입해준다.
     *
     * 만약 스프링을 사용하지 않으면, 앤티티매니저팩토리와 앤티티매니저를 모두 수동으로 생성하고,
     * 트랜잭션 커밋과 try~catch로 예외를 잡아줘야 한다.
     */
    @PersistenceContext
    private final EntityManager em;

    /*public MemberRepository(EntityManager em) {
        this.em = em;
    }*/

    // 회원 저장
    public void save(Member member) {
        em.persist(member); // 영속화, 트랜잭션 커밋 시 db에 insert 쿼리 날라감, pk는 Member 앤티티의 pk 값
    }

    // 회원 한명 조회
    public Member findOne(Long id) {
        return em.find(Member.class, id); // 단건 조회, (타입, PK)
    }

    // 회원들 전부 조회
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList(); // 회원 전부 조회 할 때는 JPQL 사용 (JPQL 언어, 반환 타입)
                // JPQL 은 SQL과 문법이 거의 같다.
                // 다만 대상이 테이블이 아닌, 객체가 대상이다.
                // 즉, select m ... 에서의 m 은 테이블이 아닌 엔티티이다.
    }

    // 이름으로 회원들 조회
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name) // :name 에 setParameter(name) 으로 설정한 name이 들어감 -> 조건을 사용한 파라미터 바인딩
                .getResultList();
    }


}
