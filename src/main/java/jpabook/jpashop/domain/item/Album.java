package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A") // 부모 클래스가 자식 클래스를 구분할 수 있도록 부모 컬럼의 dtype에 값 지정
@Getter
@Setter
public class Album extends Item {

    private String artist;
    private String etc;

}
