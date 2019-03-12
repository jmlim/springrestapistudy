package io.jmlim.springrestapistudy.configs;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    //Application.class 에 있던 modelMapper 설정 분리.
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        /**
         * 스프링 부트 최신버전에 추가.
         * prefix에 따라 적절한 인코딩 사용.
         */
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
