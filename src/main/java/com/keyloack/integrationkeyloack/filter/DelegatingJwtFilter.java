package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DelegatingJwtFilter extends OncePerRequestFilter {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtOAuth2Filter jwtOAuth2Filter;
    private final JwtUtil jwtUtil;

    public DelegatingJwtFilter(JwtRequestFilter jwtRequestFilter, JwtOAuth2Filter jwtOAuth2Filter, JwtUtil jwtUtil) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtOAuth2Filter = jwtOAuth2Filter;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if (jwtUtil.isCustomToken(token)) {
                System.out.println("Processing custom token.");
                jwtRequestFilter.doFilter(request, response, filterChain); // Custom Token Handling
                return;
            } else{
                System.out.println("Processing keycloak token.");
                jwtOAuth2Filter.doFilter(request, response, filterChain);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
