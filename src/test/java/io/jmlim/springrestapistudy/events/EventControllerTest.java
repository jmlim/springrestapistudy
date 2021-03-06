package io.jmlim.springrestapistudy.events;

import io.jmlim.springrestapistudy.accounts.Account;
import io.jmlim.springrestapistudy.accounts.AccountRepository;
import io.jmlim.springrestapistudy.accounts.AccountRole;
import io.jmlim.springrestapistudy.accounts.AccountService;
import io.jmlim.springrestapistudy.common.AppProperties;
import io.jmlim.springrestapistudy.common.BaseControllerTest;
import io.jmlim.springrestapistudy.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    /**
     * 테스트 디비가 인메모리 디비긴하나.. 테스트간에는 서로 디비를 공유하기 때문에
     * 데이터가 공유가 되버리니.. 처리
     */
    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    /**
     * 우리가 만든 테스트가 슬라이스 테스트이므로 웹용 빈만 등록해줌.
     * 해당 빈을 @MockBean을 통해 Mocking
     */
     /* @MockBean
    EventRepository eventRepository;*/
    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto eventDto = EventDto.builder()
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
        // event는 모델 매퍼를 통해 새로 만든 객체. 파라미터의 event가 있어야 하는데 그게 아니므로.. 그래서 실제 데이터가 있다 가정하고 모킹을 지움.
        // @SpringBootTest로 실제 테스트
        // Mockito.when(eventRepository.save(event)).thenReturn(event);

        /**
         * JSON 응답으로 201이 나오는지 확인.
         * Location 헤더에 생성된 이벤트를 조회할 수 있는 URI 담겨 있는지 확인.
         * id는 DB에 들어갈 때 자동생성된 값으로 나오는지 확인.
         */
        mockMvc.perform(post("/api/events/")
                // 헤더에 인증 토큰 추가.
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                // 객체를 json 문자열로 변경 후 본문에 넣음.
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                //.andExpect(header().exists("Location"))
                //.andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"));
                //type safe 하게 작성하기.
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))

                // =======위에서 Dto를 통해 입력제한을 두웠으므로 100을 입력했다 하더라도 실제 값은 100이 아님.
                .andExpect(jsonPath("id").value(Matchers.not(100)))  //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
                .andExpect(jsonPath("free").value(false)) //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name())) //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                /****
                                 * {
                                 *   "name" : "Spring",
                                 *   "description" : "REST API Development with Spring",
                                 *   "beginEnrollmentDateTime" : "2018-11-23T14:21:00",
                                 *   "closeEnrollmentDateTime" : "2018-11-24T14:21:00",
                                 *   "beginEventDateTime" : "2018-11-25T14:21:00",
                                 *   "endEventDateTime" : "2018-11-26T14:21:00",
                                 *   "location" : "강남역 D2 스타트업 팩토리.",
                                 *   "basePrice" : 100,
                                 *   "maxPrice" : 200,
                                 *   "limitOfEnrollment" : 100
                                 * }
                                 */
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("찹찹"),
                                fieldWithPath("endEventDateTime").description("이벤트가끝난시간"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("기본가격"),
                                fieldWithPath("maxPrice").description("가장 큰 가격"),
                                fieldWithPath("limitOfEnrollment").description("테스트")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        //   "manager" : null 이 나와서 relaxedResponseFields로 변경.
                        relaxedResponseFields(
                                //responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("찹찹"),
                                fieldWithPath("endEventDateTime").description("이벤트가끝난시간"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("기본가격"),
                                fieldWithPath("maxPrice").description("가장 큰 가격"),
                                fieldWithPath("limitOfEnrollment").description("테스트"),
                                fieldWithPath("free").description("프리여부"),
                                fieldWithPath("offline").description("오프라인여부"),
                                fieldWithPath("eventStatus").description("이벤트 상태"),

                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query event list"),
                                fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
        ;
        /***
         * 잭슨, jsonIgnore 같은걸로 입력제한을 둘 수 있으나 애노테이션이 많아질 수 있으므로 Dto로 따로 분리하는게 낫다.
         * Validation 관련한 애노테이션까지 생기면 헷갈릴 수 있음.
         */
    }

    public String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "Bearer " + this.getAccessToken(needToCreateAccount);
    }

    public String getAccessToken(boolean needToCreateAccount) throws Exception {
        /* 동일한 계정을 계속 생성하는 문제로 인해 @Before에서 리포지토리 부분 삭제한 부분 있음..*/
        /** 위 이유로 인해 AppConfig의 ApplicationRunner 에서 계정 생성한것이 날아가므로 계정 추가하는 부분 남겨야함. */
/*        String username = "hackerljm1@naver.com";
        String password = "1234";*/
        //Given
        if (needToCreateAccount) {
            createAccount();
        }
        /*
        String clientId = "myApp";
        String clientSecret = "pass";*/

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));

        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    private Account createAccount() {
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                .build();

        return this.accountService.saveAccount(account);
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 이러게 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100) //입력제한 둬야함.
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
                .free(true) //입력제한 둬야함.
                .offline(false) //입력제한 둬야함.
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                // 객체를 json 문자열로 변경 후 본문에 넣음.
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                // 날짜 이상하게 셋팅 (시작날짜가 크도록)
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 19, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 11, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                // 기본 가격이 10000인데 maxPrice가 200 ???
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리.")
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                //.andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                //.andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("content[0].rejectedValue").exists())
                //입력값이 잘못되었을때 나오는 경로이므로
                .andExpect(jsonPath("_links.index").exists())
        ;
        /***
         * [
         *     {
         *         "field": "endEventDateTime",
         *         "objectName": "eventDto",
         *         "code": "wrongValue",
         *         "defaultMessage": "endEventDateTime is wrong",
         *         "rejectedValue": "2018-11-23T14:21"
         *     },
         *     {
         *         "objectName": "eventDto",
         *         "code": "wrongPrices",
         *         "defaultMessage": "values for prices are wrong"
         *     }
         * ]
         */

        /** links index 추가 후 jsonArray 가 되었다. 그럴경우 unwrapped 가 되지 않으므로 아래와 같이 리턴됨
         * 그래서 기존 $[0]을 -> content[0] 으로 변경함
         * {
         *     "content": [
         *         {
         *             "field": "endEventDateTime",
         *             "objectName": "eventDto",
         *             "code": "wrongValue",
         *             "defaultMessage": "endEventDateTime is wrong",
         *             "rejectedValue": "2018-11-23T14:21"
         *         },
         *         {
         *             "objectName": "eventDto",
         *             "code": "wrongPrices",
         *             "defaultMessage": "values for prices are wrong"
         *         }
         *     ],
         *     "_links": {
         *         "index": {
         *             "href": "http://localhost:8080/api"
         *         }
         *     }
         * }
         */
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기.(인증정보 없음)")
    public void queryEvents() throws Exception {
        // Given (이벤트 30개 만들어야함.
        /*IntStream.range(0, 30).forEach(i -> {
            this.generateEvent(i);
        });*/
        IntStream.range(0, 30).forEach(this::generateEvent);


        //When
        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        //나머지 문서화도 해야하나 강의에선 생략.. (필드, 헤더, 링크스에 대한 설명 등)
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기.(인증정보 있음)")
    public void queryEventsWithAuthentication() throws Exception {
        // Given (이벤트 30개 만들어야함.
        /*IntStream.range(0, 30).forEach(i -> {
            this.generateEvent(i);
        });*/
        IntStream.range(0, 30).forEach(this::generateEvent);


        //When
        this.mockMvc.perform(get("/api/events")
                //인증정보 추가.
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                // 인증정보가 있을 시 create-event 링크 나오도록
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events"))
        //나머지 문서화도 해야하나 강의에선 생략.. (필드, 헤더, 링크스에 대한 설명 등)
        ;
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
        //Given
        Account account = this.createAccount();
        Event event = this.generateEvent(100, account);

        //When & then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        // 문서화 필요하나 생략...
        ;
    }

    @Test
    @TestDescription("없는 이벤트를 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception {
        //When & then
        this.mockMvc.perform(get("/api/events/12345"))
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index, Account account) {
        Event event = buildEvent(index);
        event.setManager(account);
        return this.eventRepository.save(event);
    }

    private Event generateEvent(int index) {
        Event event = buildEvent(index);
        return this.eventRepository.save(event);
    }

    private Event buildEvent(int index) {
        return Event.builder()
                .name("event " + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리.")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();
    }

    @Test
    @TestDescription("정상적으로 이벤트 수정")
    public void updateEvent() throws Exception {
        //Given
        Account account = this.createAccount();
        Event event = this.generateEvent(100, account);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "수정한 이벤트";
        eventDto.setName(eventName);

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                // 얘는 계정을 새로 만들필요가 없으므로..
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(false))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event"))
        //문서화 필요.
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        //Given
        Event event = this.generateEvent(100);

        EventDto eventDto = new EventDto();

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못되어있는 경우")
    public void updateEvent400_Wrong() throws Exception {
        //Given
        Event event = this.generateEvent(100);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        //When & then
        this.mockMvc.perform(put("/api/events/12345")
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        //Given
        Event event = this.generateEvent(100);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        //When & then
        this.mockMvc.perform(put("/api/events/12345")
                .header(HttpHeaders.AUTHORIZATION, this.getBearerToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}




