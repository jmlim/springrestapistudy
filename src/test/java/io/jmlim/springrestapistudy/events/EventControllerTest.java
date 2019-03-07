package io.jmlim.springrestapistudy.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 우리가 만든 테스트가 슬라이스 테스트이므로 웹용 빈만 등록해줌.
     * 해당 빈을 @MockBean을 통해 Mocking
     */
     /* @MockBean
    EventRepository eventRepository;*/
    @Test
    public void createEvent() throws Exception {

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

        // mock 리포지토리를 받았으므로 이벤트 리포지토리가 세이브가 호출이 되면 이벤트를 그대로 리턴하게 해좀.
        // event는 모델 매퍼를 통해 새로 만든 객체. 파라미터의 event가 있어야 하는데 그게 아니므로.. 그래서 실제 데이터가 있다 가정하고 모킹을 지움.
        // @SrpingBootTest로 실제 테스트
        // Mockito.when(eventRepository.save(event)).thenReturn(event);

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
                .andExpect(jsonPath("id").exists())
                //.andExpect(header().exists("Location"))
                //.andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"));
                //type safe 하게 작성하기.
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))

                // =======위에서 Dto를 통해 입력제한을 두웠으므로 100을 입력했다 하더라도 실제 값은 100이 아님.
                .andExpect(jsonPath("id").value(Matchers.not(100)))  //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
                .andExpect(jsonPath("free").value(Matchers.not(true))) //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name())); //입력제한 두지 않으면 위에 적용한대로 입력이 되어 테스트 깨딤
    }

    /***
     * 잭슨, jsonIgnore 같은걸로 입력제한을 둘 수 있으나 애노테이션이 많아질 수 있으므로 Dto로 따로 분리하는게 낫다.
     * Validation 관련한 애노테이션까지 생기면 헷갈릴 수 있음.
     */
}
