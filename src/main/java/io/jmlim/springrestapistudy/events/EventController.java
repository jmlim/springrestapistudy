package io.jmlim.springrestapistudy.events;

import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;


    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        // Validation 에러 발생 시 BadRequest 처리.
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // 데이터 검증 테스트
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // modelMapper를 사용해서 빌더통해 다 옮기지 않고 한번에 옮긴다.
        Event event = modelMapper.map(eventDto, Event.class);

        //유료인지 무료인지, 온라인인지 오프라인인지 변경.
        event.update();

        // event는 모델 매퍼를 통해 새로 만든 객체.
        Event newEvent = eventRepository.save(event);
        //HATEOAS가 제공하는 linkTo() 와 methodOn() 사용
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        return ResponseEntity.created(createdUri).body(event);
    }
}
