package com.smartcity.config;

import com.smartcity.security.CustomUserDetailsService;
import com.smartcity.security.JwtAuthenticationFilter;
import com.smartcity.util.CustomPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // NEW IMPORT
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // NEW IMPORT
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // 1. Defines the PasswordEncoder bean
    @Bean
    public PasswordEncoder passwordEncoder(CustomPasswordEncoder customPasswordEncoder) { 
        return customPasswordEncoder;
    }

    // 2. Defines the AuthenticationManager bean, resolving the circular dependency.
    // Spring now finds CustomUserDetailsService and PasswordEncoder beans automatically.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    // 🛑 REMOVED: The @Autowired public void configure(AuthenticationManagerBuilder auth) method.

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()             
                .requestMatchers("/api/public/**").permitAll()          
                .anyRequest().authenticated()                          
            );

        // Add the JWT filter before the standard authentication filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}