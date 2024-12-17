package com.keyloack.integrationkeyloack.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthenticationConverterWrapper implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<SimpleGrantedAuthority> authorities = List.of();
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toSet());
        }

        // Use "preferred_username" as principal
        String keycloakUsername = jwt.getClaimAsString("preferred_username");
        if (keycloakUsername == null || keycloakUsername.isBlank()) {
            // fallback to the subject if needed
            keycloakUsername = jwt.getSubject();
        }

        return new JwtAuthenticationToken(jwt, authorities, keycloakUsername);
    }
}
