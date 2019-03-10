package io.jmlim.springrestapistudy.common;

import io.jmlim.springrestapistudy.index.IndexController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ErrorsResource extends Resource<Errors> {
    public ErrorsResource(Errors content, Link... links) {
        super(content, links);
        //리소스로 변환 시 index 에 대한 링크 추가.
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}

