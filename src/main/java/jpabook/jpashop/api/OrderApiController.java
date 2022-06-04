package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * API 개발 고급 - 컬렉션 조회 최적화
     * v1. 엔티티 직접 노출
     * <p>
     * >> 엔티티가 변하면 API 스팩이 변한다.
     * >> 트랜잭션 안에서 지연 로딩 필요
     * >> 양방향 연관관계 문제 발생
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {

        // 회원이 주문한 주문을 모두 조회해온다.
        List<Order> all = orderRepository.findAll(new OrderSearch());

        // 조회해 온 주문을 루프를 돌려 강제 초기화한다. -> 필드를 터치한다.
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems(); // 여기서 문제!
            orderItems.stream().forEach(o -> o.getItem().getName());  // 가져온 orderItem 의 이름도 초기화
        }

        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환 (fetch join 사용X)
     * >> 트랜잭션 안에서 지연 로딩 필요
     *
     * dto로 잘 변환된것 같지만, 이 api는 문제가있다.
     * OrderDto를 보면 필드에 List<OrderItem> orderItems 에서 엔티티를 사용해주고 있다!
     * 단순히 dto로 변환해서 리턴해주면 되는 그런 문제가 아니다.
     * v2 메소드를 호출하면 리턴값에 OrderItem 의 내부 필드값이 전~부 노출되는 것을 알 수 있다.
     * 어떤 상황에서라도, 엔티티의 내부 모든 값을 외부에 노출해서는 안된다.
     * 노출이라는 의미는 의존이라는 의미이다.
     *
     * 즉, 어떤 상황에서라도 Entity 가 api의 request, response 와 의존해서는 안된다..
     *
     * dto 내부의 엔티티도 dto로 수정해줘야 한다!!!!!!!!
     */
    @GetMapping("api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    /**
     * 컬렉션 패치 조인 성능 최적화
     *
     * v2 는 dto 내부의 entity도 dto로 바꿔주었지만
     * 지연로딩으로 인한 N+1 문제가 난다.
     *
     * 컬렉션 페치 조인은 어떻게 사용하는지 알아보자.
     * @return
     */
    @GetMapping("api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    /**
     * 컬렉션 패치 조인 + 페이징
     * @return
     */
    @GetMapping("api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit) {
        // Order와 Member, Delivery 패치조인
        // ~ToOne 관계는 패치조인으로 한 번에 조회해 오기. -> 페이징에 영향을 주지 않는다.
        // (Order - Member, Order - Delivery 는 ~ToOne 관계)
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // 이 OrderItem (dto 안에 엔티티죠?) 도 dto로 바꿔야 한다!!!!

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    /**
     * dto 안의 엔티티도 dto로 바꿔준다.
     *
     * 고객은 사실상 아이템 이름과, 아이템 가격, 주문 수량만 있으면 된다.
     *
     * 외부 응답값은 OrderItem 에서 OrderItemDto로 래핑해서 나가기 때문에
     * OrderItem 엔티티의 내부 값은 볼 수 없게되고, 의존성이 낮아져 유지보수에 편리하다.
     */
    @Getter
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
