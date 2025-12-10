package com.deloitte.Auth_Service.util;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Custom PasswordEncoder that accepts any clientSecret from the Basic Auth header.
 */
public class AcceptAnyPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Accept any clientSecret from Basic Auth
        return true;
    }
}

