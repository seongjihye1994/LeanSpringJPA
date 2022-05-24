package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne (~ToOne 관계 : ManyToOne, OneToOne 관계에서의 성능 최적화)
 * <p>
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    // 테스트를 위해 Order 엔티티 그대로 api에서 받음 -> 실무에서는 절대 이렇게 하지 말기!!!!!
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch()); // 모든 주문 전부 다 조회
        // 현재 Order 엔티티를 보자.
        // Order 엔티티 안에 member 필드가 @ManyToOne
        // delivery 필드가 @OneToOne 으로 되어있음.

        // 이 상태에서 그대로 모든 주문을 조회한 all 을 리턴한다면??

        // 무한루프에 빠지게 된다.

        // 왜일까?

        // Order 엔티티를 잘 보면
        // Order 엔티티 내부에 member 필드가 있다.
        // Order 엔티티는 조인하면서 member를 가져오고 해당 member 정보를 json으로 뿌려줘야 한다.

        // member로 가보자.
        // Member 엔티티로 가보니, 필드에 orders 가 List로 존재한다.
        // 그럼 이 Member 엔티티도 orders 를 json으로 뿌려줘야 한다.
        // Order로 간다.

        // 무한반복...

        // 객체를 json 으로 변환하는 jackson 라이브러리 입장에서는
        // Order에서 member 필드를, Member에서 orders 필드를
        // 지속적으로 생성하게 된다...

        // 이는 양방향 연관관계에서 일어나는 문제점이다.
        // 해결하기 위해서는 양방향 엔티티 중 한 쪽 필드를 @JsonIgnore 를 해줘야 한다.
        // jackson 라이브러리가 무한으로 양방향 필드를 json화 하는 것을 끊어주는 것.

        // jsonIgnore 설정 후 재기동하면? 바이트버디 500 에러 발생!

        // 두번째 문제가 발생했다.
        // 왜 이런 에러가 발생할까?

        // Order와 Member는 양방향 연관관계이다.
        // 또한, 서로 지연로딩으로 설정되어 있다.
        // 지연로딩은, Order를 DB에서 당겨올 때, Member를 바로 가지고 오지 않는다.
        // 그렇기 때문에 Order를 DB에서 당겨올 때 필요한 Member는 byteBuddy인터페이스를 사용한
        // 가짜 프록시 객체이다. (Member를 상속받은)
        // 이 프록시 객체를 가짜로 넣어놓고
        // 실제로 멤버가 터치되는 순간에 디비에서 멤버값을 조회해 영속성 1차캐시에 넣어놓고
        // member 객체에 채워주는 것이다. -> 프록시 초기화

        // 잭슨 라이브러리가 Order 엔티티를 확인하고 member가 있군?
        // member 필드에 대해 json화 시키려는데 오잉?
        // 진짜 멤버가 아니다!
        // 바이트버디 가짜 객체다!
        // 나는 이런 바이트버디 가짜 객체는 처리할 수 없다!
        // 해서 500 에러를 터트린다.

        // 이 두번째 문제를 해결하기 위해
        // 잭슨아... 아무것도 뿌리지 말거라.. 설정을 해보자.
        // 1. build.gradle 에 jackson-datatype-hibernate5 라이브러리 설정하기!!
        // 2. 빈으로 등록해주기!

        // 이렇게 설정하면 json에서 뿌릴 때 지연 로딩이라면 무시해버린다.


        // 또는 Lazy 강제 초기화를 통해 원하는 필드를 가져올 수 있다.
        for (Order order : all) {
            order.getMember().getName();
            // order.getMember() 여기까지는 프록시 객체!!
            // .getName() 호출하면 DB에서 진짜 Member를 가져온다!!! -> Lazy 강제 초기화!
            order.getDelivery().getAddress();
        }

        // 왜 에러가 나는지만 알아갈 것!!!
        // 가장 아래 해결방법은 알아만 두자.
        // 어차피 실무에서 엔티티를 그대로 노출해서 사용하지 않는다면 이 에러는 발생하지 않는다!

        return all;
    }

    /**
     * 엔티티를 그대로 노출하지 않고 Dto를 별도로 만들어서 반환
     *
     * 하지만 여전히 N+1 쿼리 문제가 발생한다.
     * -> v3 에서 fetch join 을 사용해서 성능 이슈를 수정해보자.
     * @return
     */
    @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {

        // ORDER 2개
        // N + 1 -> 1 + 회원 + 배송 N
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o)) // order 를 SimpleOrderDto로 변환
                .collect(Collectors.toList()); // SimpleOrderDto로 변환한 것을 collect 사용해서 list로 변환

        return result;

    }

    /**
     * 엔티티를 그대로 노출하지 않고 Dto를 별도로 만들어서 반환
     *
     * -> v2에서 발생한 지연로딩 N+1 쿼리 발생 성능 이슈를 fetch join 으로 수정
     *
     * ** 패치 조인을 사용해야 JPA 쿼리 성능을 높일 수 있다. **
     * @return
     */
    @GetMapping("api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(); // fetch join을 사용해 Order와 member, delivery 쿼리 한 방에 가져오기

        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());


        /*
        ** 쿼리 결과!! **

        select
        order0_.order_id as order_id1_6_0_,
                member1_.member_id as member_i1_4_1_,
        delivery2_.delivery_id as delivery1_2_2_,
                order0_.delivery_id as delivery4_6_0_,
        order0_.member_id as member_i5_6_0_,
                order0_.order_date as order_da2_6_0_,
        order0_.status as status3_6_0_,
                member1_.city as city2_4_1_,
        member1_.street as street3_4_1_,
                member1_.zipcode as zipcode4_4_1_,
        member1_.name as name5_4_1_,
                delivery2_.city as city2_2_2_,
        delivery2_.street as street3_2_2_,
                delivery2_.zipcode as zipcode4_2_2_,
        delivery2_.status as status5_2_2_
                from
        orders order0_
        inner join
        member member1_
        on order0_.member_id=member1_.member_id
        inner join
        delivery delivery2_
        on order0_.delivery_id=delivery2_.delivery_id

        쿼리 딱 한 방 나갔다!!

        v2 버전은 쿼리가 5방이 나감..

        */

    }



    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // 생성자에서 바로 데이터 세팅
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // 위에서 orders.stream 할 떄 LAZY 초기화 일어남
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // 위에서 orders.stream 할 떄 LAZY 초기화 일어남
        }
    }

}
