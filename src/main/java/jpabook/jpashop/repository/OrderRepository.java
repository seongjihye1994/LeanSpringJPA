package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor // final이 붙은 멤버변수를 대상으로 자동으로 생성자를 만들어줌! (생성자로 객체를 생성함과 동시에 의존성 주입까지!)
public class OrderRepository {


    /**
     * 이 어노테이션이 있으면 JPA의 엔티티 매니저가 스프링이 생성한 엔티티 매니저를 자동으로 의존 주입해준다.
     * 스프링이 엔티티 매니저를 만들어서 주입해준다.
     * <p>
     * 만약 스프링을 사용하지 않으면, 앤티티매니저팩토리와 앤티티매니저를 모두 수동으로 생성하고,
     * 트랜잭션 커밋과 try~catch로 예외를 잡아줘야 한다.
     */
    @PersistenceContext
    private final EntityManager em;

    /**
     * 주문 상품 저장
     *
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
     * fetch join 을 사용해서 Order를 조회할 때 Member와 Delivery도 그래프탐색으로 쿼리 한방에 조회하기.
     * -> 즉시로딩, 지연로딩의 N+1 이슈 해결
     *
     * @return
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" + // Order 를 조회할 때
                        " join fetch o.member m" + // member 와
                        " join fetch o.delivery d", Order.class // delivery 도 그래프탐색으로 한 번에 조회
        ).getResultList();

        // Order 조회 시 member와 delivery 조인해서 한 방에 가져옴
        // 현재 Order 엔티티를 보면, member 필드와 delivery 필드가 지연로딩으로 설정되어 있다.
        // 하지만 fetch join을 사용하면 지연로딩 모두 무시하고 쿼리 한방에 조회해서 한 번에 가져온다.
    }

    /**
     * 컬렉션 패치 조인 + 페이징
     * @param offset
     * @param limit
     * @return
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o" + // Order 를 조회할 때
                                " join fetch o.member m" + // member 와
                                " join fetch o.delivery d", Order.class // delivery 도 그래프탐색으로 한 번에 조회
                ).setFirstResult(offset) // 페이징 처리
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Order> findAllWithItem() {
        // fetch join
        return em.createQuery(
                        "select distinct o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d" + // 여기까지는 v2와 같음
                                " join fetch o.orderItems oi" + // order와 orderItems 를 조인한다!! -> order 2개 + orderItems 4개 -> 쿼리 결과는 결국 4개가 된다.
                                " join fetch oi.item i", Order.class)
                .getResultList();

        // order 2개, orderItem 4개 조인 시 왜 쿼리 결과가 4개가 될까?

        /*SELECT * FROM ORDERS
        where order_id = 4;

        SELECT *  from ORDER_ITEM
        where order_id = 4;

        select * from orders o
        join order_item oi on o.order_id = oi.order_id
        where o.order_id = 4;*/

        // 이 sql 을 실행하면 마지막 최종 결과는 2행이 조회된다.

        // distinct???
        // db 의 distinct를 말하는건가?
        // 맞는데, 여기서 조금 다르다.
        // 1. 먼저 db에서 쿼리를 날릴 때 distinct를 해준다.
        // 2. 근대 distinct는 로우의 데이터들이 완전히 동일할 때만 중복을 제거해준다.
        // 그래서 여기까지는 큰 의미가 없다.
        // 하지만, jpa 에서의 distinct는 db에서 쿼리를 날릴 때 한번 먼저 distinct를 해주고
        // application 으로 가져와 진 쿼리에서 id 값이 같은지 확인하고,
        // 만약 pk값이 같다면 같은 데이터로 인식해 중복을 줄여준다!
        // 이후에 collection에 담는다.

        // 아래는 distinct 한 쿼리 결과다.
        // 원하는 모양대로 잘 나왔다.
        // 쿼리가 딱 한번 나왔고, order 에 대한 orderItem이 각각 배열 내부에 잘 들어와져 있다.
        // **** jpa의 페치조인을 사용해 쿼리를 튜닝했다. ****

        /*[
        {
            "orderId": 4,
                "name": "userA",
                "orderDate": "2022-06-02T18:44:52.626902",
                "orderStatus": "ORDER",
                "address": {
            "city": "서울",
                    "street": "1",
                    "zipcode": "1111"
        },
            "orderItems": [
            {
                "itemName": "JPA1 BOOK",
                    "orderPrice": 10000,
                    "count": 1
            },
            {
                "itemName": "JPA2 BOOK",
                    "orderPrice": 20000,
                    "count": 2
            }
        ]
        },
        {
            "orderId": 11,
                "name": "userB",
                "orderDate": "2022-06-02T18:44:52.706907",
                "orderStatus": "ORDER",
                "address": {
            "city": "진주",
                    "street": "2",
                    "zipcode": "2222"
        },
            "orderItems": [
            {
                "itemName": "SPRING1 BOOK",
                    "orderPrice": 20000,
                    "count": 3
            },
            {
                "itemName": "SPRING2 BOOK",
                    "orderPrice": 40000,
                    "count": 4
            }
        ]
        }
    ]*/


    }

}


/**
 * 주문건 검색 기능
 * <p>
 * JPA Criteria
 * <p>
 * 딱봐도 안 쓸것같이 생김.
 *
 * @param orderSearch
 * @return
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



