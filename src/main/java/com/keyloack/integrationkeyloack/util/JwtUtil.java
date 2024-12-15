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
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "cRfZWEWcam0rZfeiATUqsM7kJzCsKS0x7f4Yns5xcLQ=";
    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 10;
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30;

    public String generateToken(String username, Collection<SimpleGrantedAuthority> authorities){
        List<String> roles = authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuer("custom")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateRefreshToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token){
        Claims claims = extractAllClaims(token);
        Object roles =  claims.get("roles");

        if(roles instanceof List){
            return ((List<?>)roles).stream()
                    .map(role -> new SimpleGrantedAuthority(role.toString()))
                    .toList();
        }
        throw new IllegalArgumentException("Invalid roles format in token");

    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isCustomToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getIssuer() != null && claims.getIssuer().equals("custom");
        } catch (Exception e) {
            return false;
        }
    }
}
