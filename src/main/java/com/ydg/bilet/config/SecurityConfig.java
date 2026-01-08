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

        // Browser basic-auth popup yerine düzgün 401/403 akışı için
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("YDG Bilet");

        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 1) Statik & auth endpoints
                        .requestMatchers("/", "/index.html", "/ui/**", "/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 2) Public GET
                        .requestMatchers(HttpMethod.GET, "/api/etkinlik/**", "/api/mekan/**").permitAll()

                        // 3) USER -> Admin isteği (Senaryo2'nin kırıldığı yer burası)
                        // Kullanıcı admin olmak için istek atabilsin:
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests").authenticated()
                        // (opsiyonel) kullanıcı kendi isteğini görüyorsa:
                        .requestMatchers(HttpMethod.GET, "/api/admin-requests/**").authenticated()

                        // 4) ADMIN işlemleri
                        .requestMatchers(HttpMethod.GET, "/api/admin-requests/pending/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin-requests/*/reject").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/mekan/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/etkinlik/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 5) Diğer her şey login ister
                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint));

        return http.build();
    }
}
