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

        /**
         * 아이템의 아이디가 없는 경우?
         * -> 아이템 등록
         * 아이템의 아이디가 있는 경우?
         * -> 아이템 수정
         *
         * 즉, 아이템의 아이디가 있으면 해당 아이템을 수정하기 위한 로직이다.
         * 이 때는 영속성 컨텍스트의 1차 캐시에 이미 데이터가 존재하므로
         * persist 가 아닌 merge 를 시킨다.
         *
         * merge 란?
         *
         * 영속성 컨텍스트가 관리하지 않는 준영속 상태의 엔티티는
         * 값을 변경하더라도 DB가 업데이트 되지 않는다.
         *
         * 이렇게 준영속 상태의 엔티티를 영속성 컨텍스트가 더티체킹을
         * 자동으로 해주지 않기 때문에 DB와 싱크해주는 방법이 2가지가 있다.
         *
         * 그 중 하나가 merge 이다.
         */
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
