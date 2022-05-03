package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor // final이 붙은 멤버변수를 대상으로 자동으로 생성자를 만들어줌! (생성자로 객체를 생성함과 동시에 의존성 주입까지!)
public class OrderRepository {


    /**
     * 이 어노테이션이 있으면 JPA의 엔티티 매니저가 스프링이 생성한 엔티티 매니저를 자동으로 의존 주입해준다.
     * 스프링이 엔티티 매니저를 만들어서 주입해준다.
     *
     * 만약 스프링을 사용하지 않으면, 앤티티매니저팩토리와 앤티티매니저를 모두 수동으로 생성하고,
     * 트랜잭션 커밋과 try~catch로 예외를 잡아줘야 한다.
     */
    @PersistenceContext
    private final EntityManager em;

    /**
     * 주문 상품 저장
     * @param order
     */
    public void save(Order order) {
        em.persist(order); // 영속성 컨텍스트 1차 캐시에 저장, DB 쿼리 x
    }

    /**
     * 주문 상품 하나 조회
     */
    public Order findOne(Long id) {
        return em.find(Order.class, id); // 영속성 컨텍스트 1차 캐시에서 조회, DB 쿼리 x
    }

    /**
     * 주문 상품 검색 기능
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        return null;
    }





}
