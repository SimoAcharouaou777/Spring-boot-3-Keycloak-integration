package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.repository.UserRepository;
import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The only filter that Spring sees in the chain.
 * Delegates token parsing to either JwtRequestFilter (custom token)
 * or JwtOAuth2Filter (Keycloak token).
 */
@Component
public class DelegatingJwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtOAuth2Filter jwtOAuth2Filter;

    // We inject the dependencies needed for sub-filters: JwtUtil + JwtDecoder.
    public DelegatingJwtFilter(JwtUtil jwtUtil, JwtDecoder jwtDecoder, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;

        // Manually create sub-filter instances (no @Component):
        this.jwtRequestFilter = new JwtRequestFilter(jwtUtil);
        this.jwtOAuth2Filter = new JwtOAuth2Filter(jwtDecoder, jwtUtil , userRepository);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // If it's a custom token, call JwtRequestFilter logic
            if (jwtUtil.isCustomToken(token)) {
                System.out.println("Processing custom token");
                jwtRequestFilter.doFilter(request, response, filterChain);
                return; // Short-circuit so Keycloak filter won't run
            } else {
                // Otherwise treat as Keycloak token
                System.out.println("Processing Keycloak token");
                jwtOAuth2Filter.doFilter(request, response, filterChain);
                return; // Stop after Keycloak filter
            }
        }

        // No Bearer token? Just continue
        filterChain.doFilter(request, response);
    }
}
