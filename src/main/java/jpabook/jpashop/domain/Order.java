package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders") // 테이블 이름 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected 접근 제한자인 디폴트 생성자를 자동으로 생성해주는 롬복 -> 외부에서 new 키워드로 무분별한 객체 생성을 막음
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id") // 테이블 컬럼명 지정
    private Long id;

    /**
     * 서로 엔티티 관계가 자주 함께 사용되면 즉시로딩
     * 서로 엔티티 관계가 가끔 함께 사용되면 지연로딩
     * (여기서 멤버와 주문은 가끔 사용되니 지연로딩을 설정)
     */
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩, 실제 연관관계를 가진 필드가 touch 될 때 쿼리를 날림 (미리 조인 x)
    @JoinColumn(name = "member_id") // 조인 할 테이블의 컬럼 지정 -> FK 설정
    private Member member; // 주문 회원

    // 자식(1 테이블)에 mappedBy 설정
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // 상위 엔터티에서 하위 엔터티로 모든 작업을 전파
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 하나의 주문은 하나의 배송을 가진다.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    /**
     * EnumType.STRING 옵션 사용
     * ORDINAL 사용 x
     */
    @Enumerated // java enum 타입 매핑
    private OrderStatus status;


    /**
     * 생성자를 통해 양방향 연관관계를 모두 설정한다.
     *
     * 생성자를 사용하지 않고 양방향 연관관계를 이후 수정자로 설정하면
     * 수정자로 양방향 연관관계를 설정하지 않을 수 있는 실수가 일어날 수 있으므로,
     * 애초에 생성할 때 부터 양방향 연관관계가 설정될 수 있도록 한다.
     */
    // == 연관관계 편의 메서드 == //
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this); // 멤버를 객체 생성 시 주문 연관관계 설정
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this); // 주문아이템 객체 생성 시 주문 연관관계 설정
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this); // 배송 객체 생성 시 주문 연관관계 설정
    }

    // == 생성 메서드 ==

    /**
     * 주문 생성
     *
     * @param member
     * @param delivery
     * @param orderItems
     * @return
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) { // OrderItem... : 여러개의 OrderItem 파라미터를 넘길 수 있음.

        Order order = new Order();

        // 주문 한 사람
        order.setMember(member);

        // 배송
        order.setDelivery(delivery);

        // 주문 한 수 만큼 addOrderItem
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        // 주문 상태를 ORDER 로 강제
        order.setStatus(OrderStatus.ORDER);

        // 주문 시간 현재 시간으로 강제
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    // == 비즈니스 로직 == //

    /**
     * 주문 취소
     */

    public void cancel() {

        // 만약 배송 상태가 이미 배송 완료라면 배송 취소 불가
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료 된 상품은 취소가 불가합니다.");
        }

        // 주문 상태 '취소'로
        this.setStatus(OrderStatus.CANCEL);

        for (OrderItem orderItem : orderItems) {
            orderItem.cancel(); // 각각의 주문에 주문 취소를 해야한다.
        }
    }

    // == 조회 로직 == //

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }


}
