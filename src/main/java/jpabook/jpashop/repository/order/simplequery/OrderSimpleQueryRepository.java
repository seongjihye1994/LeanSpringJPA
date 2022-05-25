package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    @PersistenceContext
    private final EntityManager em;

    /*
    * 복잡한 조인 쿼리를 가지고 커스텀 Dto를 뽑아야 하는 경우에는
    * OrderSimpleQueryRepository 처럼 리포지토리를 별도로 하나 더 생성해서
    * 모아놓는다.
    *
    * 기존 repository 를 더럽히지 않으려는 목적도 있다.
    * 기존 리포지토리의 페치조인 쿼리(v3)는 재사용도 가능하기 때문에 유지보수에 좋지만,
    *
    * 지금 OrderSimpleQueryRepository 의 findOrderDtos 와 같은 원하는 필드만 select 해서 엔티티가 아닌,
    * 커스텀 dto로 바로 JPA가 끄집어 내도록 하는 기능은 해당 dto가 아니면
    * 재사용이 거의 불가능 하기 때문에
    *
    * 유지보수 및 클린코드를 위해서라도 별도로 분리하도록 설계하는 것이 좋다.
    *
    * 이렇게 별도로 리포지토리를 분리해서 사용하면 뭔가 특수한 상황에서만 써야한다는 느낌이 있기 때문에
    * 다른 개발자랑 같이 협업할때도 쉽게 유지보수가 가능하겠죠?
    *
    *
    * 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다.
    * 둘중 상황에 따라서 더 나은 방법을 선택하면 된다.
    * 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.
    * 하지만 DTO로 조회하면 원하는 필드만 가져와서 사용할 수 있으므로, 페치조인보다는 select 필드 개수가 적어지기 때문에 성능이 죄금 더 좋아진다.
    * 따라서 권장하는 방법은 다음과 같다.

    *   쿼리 방식 선택 권장 순서

    1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
    2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 여기서 해결된다.
    3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
    4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
    *
    * */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        // 쿼리문에서 엔티티가 아닌 커스텀 dto로 변환해서 jpa가 바로 끄집어내게 하려면 쿼리문에 아래처럼 new~ 로 적어줘야 한다.
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) "
                                + " from Order o"
                                + " join o.member m"
                                + " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
