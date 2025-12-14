package com.smartcity.security;

import com.smartcity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // 1. Extract Token from "Bearer <token>" header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                // This is the method we fixed previously
                email = jwtUtil.getEmailFromToken(token);
            } catch (Exception e) {
                // Log exception if token is invalid or expired
                System.err.println("JWT error: " + e.getMessage());
            }
        }

        // 2. Validate Token and set Security Context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token)) {

                    // Create Authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Add request details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the security context, indicating the user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // If user not found or other error, just ignore and proceed as anonymous
                // This prevents 403 errors on public endpoints if the token is invalid/old
                System.err.println("JWT Filter Exception (ignoring): " + e.getMessage());
                SecurityContextHolder.clearContext(); // Ensure context is clear
            }
        } else {
            // Debug log for anonymous access
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("DEBUG: JWT Filter - Proceeding as anonymous");
            }
        }

        filterChain.doFilter(request, response);
    }
}