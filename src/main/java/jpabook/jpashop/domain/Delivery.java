package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY) // 지연 로딩 설정 (디폴트)
    private Order order;

    @Embedded // 임베디드 타입, DB에 테이블 생성 x
    private Address address;

    @Enumerated(EnumType.STRING) // java의 ENUM 타입 명시
    private DeliveryStatus status; // ENUM [READY(준비), COMP(배송)]

}

/**
 * @Enumerated 사용 시 주의사항!
 * Enum 타입의 디폴트인 오디널은 사용하지 말자. -> 순서대로 추가되는 형식은 추후 데이터가 변하면 순서가 엉망이 될 수 있다.
 *
 * 그냥 명시적으로 적어주는 String 타입을 사용하자!
 */
