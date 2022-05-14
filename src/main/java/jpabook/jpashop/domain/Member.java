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
    @Column(name = "member_id") // PK 컬럼명 지정 -> 객체는 id, DB의 컬럼명은 member_id로 지정
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

/**
 * @Column 속성
 *
 * name - DB 컬럼명 지정 가능, 디폴트는 객체의 필드 이름
 * insertable, updatable - 등록, 변경 가능 여부 -> 만약 false 로 두면 JPA 단에서는 절대 insert 되거나 update 되지 않는다.
 * nullable - null 값의 허용 여부를 설정한다. false 로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.
 * unique - @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. 잘 사용하지 않는다. 제약조건의 이름을 설정할 수 없기 때문, @Table uniqueConstraints 사용하자.
 * columnDefinition - 데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default 'EMPTY'
 * length - 문자 길이 제약조건, String 타입에만 사용한다. ex) length 를 10으로 설정하면 varchar 10 이 됨
 * precision, scale - 빅데시멀 타입에서 사용한다.
 */
