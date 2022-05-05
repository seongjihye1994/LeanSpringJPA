package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    /**
     * 주문 내역 속의 주문 아이템은 아이템 중에서 여러개일 수 있다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item; // 주문 상품

    /**
     * 주문 내역 속의 주문 아이템은 주문건 속에서 여러개일 수 있다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    // 외부에서 new 키워드로 무분별한 생성을 막기 위해
    // 디폴트 생성자를 protected 로 생성해준다.
    /*protected OrderItem() {

    }*/
    // @NoArgsConstructor(access = AccessLevel.PROTECTED) 롬복을 사용하면
    // 위의 코드도 안적어줘도 된다!


    // == 생성 메소드 == //
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();

        // 주문 아이템 생성
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        // 주문 한 갯수만큼 재고 차감
        item.removeStock(count);

        return orderItem;
    }


    // == 비즈니스 로직 == //

    /**
     * 주문 취소 == 재고 수량을 원복함
     */
    public void cancel() {
        getItem().addStock(count);
    }


    // == 조회 로직 == //

    /**
     * 주문 상품 전체 주문 가격 계산
     * <p>
     * 이 메소드는 OrderItem 에 정의하는 것이 옳다.
     * 왜냐? 이 클래스의 멤버 변수로 주문 가격과 주문 수량이 있기 때문이다.
     *
     * @return
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
