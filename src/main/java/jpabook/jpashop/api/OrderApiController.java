package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * API 개발 고급 - 컬렉션 조회 최적화
     * v1. 엔티티 직접 노출
     *
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
}
