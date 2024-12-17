package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Filter for handling custom HS256 token (issuer = "custom").
 * Called ONLY by DelegatingJwtFilter if isCustomToken(...) is true.
 */
public class JwtRequestFilter extends org.springframework.web.filter.OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                System.out.println("Processing custom token: " + token);

                String username = jwtUtil.extractUsername(token);
                var authorities = jwtUtil.getAuthoritiesFromToken(token);

                System.out.println("Authenticated username: " + username);
                System.out.println("Authorities: " + authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Custom token set in SecurityContext: "
                        + SecurityContextHolder.getContext().getAuthentication().getName());
            } catch (Exception e) {
                System.out.println("Custom token authentication failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
