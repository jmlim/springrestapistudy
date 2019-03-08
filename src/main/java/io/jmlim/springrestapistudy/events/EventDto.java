package io.jmlim.springrestapistudy.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private LocalDateTime beginEnrollmentDateTime;
    @NotNull
    private LocalDateTime closeEnrollmentDateTime;
    @NotNull
    private LocalDateTime beginEventDateTime;
    @NotNull
    private LocalDateTime endEventDateTime;
    private String location;
    @Min(0)
    private int basePrice;
    @Min(0)
    private int maxPrice;
    @Min(0)
    private int limitOfEnrollment;
}
