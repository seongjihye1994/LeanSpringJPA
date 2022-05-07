package jpabook.jpashop.Controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
@NoArgsConstructor
public class BookForm {

    private Long id;

    // == 상품 공통 속성 == //
    private String name;
    private int price;
    private int stockQuantity;

    // == 책에 대한 속성 == //
    private String author;
    private String isbn;

}
