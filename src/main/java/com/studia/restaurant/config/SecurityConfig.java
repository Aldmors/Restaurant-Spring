package com.studia.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures a {@link SecurityFilterChain} for securing HTTP requests.
     *
     * This method defines authorization rules, OAuth2 resource server configuration,
     * session management policy, and disables CSRF protection.
     *
     * @param http the {@link HttpSecurity} object used to configure the HTTP security settings
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.GET, "/api/photos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/restaurants/**").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Provides a JwtAuthenticationConverter bean for use in JWT-based authentication
     * within the security configuration. This converter is responsible for converting
     * a JWT into an Authentication object.
     *
     * @return an instance of JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

}
