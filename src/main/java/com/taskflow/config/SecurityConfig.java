
package com.taskflow.config;

import com.taskflow.service.GoogleOidcUserService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final GoogleOidcUserService googleOidcUserService;
    private final GoogleOAuth2SuccessHandler googleSuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            GoogleOidcUserService googleOidcUserService,
            GoogleOAuth2SuccessHandler googleSuccessHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.googleOidcUserService = googleOidcUserService;
        this.googleSuccessHandler = googleSuccessHandler;
    }

    // Google OAuth2 login and callback chain
    @Bean
    @Order(1)
    public SecurityFilterChain oauthSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .securityMatcher(
                        "/oauth2/**",
                        "/login/oauth2/**"
                )
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(
                                        googleOidcUserService
                                )
                        )
                        .successHandler(googleSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"message\":\"Google authentication failed\"}"
                            );
                        })
                );

        return http.build();
    }

    // Existing stateless JWT API chain
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}
