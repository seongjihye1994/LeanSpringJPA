package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * 값 타입은 변경 불가능하게 설계해야 한다.
 * -> @Setter 를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들자.
 *
 * JPA 스펙상 엔티티나 임베디드 타입( @Embeddable )은
 * 자바 기본 생성자(default constructor)를 public 또는 protected 로 설정해야 한다.
 * public 으로 두는 것 보다는 protected 로 설정하는 것이 그나마 더 안전하다.
 *
 * JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가
 * 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.
 */
@Embeddable // 임베디드 타입 (JPA 내장타입, DB에는 테이블이 생성되지 않는다.)
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    // @Setter 제거 후 생성자에서 값을 모두 초기화해서 변경 불가능하게 설정
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    


}
