package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // 컴포넌트 스캔의 대상이 됨 ->  스프링 빈으로 등록됨
// JPA 조회 성능 최적화, 데이터의 변경은 트랜잭션 안에서 실행되어야 함
// class 레벨에서 사용하면 public 메소드들은 모두 트랜잭션이 적용된다.
@Transactional(readOnly = true)
// @AllArgsConstructor // 모든 멤버변수를 대상으로 생성자를 만들어준다. (아래 생성자 코드를 적지 않아도 된다!)
@RequiredArgsConstructor // final 이 붙은 멤버변수를 대상으로 생성자를 만들어준다!
public class ItemService {

    private final ItemRepository itemRepository;

    // 세터 인젝션은 테스트는 용이하지만, 외부에서 세터를 호출해 값을 수정할 수 있기 때문에 위험하다. -> 사용x
    // 생성자 인젝션을 사용하자.
    // 테스트도 용이하며, 외부 수정도 불가능하고, 런타임까지 가지 않고
    // 컴파일에서 오류를 알려줘 의존성 빈을 미리 설정할 수 있다.
    // @Autowired // 생성자가 1개인 경우는 @Autowired 생략 가능
    /*public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }*/

    /**
     * 아이템 저장
     * @param item
     */
    @Transactional // 저장 트랜잭션은 readOnly x!!
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    /** 준영속 엔티티를 수정하는 2가지 방법
     * 1. 변경 감지 기능 사용
     * @param itemId
     * @param param
     */
    @Transactional
    public Item updateItem(Long itemId, Book param) {
        // 실제 DB 에 있는 값, 영속 상태의 엔티티를 찾아옴
        Item findItem = itemRepository.findOne(itemId);

        // 찾아온 객체를 수정함
        findItem.setPrice(param.getPrice());
        findItem.setName(param.getName());
        findItem.setStockQuantity(param.getStockQuantity());

        // 이렇게 객체를 수정해주면 @Transactional 이 실행되면서
        // flush() 가 이루어지고, 영속성 컨텍스트와 DB가 싱크된다.
        // 즉, 위에서 바꿔준 값으로 DB에 쿼리를 날린다.

        // 이렇게 준영속 상태의 엔티티는 자동으로 더티체킹을 해줄 수 없기 때문에
        // 메소드를 사용해 직접 수정을 해준 후 객체를 리턴한다.

        // 이처럼 수동으로 값을 수정해서 리턴해주는 메소드 방식이 있고,
        // merge() 를 호출하면 이 메소드랑 똑같은 기능을 하지만, 직접 작성하지 않아도 되는 이점이 있다.

        // 하지만! 실무에서는 merge 를 사용하면 안된다.
        // 왜? 우선 병합 동작 방식을 살펴보면 아래와 같다.

        // 병합시 동작 방식을 간단히 정리
        // 1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
        // 2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
        // 3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행

        // 핵심은 2번의 준영속 엔티티의 값으로 '모두 교체'이다.
        // 변경 감지 기능을 사용하면 원하는 속성(필드)만 선택해서 변경할 수 있지만,
        // 병합을 사용하면 모든 속성이 변경된다!!!!!!!!!!
        // 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)

        // 그래서 실무에서는 merge를 사용하지 말고, 변경 감지 메소드를 직접 만들어서 사용하자!
        return findItem;

    }

    /**
     * 모든 아이템 조회
     * @return
     */
    // 트랜잭셔널이 클래스레벨에 readOnly로 적용되어 있어서 생략
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /**
     * 아이템 하나 조회
     * @param itemId
     * @return
     */
    // 트랜잭셔널이 클래스레벨에 readOnly로 적용되어 있어서 생략
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}
