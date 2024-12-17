package com.keyloack.integrationkeyloack.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Optional debugging filter to log roles for every request.
 * If you don’t need this, you can remove it or keep it in the chain.
 */
@Component
public class RoleLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            System.out.println("Authenticated user: " + authentication.getName());
            authentication.getAuthorities().forEach(auth ->
                    System.out.println("Authority: " + auth.getAuthority())
            );
        } else {
            System.out.println("No authenticated user for this request");
        }

        filterChain.doFilter(request, response);
    }
}
