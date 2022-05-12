package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id // 엔티티 식별자
    @GeneratedValue // db 에서 pk 값 자동 생성 위임
    @Column(name = "member_id") // PK 컬럼명 지정
    private Long id;

    @NotEmpty // @Valid 가 검증함. 만약 값이 비어있다면 에러
    private String name;

    @Embedded // 값 타입의 임베디드 타입 사용
    private Address address;

    /**
     * 1:N 관계에서는 FK가 N 테이블에 있으므로
     * 주인은 FK가 있는 N 테이블이 된다.
     *
     * 한 명의 회원(1)은 여러개의 주문(N)을 주문할 수 있다.
     * 회원 엔티티 입장에서의 주문 엔티티는 OneToMany
     */
    @OneToMany(mappedBy = "member") // 일대다 관계 정의(주인 x)
    private List<Order> orders = new ArrayList<>();

}
