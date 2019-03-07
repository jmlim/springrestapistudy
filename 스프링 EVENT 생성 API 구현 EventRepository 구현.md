# Event 생성 API 구현: EventRepository 구현

## 스프링 데이터 JPA
 - JpaRepository 상속 받아 만들기

## Enum을 JPA 맵핑시 주의할 것.
```java
@Enumerated(EnumType.STRING)
    private EventStatus eventStatus;
```

## @MockBean
 - Mockito 사용해서 mock객체를 만들고 빈으로 등록해 줌.
 - (주의) 기존 빈을 테스트용 빈이 대체 한다.

## 테스트 할 것
 - 입력값들을 전달하면 JSON 응답으로 201이 나오는지 확인
   - Location 헤더에 생성된 이벤트를 조회할 수 있는 URI 담겨 있는지 확인
   - id는 DB에 들어갈 때 자동생성된 값으로 나오는지 확인

```java
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
    private EventStatus eventStatus;
}

```


```java

@RunWith(SpringRunner.class)
@WebMvcTest
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 우리가 만든 테스트가 슬라이스 테스트이므로 웹용 빈만 등록해줌.
     * 해당 빈을 @MockBean을 통해 Mocking
     */
    @MockBean
    EventRepository eventRepository;

    @Test
    public void createEvent() throws Exception {

        Event event = Event.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리.")
                .build();

        // mock 리포지토리를 받았으므로 이벤트 리포지토리가 세이브가 호출이 되면 이벤트를 그대로 리턴하게 해좀.
        event.setId(10);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        /**
         * JSON 응답으로 201이 나오는지 확인.
         * Location 헤더에 생성된 이벤트를 조회할 수 있는 URI 담겨 있는지 확인.
         * id는 DB에 들어갈 때 자동생성된 값으로 나오는지 확인.
         */
        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                // 객체를 json 문자열로 변경 후 본문에 넣음.
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists());
    }
}
```