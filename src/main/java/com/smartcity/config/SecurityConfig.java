package com.smartcity.config;

import com.smartcity.security.CustomUserDetailsService;
import com.smartcity.security.JwtAuthenticationFilter;
import com.smartcity.util.CustomPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private com.smartcity.security.CustomAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private com.smartcity.security.CustomAccessDeniedHandler accessDeniedHandler;

    // 1. Defines the PasswordEncoder bean
    @Bean
    public PasswordEncoder passwordEncoder(CustomPasswordEncoder customPasswordEncoder) {
        return customPasswordEncoder;
    }

    // 2. Defines the AuthenticationManager bean, resolving the circular dependency.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3. Global CORS Configuration Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow all origins (for development - restrict in production)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose headers to the client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the configuration source
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Explicitly allow OPTIONS for all (CORS preflight)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/notifications/**").permitAll()

                        // 3. Broad controller matchers
                        .requestMatchers("/api/technicians/**").permitAll()
                        .requestMatchers("/api/citizens/**").authenticated() // Require auth for citizens
                        .requestMatchers("/api/equipment/**").permitAll()
                        .requestMatchers("/api/map/**").permitAll()

                        // 4. All other requests require a valid JWT token
                        .anyRequest().authenticated());

        // Add the JWT filter before the standard authentication filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}