package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
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

    /**
     * 모든 아이템 조회
     * @return
     */
    // 트랜잭셔널이 클래스레벨에 readOnly로 적용되어 있어서 생략
    public List<Item> findItem() {
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
