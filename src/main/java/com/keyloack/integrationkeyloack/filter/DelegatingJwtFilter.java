package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Delegates filtering to either the custom JWT filter (JwtRequestFilter)
 * or the Keycloak filter (JwtOAuth2Filter), depending on token issuer.
 */
@Component
public class DelegatingJwtFilter extends OncePerRequestFilter {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtOAuth2Filter jwtOAuth2Filter;
    private final JwtUtil jwtUtil;

    public DelegatingJwtFilter(JwtRequestFilter jwtRequestFilter,
                               JwtOAuth2Filter jwtOAuth2Filter,
                               JwtUtil jwtUtil) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtOAuth2Filter = jwtOAuth2Filter;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                // Distinguish if it’s a custom JWT or a Keycloak JWT
                if (jwtUtil.isCustomToken(token)) {
                    System.out.println("Processing custom token.");
                    // Delegate to custom JWT filter
                    jwtRequestFilter.doFilter(request, response, filterChain);
                } else {
                    System.out.println("Processing Keycloak token.");
                    // Delegate to Keycloak JWT filter
                    jwtOAuth2Filter.doFilter(request, response, filterChain);
                }
                return;  // Ensure we do not call filterChain.doFilter twice
            } catch (Exception e) {
                System.out.println("Custom token check failed : " + e.getMessage());
                // If we fail here, just let the chain continue—no auth set
            }
        }

        // If no Bearer token in the header, or something went wrong, proceed normally
        filterChain.doFilter(request, response);
    }
}
