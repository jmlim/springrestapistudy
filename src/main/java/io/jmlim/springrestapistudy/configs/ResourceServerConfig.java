package io.jmlim.springrestapistudy.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/**
 * 어떤 외부 요청이 Resource에 접근할 시 인증이 필요하다면 OAuth2 서버에서
 * 제공하는 토큰 서비스를 사용, 토큰이 유효한지 확인.
 * 토큰기반으로 인증정보가 있는지 없는지 확인하고 접근제어를 함.
 *
 * 보통은 분리..(Oauth 서버와 Resource서버는 분리함, 작은서비스는 같이쓰는 경우도 있음)
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    /**
     * 리소스의 아이디 같은것을 설정하는 곳.
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        // 리소스 아이디만 설정
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous()
                    .and()
                .authorizeRequests()
                    .mvcMatchers(HttpMethod.GET, "/api/**")
                        .anonymous()
                    .anyRequest().authenticated()
                    .and()
                // 인증이 잘못되었다거나 권한이 없을 때 예외 발생.
                .exceptionHandling()
                    .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
