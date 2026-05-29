package ru.nuclearius.finam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpMessageConverterAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // configuration.addAllowedOrigin("*");
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", configuration);
        return configSource;
    }
    
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(cts -> cts
                // .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers("/", "/index.html", "/login", "/assets/**", "/favicon.*").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs*/**").permitAll()
                .anyRequest().authenticated())
                // .csrf(cts -> cts.ignoringRequestMatchers(PathRequest.toH2Console()))
                .csrf(cts -> cts.disable())
                .headers(cts -> cts.frameOptions(focts -> focts.disable()))
                .exceptionHandling(cts -> cts
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        }))
                .cors(cts -> cts
                        .configurationSource(corsConfigurationSource()))
                .formLogin(customizer -> customizer
                        .loginPage("/auth")
                        .failureUrl("/error")
                        .defaultSuccessUrl("/main")
                        .successHandler(new HttpMessageConverterAuthenticationSuccessHandler())
                        .failureHandler(jsonAuthenticationFailureHandler()))
                // .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                // new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                // PathPatternRequestMatcher.withDefaults().matcher("/api/v1/**")))
                .build();
    }
 
     private AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF8");

            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("message", exception.getMessage());
            data.put("errorType", exception.getClass().getSimpleName());

            new ObjectMapper().writeValue(response.getWriter(), data);
        };
    }
}
