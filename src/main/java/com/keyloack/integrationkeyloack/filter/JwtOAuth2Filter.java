package com.keyloack.integrationkeyloack.filter;

import com.keyloack.integrationkeyloack.entity.User;
import com.keyloack.integrationkeyloack.repository.UserRepository;
import com.keyloack.integrationkeyloack.util.JwtAuthenticationConverterWrapper;
import com.keyloack.integrationkeyloack.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

public class JwtOAuth2Filter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider jwtAuthProvider;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtOAuth2Filter(JwtDecoder jwtDecoder, JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtAuthProvider = new JwtAuthenticationProvider(jwtDecoder);
        this.jwtAuthProvider.setJwtAuthenticationConverter(new JwtAuthenticationConverterWrapper());
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // If custom HS256 token, skip Keycloak decoding
            if (jwtUtil.isCustomToken(token)) {
                System.out.println("Skipping Keycloak decoding for custom token");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                System.out.println("Processing Keycloak token: " + token);

                // This line calls the Keycloak JwtAuthenticationProvider
                var authentication = jwtAuthProvider.authenticate(new BearerTokenAuthenticationToken(token));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // ====== BRIDGING CODE: ensure local DB user exists ======
                if (authentication != null && authentication.isAuthenticated()) {
                    Jwt principalJwt = (Jwt) authentication.getPrincipal();

                    // Usually "preferred_username" or fallback to subject
                    String preferredUsername = principalJwt.getClaimAsString("preferred_username");
                    if (preferredUsername == null || preferredUsername.isEmpty()) {
                        preferredUsername = principalJwt.getSubject();
                    }
                    String keycloakSub = principalJwt.getSubject();

                    System.out.println("Keycloak principal username: " + preferredUsername +
                            ", sub: " + keycloakSub);

                    // Check if a local user row matches keycloakId= sub
                    var optionalUser = userRepository.findByKeycloakId(keycloakSub);
                    if (optionalUser.isEmpty()) {
                        // Create a new local user
                        User newUser = new User();
                        newUser.setKeycloakId(keycloakSub);
                        newUser.setUsername(preferredUsername);  // keycloak username

                        // Dummy password to satisfy not-null constraint
                        newUser.setPassword("N/A");

                        // Possibly set default ROLE_USER. If you want roles from Keycloak,
                        // you can skip or handle separately
                        // newUser.setRoles(new HashSet<>());

                        userRepository.save(newUser);
                        System.out.println("Created new local user for Keycloak principal: " + preferredUsername);
                    }
                }
                // ====== END BRIDGING CODE ======

                System.out.println("Keycloak token set in SecurityContext: " + authentication.getName());
            } catch (Exception e) {
                System.out.println("Keycloak token authentication failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
