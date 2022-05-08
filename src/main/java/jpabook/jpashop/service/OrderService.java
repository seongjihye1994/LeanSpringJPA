package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;


    /**
     * 주문 생성
     *
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 주문자 pk, 아이템 pk, 주문 수량이 넘어옴

        // 엔티티 조회회
        Member member = memberRepository.findOne(memberId); // 멤버 엔티티 조회
        Item item = itemRepository.findOne(itemId); // 아이템 엔티티 조회

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress()); // 실제로는 배송 정보는 고객 주소와 다를 수 있지만, 예제이므로 동일하게 설정

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        // 객체의 무분별한 new 키워드 생성을 막기 위해 디폴트 생성자를 protected 로 생성해주면
        // OrderItem orderItem1 = new OrderItem();
        // new 로 객체를 생성할 때 컴파일 오류를 뱉는다.

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        // 객체의 무분별한 new 키워드 생성을 막기 위해 디폴트 생성자를 protected 로 생성해주면
        // Order order1 = new Order();
        // new 로 객체를 생성할 때 컴파일 오류를 뱉는다.

        // 주문 저장
        orderRepository.save(order);
        return order.getId();
        // Order 클래스의 orderItems 와 delivery 필드는
        // cascade = CascadeType.ALL 설정이 되어있다.
        // 그래서 연관관계가 매핑되어 있는 테이블에도 모든 작업을 전파한다.
        // 그래서, Order 객체가 영속성 컨텍스트 1차 캐시에 저장될 때
        // Order 와 연관된 객체도 영속성 컨텍스트 1차 캐시에 저장되는 작업이 되물림된다.
        // 그래서 orderRepository.save 로 한번만 저장해줘도 Order와 연관된 객체들도 영속화된다.

    }


    /**
     * 주문 취소
     *
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long orderId) {

        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();
        // 여기서 JPA 의 장점이 설명될 수 있다.
        // 만약 직접 쿼리를 다루는 서비스라면(mybatis, jdbc template 등...)
        // 비즈니스 로직에서도 주문 취소의 수량을 바꿔줘야 하고,
        // 쿼리도 직접 수량을 바꿔주는 sql 문을 작성해야 한다.
        // 하지만 JPA는 엔티티의 주문 취소 데이터만 수정해주면
        // JPA 의 더티체킹 기능 덕분에 sql문을 직접 작성하지 않아도
        // 쉽게 수정할 수 있다.
    }

    /**
     * 주문 검색
     *
     * @param orderSearch
     * @return
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }

}
