package com.keyloack.integrationkeyloack.config;

import com.keyloack.integrationkeyloack.filter.DelegatingJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;  // <-- Make sure to import this
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Modern annotation for method-level security
public class SecurityConfig {

    private final DelegatingJwtFilter delegatingJwtFilter;

    public SecurityConfig(@Lazy DelegatingJwtFilter delegatingJwtFilter) {
        this.delegatingJwtFilter = delegatingJwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // The main differences from your original code:
        // 1) Disable CSRF if you’re purely stateless
        // 2) Add the DelegatingJwtFilter before UsernamePasswordAuthenticationFilter
        // 3) Set sessionCreationPolicy(STATELESS)
        // 4) Add explicit request matchers (like your friend’s code) for open endpoints vs. role-based
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Open endpoints
                        .requestMatchers("/auth/**").permitAll()
                        // Role-based endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/articles/**").hasAnyRole("USER")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Put your DelegatingJwtFilter in front of the UsernamePasswordAuthenticationFilter
                .addFilterBefore(delegatingJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Optional but nice to have: role hierarchy
    // e.g. ROLE_ADMIN > ROLE_EMPLOYEE means an ADMIN is also recognized as an EMPLOYEE
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_EMPLOYEE");
        return roleHierarchy;
    }

    // Configure the JwtDecoder for Keycloak tokens.
    @Bean
    public JwtDecoder jwtDecoder() {
        // Make sure this is correct Keycloak certs URL for your realm
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8080/realms/bankify/protocol/openid-connect/certs")
                .build();
    }
}
