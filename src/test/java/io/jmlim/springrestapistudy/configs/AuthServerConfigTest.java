package io.jmlim.springrestapistudy.configs;

import io.jmlim.springrestapistudy.accounts.Account;
import io.jmlim.springrestapistudy.accounts.AccountRole;
import io.jmlim.springrestapistudy.accounts.AccountService;
import io.jmlim.springrestapistudy.common.AppProperties;
import io.jmlim.springrestapistudy.common.BaseControllerTest;
import io.jmlim.springrestapistudy.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {

        /*String username = "hackerljm1@naver.com";
        String password = "1234";*/
        //Given

        //AppConfig 에서 user@email.com 에 대한 계정을 만들었으므로 주석처리.
        /*Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                .build();

        this.accountService.saveAccount(account);*/
/*
        String clientId = "myApp";
        String clientSecret = "pass";*/

        this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
        ;

    }
}