package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.util.JwtAuthenticationConverterWrapper;
import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import java.io.IOException;

/**
 * Filter for Keycloak tokens (RS256), called ONLY by DelegatingJwtFilter if token is not custom.
 */
public class JwtOAuth2Filter extends org.springframework.web.filter.OncePerRequestFilter {

    private final JwtAuthenticationProvider jwtAuthProvider;
    private final JwtUtil jwtUtil;

    public JwtOAuth2Filter(JwtDecoder jwtDecoder, JwtUtil jwtUtil) {
        this.jwtAuthProvider = new JwtAuthenticationProvider(jwtDecoder);
        // Use custom converter if needed
        this.jwtAuthProvider.setJwtAuthenticationConverter(new JwtAuthenticationConverterWrapper());
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // If it's actually a custom token, skip Keycloak decoding
            if (jwtUtil.isCustomToken(token)) {
                System.out.println("Skipping Keycloak decoding for custom token");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                System.out.println("Processing Keycloak token: " + token);
                var authentication = jwtAuthProvider.authenticate(new BearerTokenAuthenticationToken(token));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Keycloak token set in SecurityContext: "
                        + SecurityContextHolder.getContext().getAuthentication().getName());
            } catch (Exception e) {
                System.out.println("Keycloak token authentication failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
