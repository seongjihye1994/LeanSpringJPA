package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    @PersistenceContext
    private final EntityManager em;

    // 상품 저장
    public void save(Item item) {

        if (item.getId() == null) {
            em.persist(item); // item은 JPA가 저장하기 전까지는 id가 없으므로 id값이 없으면 완전 새로 생성한 객체라는 의미, id가 있다면 이미 db에 등록되어 있는 값을 들고 온 것
        } else {
            em.merge(item); // id가 있다면 강제 업데이트
        }
    }

    // 상품 1개 조회
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    // 상품 전체 조회
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }

}
