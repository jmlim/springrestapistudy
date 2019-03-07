package io.jmlim.springrestapistudy.events;

import lombok.*;

import java.time.LocalDateTime;

/***
 * 잭슨, jsonIgnore 같은걸로 입력제한을 둘 수 있으나 애노테이션이 많아질 수 있으므로 Dto로 따로 분리하는게 낫다.
 * Validation 관련한 애노테이션까지 생기면 헷갈릴 수 있음.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
}
