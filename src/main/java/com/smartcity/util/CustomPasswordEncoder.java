package com.smartcity.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder; // This is the official interface

@Component
public class CustomPasswordEncoder implements PasswordEncoder { // 🛑 RENAMED CLASS

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); 

    @Override
    public String encode(CharSequence rawPassword) {
        return encoder.encode(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword.toString(), encodedPassword);
    }
    
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }
}