package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * 총 주문 2개 생성되어야 함
 *
 * 1. userA
 *  JPA1 BOOK
 *  JPA2 BOOK
 *
 * 2. userB
 *  SPRING1 BOOK
 *  SPRING2 BOOK
 */
@Component // 컴포넌트 스캔의 대상 -> 스프링 빈으로 등록
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct // 빈 초기화 어노테이션
    public void init() {
        initService.dbInit1(); // InitDb 빈이 생성(스프링빈 컨테이네어 등록)되고, 의존 주입이 완료된 후 사용 직전에 호출됨.
        initService.dbInit2(); // InitDb 빈이 생성(스프링빈 컨테이네어 등록)되고, 의존 주입이 완료된 후 사용 직전에 호출됨.
    }



    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;


        /**
         * userA 가 JPA1, JPA2 를 주문
         */
        public void dbInit1() {
            Member member = createMember("userA", "서울", "1", "1111");

            // 멤버 영속화
            em.persist(member);

            // 북1 생성
            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            // 북1 영속화
            em.persist(book1);

            // 북2 생성
            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            // 북2 영속화
            em.persist(book2);

            // 주문 아이템 생성
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            // 배송 생성
            Delivery delivery = createDelivery(member);

            // 주문
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);

            // 주문 영속화
           em.persist(order);
        }

        /**
         * userB 가 SPRING BOOK1, SPRING BOOK2 를 주문
         */
        public void dbInit2() {

            // 멤버 생성
            Member member = createMember("userB", "진주", "2", "2222");
            // 멤버 영속화
            em.persist(member);

            // 북1 생성
            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            // 북1 영속화
            em.persist(book1);

            // 북2 생성
            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            // 북2 영속화
            em.persist(book2);

            // 주문 아이템 생성
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            // 배송 생성
            Delivery delivery = createDelivery(member);

            // 주문
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);

            // 주문 영속화
            em.persist(order);
        }



        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }


        private Member createMember(String name, String city, String street, String zipcode) {
            // 멤버 생성
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }


    }
}


