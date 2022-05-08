package jpabook.jpashop.Controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {

        model.addAttribute("form", new BookForm());

        return "/items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {

        Book book = createBook(form);

        itemService.saveItem(book);

        return "redirect:/";
    }

    /**
     * 실무에서는 setter 를 모두 private 으로 막아놓자!!
     * <p>
     * 정적 팩토리 메서드를 Book 클래스 내부에 생성
     * 하지만 여전히 book 객체를 만들기 위해서는 setter 불가피
     * <p>
     * setter 를 닫으면서 정적 팩토리 메소드를 작성하기 위한 두가지 방법
     * 1. 생성자를 protected로 생성, 정적 팩토리 메소드에서 setter가 아닌 생성자로 객체를 생성하는 방법
     * 2. setter 를 private로 생성, 정적 팩토리 메소드 안에서믄 setter 를 사용하게 허가하는 방법
     * <p>
     * 둘 중 편한거 사용하기.
     * <p>
     * 추가로, 위의 두 방법 모두 setter 의 부재로 변경 감지 (더티 체킹) 사용하지 못하는 문제점 발생.
     * 그래서 setter 대신 비즈니스 메소드를 만들어 변경을 감지하고,
     * 정적 팩토리 메소드에서는 첫 번째 방법처럼 생성자로 객체를 생성하는 방법을 사용하자.
     */
    private static Book createBook(BookForm form) {

        Book book = new Book();

        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        return book;
    }

    /**
     * 상품 목록 조회
     *
     * @param model
     * @return
     */
    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItem();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * 수정할 해당 상품 페이지로 이동
     * @param itemId
     * @param model
     * @return
     */
    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);
        // 실무에서는 캐스팅 방법이 옳지 않다.
        // 예제에서는 Book 만 조회하기 때문에 Book 으로 캐스팅 해줬다.

        BookForm form = new BookForm(); // Book 엔티티가 아닌, BookForm DTO 를 사용
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);

        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {

        /**
         * 실무에서는 권한(해당 글 수정)이 필요하기 때문에 회원을 조회해서
         * 해당 회원이 이 글의 수정 권한이 있는지부터 체크해야 한다.
         *
         * 또한, 예제에서는 @PostMapping 을 사용했지만, 실무에서는
         * 가급적 수정은 Patch 을 사용해야 한다.
         * Put의 경우 전체 데이터를 다 변경할 때 사용한다.
         * 예제를 만들때는 이런 부분들까지 설명드리기는 어려워서 Post를 사용했다.
         * Post의 경우 등록, 수정 모든 곳에서 사용해도 된다.
         *
         */

        // 준영속 상태 엔티티 -> 객체는 분명 새로 만들었지만, id(pk) 가 이미 세팅되어 있다. -> 영속성 컨텍스트에서 관리 되었다가 분리된 상태다.
        Book book = new Book();

        book.setId(form.getId());
        // 그런데 분명... 위에서 new 로 Book 을 생성한 후
        // .setId 를 해주는게 어째서 id 가 이미 세팅되어 있다는 것인지?
        // 이 상황에서는 수정을 위해 html form에 데이터를 노출한 이후에 다시 new로 재조립된 엔티티이다.
        // -> 식별자를 기준으로 이미 한 번 영속상태가 되어 버린 인티티지만,
        // 더 이상 영속성 컨텍스트가 관리하지 않는 준영속 상태

        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/items";
    }

    /**
     * 핵심은 식별자를 기준으로 영속상태가 되어서 DB에 저장된 적이 있는가?
     *
     * 그래서 식별자를 기준으로 이미 한 번 영속상태가 되어 버린 엔티티가 있는데,
     * 더 이상 영속성 컨텍스트가 관리하지 않으면 모두 준영속 상태이다.
     *
     * 그게 em.detach() 를 해서 직접적으로 준영속 상태가 될 수 도 있고,
     * 지금처럼 수정을 위해 html form에 데이터를 노출한 이후에 다시 new 로 재조립된 엔티티 일 수도 있다.
     *
     * new 상태인 객체와 준영속 상태의 객체는 merge() 라는 명령에서 동작에 차이가 있다.
     * new 상태인 객체는 merge() 를 호출할 때 완전히 새로운 엔티티를 만든다.
     *
     * 반면 준영속 상태의 엔티티는 DB에서 기존 엔티티를 찾고 그 값을 준영속 상태의
     * 객체로 변경한 후 반환한다. 마치 준영속 상태의 객체가 영속 상태가 된 것 처럼.
     *
     * 영속성 컨텍스트에서 관리되고 있는 상태에서는 persist
     * 영속성 컨텍스트에서 분리돼 관리되고 있지 않은 상태에서는 merge 를 호출한다.
     *
     * 정리하자면,
     *
     * ## Transient 인지 Detachect 인지 어떻게 판단?
     *
     * - **엔티티의 @Id 프로퍼티 찾기**.
     *     - 이 Id 프로퍼티가 null 이면 Transient 상태로 판단하고
     *     - id 프로퍼티가 null 이 아니면 Detached 상태로 판단
     *
     * - 엔티티가 Persistable 인터페이스를 구현하고 있으면 isNew() 메소드에 위임한다.
     * - JpaRepositoryFactory 를 상속받는 클래스를 만들고 getEntityInfomation() 을
     *   오버라이딩해서 자신이 원하는 판단 로직을 구현할 수도 있다.
     *
     *   준영속 상태의 엔티티는 영속성 컨텍스트가 관리하지 않기 때문에 더티체킹(변경 감지)이 되지 않는다.
     *
     * 즉, 영속성 컨텍스트가 관리하지 않는 준영속 상태의 엔티티는 값을 변경하더라도 DB가 업데이트 되지 않는다.
     *
     * 이렇게 준영속 상태의 엔티티를 영속성 컨텍스트가 더티체킹을 자동으로 해주지 않기 때문에 DB와 싱크해주는 방법이 2가지가 있다.
     */
}
