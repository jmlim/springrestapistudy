package io.jmlim.springrestapistudy.events;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 자바빈 스펙
 * 각각의 필드 게터 세터가 있어야 함.
 * 생성자가 있어야함.
 * 여기선 롬복을 사용하여 처리.
 */

/**
 * Control + Shift + t 이용해서 테스트 코드로 이동 가능.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
// 나중에 엔티티간의 연간관계가 있을 때 상호참조가 일어날경우 스택오버플로우가 발생하는 경우 발생함.
// 다른걸 추가해도 생관없으나 연관관계의 묶음을 추가하는 것은 좋지 않다. 서로간의 메소드 계속 호출..할 수 있다는것.
//  @Data를 쓰지 않는 이유가 위의 내용 때문..
@EqualsAndHashCode(of = "id")
@Entity
public class Event {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    /***
     * 스프링부트 2.1부터는 jpa 3.2를 지원하므로 LocalDateTime도 기본값으로 지원함.
     */
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;
}
