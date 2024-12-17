package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.util.JwtAuthenticationConverterWrapper;
import com.keyloack.integrationkeyloack.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtOAuth2Filter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtOAuth2Filter(JwtDecoder jwtDecoder, JwtUtil jwtUtil) {
        this.jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        this.jwtAuthenticationProvider.setJwtAuthenticationConverter(new JwtAuthenticationConverterWrapper());
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if(jwtUtil.isCustomToken(token)){
                System.out.println("Skipping Keycloak decoding for custom token");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                System.out.println("Processing Keycloak token: " + token);
                var authentication = jwtAuthenticationProvider.authenticate(new BearerTokenAuthenticationToken(token));
                System.out.println("Authenticated user: " + authentication.getName());
                System.out.println("Authorities: " + authentication.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Keycloak token set in SecurityContext: "+SecurityContextHolder.getContext().getAuthentication().getName());
            } catch (Exception e) {
                System.out.println("Keycloak token authentication failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

}
