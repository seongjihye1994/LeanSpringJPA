package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jni.Address;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class Delivery {

    private Long id;

    private Order order;

    private Address address;

    private DeliveryStatus status; // ENUM [READY(준비), COMP(배송)]


}
