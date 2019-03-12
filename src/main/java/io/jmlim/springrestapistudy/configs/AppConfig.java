package io.jmlim.springrestapistudy.configs;

import io.jmlim.springrestapistudy.accounts.Account;
import io.jmlim.springrestapistudy.accounts.AccountRole;
import io.jmlim.springrestapistudy.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {
            @Autowired
            AccountService accountService;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account account = Account.builder()
                        .email("jeongmuklim@naver.com")
                        .password("1234")
                        .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                        .build();
                accountService.saveAccount(account);
            }
        };
    }
}
