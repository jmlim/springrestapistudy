package io.jmlim.springrestapistudy.events;

import io.jmlim.springrestapistudy.accounts.Account;
import io.jmlim.springrestapistudy.accounts.CurrentUser;
import io.jmlim.springrestapistudy.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

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

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors, @CurrentUser Account currentUser) {
        // Validation 에러 발생 시 BadRequest 처리.
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // 데이터 검증 테스트
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // modelMapper를 사용해서 빌더통해 다 옮기지 않고 한번에 옮긴다.
        Event event = modelMapper.map(eventDto, Event.class);

        //유료인지 무료인지, 온라인인지 오프라인인지 변경.
        event.update();
        // 매니저 정보 현재유저로 설정가능.
        event.setManager(currentUser);

        // event는 모델 매퍼를 통해 새로 만든 객체.
        Event newEvent = eventRepository.save(event);


        // 링크를 추가하는 부분
        //HATEOAS가 제공하는 linkTo() 와 methodOn() 사용
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        // 셀프링크는 EventResource 안에 있으므로 주석처리.
        //eventResource.add(selfLinkBuilder.withSelfRel());
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-event-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      // @AuthenticationPrincipal User user) {
                                      // @AuthenticationPrincipal AccountAdapter currentUser) {
                                      // @AuthenticationPrincipal(expression = "account") Account account) {
                                      @CurrentUser Account account) {

        Page<Event> page = this.eventRepository.findAll(pageable);
        //페이지와 관련된 링크 정보들도 같이 넘겨줌 (현재페이지, 이전페이지, 다음페이지, ...)
        //- Event를 EventResource로 변환해서 받기
        //    - 각 이벤트 마다 self (  e -> new EventResource(e) )
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page, e -> new EventResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        if(account != null) {
            pagedResources.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id,
                                   @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);

        //Anti pattern...
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
        // 글을 쓴 유저와 동일한 경우 update link를 줄 수 있음.
        if(event.getManager().equals(currentUser)) {
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validation 에러 발생 시 BadRequest 처리.
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // 데이터 검증 테스트
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();
        if(!existingEvent.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(eventDto, existingEvent);

        Event savedEvent = this.eventRepository.save(existingEvent);
        //유료인지 무료인지, 온라인인지 오프라인인지 변경.
        savedEvent.update();

        EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }
}
