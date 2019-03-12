package io.jmlim.springrestapistudy.accounts;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUsername() {
        //Given
        String username = "hackerljm@naver.com";
        String password = "1234";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                .build();
        this.accountService.saveAccount(account);

        // When
        UserDetailsService userDetailsService = (UserDetailsService) accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        //assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(this.passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }

    /*
     *  이렇게만 하면 예외의 타입밖에 확인 못함.
     *  */
    /*@Test(expected = UsernameNotFoundException.class)
    public void findByUsernameFail() {
        String username = "jmlim@eyeq.co.kr";
        accountService.loadUserByUsername(username);
    }*/

    /**
     * 더 많은것을 테스트 할 수 있으나 코드가 장황해짐.
     */
   /* @Test
    public void findByUsernameFail() {
        String username = "jmlim@eyeq.co.kr";
        try {
            accountService.loadUserByUsername(username);
            fail("supposed to be failed");
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).containsSequence(username);
        }
    }*/
    /**
     * 더 많은것을 테스트 할 수 있으나 코드가 장황해짐.
     */
    @Test
    public void findByUsernameFail() {
        // Expected
        String username = "jmlim@eyeq.co.kr";
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        //When
        accountService.loadUserByUsername(username);
    }
}