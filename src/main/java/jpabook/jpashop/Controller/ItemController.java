package jpabook.jpashop.Controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
     *
     * 정적 팩토리 메서드를 Book 클래스 내부에 생성
     * 하지만 여전히 book 객체를 만들기 위해서는 setter 불가피
     *
     * setter 를 닫으면서 정적 팩토리 메소드를 작성하기 위한 두가지 방법
     * 1. 생성자를 protected로 생성, 정적 팩토리 메소드에서 setter가 아닌 생성자로 객체를 생성하는 방법
     * 2. setter 를 private로 생성, 정적 팩토리 메소드 안에서믄 setter 를 사용하게 허가하는 방법
     *
     * 둘 중 편한거 사용하기.
     *
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
}
