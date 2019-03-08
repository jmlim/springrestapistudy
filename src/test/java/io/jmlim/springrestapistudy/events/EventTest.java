package io.jmlim.springrestapistudy.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
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

    @Test
   /* @Parameters({
            // 아래 방법은 typeSafe 하지 않다는 단점 존재
            "0, 0, true",
            "100, 0, false",
            "0, 100, false"
    })*/
    @Parameters//(method = "paramsForTestFree")
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // Given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    /**
     * typeSafe하게 정의하기.
     */
    // private Object[] paramForTestFree() {
    // 메소드명을 컨벤션(parametersFor) 에 맞춰주면 위 @Parameters 어노테이션에 method 에 메소드이름 지정 해주지 않아도 됨.
    private Object[] parametersForTestFree() {
        return new Object[]{
                new Object[]{0, 0, true},
                new Object[]{100, 0, false},
                new Object[]{0, 100, false},
                new Object[]{100, 200, false}
        };
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean isOffline) {
        // Given
        Event event = Event.builder()
                .location(location)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }

    private Object[] parametersForTestOffline() {
        return new Object[]{
                new Object[]{"강남역 네이버 D2 스타트업팩토리", true},
                new Object[]{null, false},
                new Object[]{"               ", false}
        };
    }
}