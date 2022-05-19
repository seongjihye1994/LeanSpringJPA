package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
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
        em.persist(order); // 영속성 컨텍스트 1차 캐시에 저장
    }

    /**
     * 주문 상품 하나 조회
     */
    public Order findOne(Long id) {
        return em.find(Order.class, id); // 영속성 컨텍스트 1차 캐시에서 조회
    }


    public List<Order> findAll(OrderSearch orderSearch) {
        // language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }


    /**
     * 주문건 검색 기능
     */
//    public List<Order> findAll(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m" +
//                        " where o.status = :status " + // 파라미터 바인딩 시 : 필수
//                        " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus()) // 조건 파라미터 -> 주문 상태
//                .setParameter("name", orderSearch.getMemberName()) // 조건 파라미터 -> 회원명
//                .setMaxResults(1000) // 최대 1000건 까지 조회
//                .getResultList();

        // 그런데 동적 쿼리는 어떻게 적용하지?
        // 검색 조건이 있을 때만 쿼리를 실행하고 싶다.

        // 1. 직접 동적으로 생성한다. -> 코드 너무 복잡하고 막노동이라 그냥 네이티브 sql 쓰는게 낫겠음.

        // 주문 상태 검색
        //language=JPAQL
        /*String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                 .setMaxResults(1000); //최대 1000건

                 if (orderSearch.getOrderStatus() != null) {
                     query = query.setParameter("status", orderSearch.getOrderStatus());
                 }

                 if (StringUtils.hasText(orderSearch.getMemberName())) {
                     query = query.setParameter("name", orderSearch.getMemberName());
                 }

                 return query.getResultList();

        */

        // 2. JPA Criteria로 처리한다. (아래 코드)
    //}

    /**
     * JPA Criteria
     *
     * 딱봐도 안 쓸것같이 생김.
     *
     * @param orderSearch
     * @return
     */
    /*public List<Order> findAllByCriteria(OrderSearch orderSearch) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); // 회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건

        return query.getResultList();
    }*/

    // 3. Query DSL 쓰자.


}
