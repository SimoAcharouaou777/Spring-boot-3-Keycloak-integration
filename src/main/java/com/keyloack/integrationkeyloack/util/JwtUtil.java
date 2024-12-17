package com.keyloack.integrationkeyloack.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "cRfZWEWcam0rZfeiATUqsM7kJzCsKS0x7f4Yns5xcLQ=";
    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30; // 30 days

    public String generateToken(String username, Collection<SimpleGrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuer("custom")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("custom")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean isCustomToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return "custom".equals(claims.getIssuer());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = extractClaims(token).getSubject();
            System.out.println("Extracted username: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("Failed to extract username: " + e.getMessage());
            throw e;
        }
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            List<String> roles = claims.get("roles", List.class);
            System.out.println("Extracted roles: " + roles);
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Failed to extract authorities: " + e.getMessage());
            return List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        }
    }

    private Claims extractClaims(String token) {
        try{
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate custom token : " + e.getMessage());
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

}
