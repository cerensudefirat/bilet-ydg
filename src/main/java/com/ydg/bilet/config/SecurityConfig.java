package com.ydg.bilet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("YDG Bilet");

        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/index.html", "/ui/**", "/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/etkinlik/**", "/api/mekan/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/admin-requests/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/admin-requests/pending/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests/*/reject").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/mekan/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/etkinlik/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint));

        return http.build();
    }
}
