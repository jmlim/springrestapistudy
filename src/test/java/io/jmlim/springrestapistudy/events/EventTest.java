package io.jmlim.springrestapistudy.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void builder() {
        // 빌더를 쓰면 좋은 점.
        // 내가 입력한 값이 뭔지 알 수 있음.
        Event event = Event.builder()
                .name("Infleran Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        //Given
        Event event = new Event();
        String name = "Event";
        String description = "Spring";

        //When
        event.setName(name);
        event.setDescription(description);

        //Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }
}