package ru.nuclearius.finam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpMessageConverterAuthenticationSuccessHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(cts -> cts
                .requestMatchers("/api/v1/**").permitAll()
                .anyRequest().permitAll())
                // .csrf(cts -> cts.ignoringRequestMatchers(PathRequest.toH2Console()))
                .csrf(cts -> cts.disable())
                .headers(cts -> cts.frameOptions(focts -> focts.disable()))
         
                .formLogin(customizer -> customizer
                        .loginPage("/auth")
                        .failureUrl("/error")
                        .defaultSuccessUrl("/main")
                        .successHandler(new HttpMessageConverterAuthenticationSuccessHandler())
                        )
                // .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                // new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                // PathPatternRequestMatcher.withDefaults().matcher("/api/v1/**")))
                .build();
    }
 
}
