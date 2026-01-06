package com.ydg.bilet.security;

import com.ydg.bilet.entity.Kullanici;
import com.ydg.bilet.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        if (!Boolean.TRUE.equals(kullanici.getAktif())) {
            throw new UsernameNotFoundException("Kullanıcı pasif");
        }

        String role = (kullanici.getRole() == null) ? "USER" : kullanici.getRole().name();

        return new User(
                kullanici.getEmail(),
                kullanici.getSifre(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
