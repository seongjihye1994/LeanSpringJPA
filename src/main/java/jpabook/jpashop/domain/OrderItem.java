package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "order_item")
@Getter
@Setter
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
}
