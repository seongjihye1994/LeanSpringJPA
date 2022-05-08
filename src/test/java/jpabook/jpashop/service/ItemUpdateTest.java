package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;
    
    @DisplayName("준영속 엔티티 테스트")
    public void ItemUpdateTest() {

        Book book = em.find(Book.class, 1L); // Book 찾기

        // TX
        book.setName("test"); // 이 시점에 Book에 수정이 일어났으니, 영속성 컨텍스트에서 수정되고, 커밋 시점에 DB에 자동으로 반영한다. -> 더티체킹

        // -> 더티 체킹! (변경 감지) -> 이 매커니즘으로 원하는 데이터를 바꿀 수 있다.
        // 편하고 좋은데, 문제는 준영속 엔티티 상태일 때 발생한다!
        // 준영속 엔티티 -> JPA 의 영속성 컨텍스트가 더이상 관리하지 않는 엔티티를 말한다.
        // TX Commit


    }
}
