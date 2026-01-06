package com.ydg.bilet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ✅ Actuator public
                        .requestMatchers("/actuator/**").permitAll()

                        // ✅ Swagger public
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // ✅ Auth açık
                        .requestMatchers("/auth/**").permitAll()

                        // ✅ Public etkinlik
                        .requestMatchers(HttpMethod.GET, "/api/etkinlik/**").permitAll()

                        // ✅ Admin etkinlik CRUD
                        .requestMatchers(HttpMethod.POST, "/api/admin/etkinlik/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/etkinlik/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/etkinlik/**").hasRole("ADMIN")

                        // ✅ Admin request yönetimi
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/admin-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin-requests/**").hasRole("ADMIN")

                        // ✅ Bilet/Ani
                        .requestMatchers("/api/bilet/**").authenticated()
                        .requestMatchers("/api/ani/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
