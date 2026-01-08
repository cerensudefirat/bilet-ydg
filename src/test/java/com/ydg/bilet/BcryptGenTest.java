package com.ydg.bilet;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenTest {
    @Test
    void gen() {
        System.out.println(new BCryptPasswordEncoder().encode("adminpass"));
    }
}
