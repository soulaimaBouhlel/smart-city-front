package com.smartcity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

// NOTE: You'll need to create the 'security' package.

@Component
public class JwtTokenProvider {

    // IMPORTANT: Replace this with a secure, long secret key (e.g., from application.properties)
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationInMs;

    // Generate JWT token
    public String generateToken(Authentication authentication) {
        String userEmail = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Helper method to get the signing key
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Get email from JWT token
    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Validate JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            // Log this: Invalid JWT token
        } catch (ExpiredJwtException ex) {
            // Log this: Expired JWT token
        } catch (UnsupportedJwtException ex) {
            // Log this: Unsupported JWT token
        } catch (IllegalArgumentException ex) {
            // Log this: JWT claims string is empty
        }
        return false;
    }
}