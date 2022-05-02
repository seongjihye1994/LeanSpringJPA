package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // DB 에서도 상속 관계 설정(default: 단일 테이블 전략 -> 하나의 테이블에 모든 필드 넣음)
@DiscriminatorColumn(name = "dtype") // 부모 클래스에 선언 (하위 클래스를 구분하는 용도)
@Getter
@Setter // 연습을 위해 세터를 기재함, 실무에서는 사용 지양 -> 데이터 수정은 세터가 아닌 메소드를 만들어서 사용하기!
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

    // == 비즈니스 로직 시작 == //
    // 데이터가 있는 쪽에 비즈니스 로직을 작성하는 것이 객체지향적이다.
    // ex) stockQuantity 필드변수가 있음. -> stockQuantity 와 관련된 비즈니스 로직을 해당 클래스에 작성

    /**
     * 재고 증가
     * @param quantity
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     * 0보다는 커야한다.
     * @param quantity
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;

        if (restStock < 0) { // 재고는 0보다는 커야함.
            throw new NotEnoughStockException("need more stock");
        }

        this.stockQuantity = restStock;
    }


}
