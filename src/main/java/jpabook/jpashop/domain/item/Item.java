package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // DB 에서도 상속 관계 설정(default: 단일 테이블 전략)
@DiscriminatorColumn(name = "dtype") // 부모 클래스에 선언 (하위 클래스를 구분하는 용도)
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    /**
     * 여러개의 아이템은 여러개의 카테고리에 포함될 수 있다.
     */
    @ManyToMany(mappedBy = "items") // 자식에 설정
    private List<Category> categories = new ArrayList<Category>();
}
