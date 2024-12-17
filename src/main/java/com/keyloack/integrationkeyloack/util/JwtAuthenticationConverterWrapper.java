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

/**
 * A Keycloak-specific converter that extracts realm-level roles from JWT claim "realm_access".
 */
public class JwtAuthenticationConverterWrapper implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Typically the Keycloak realm roles are stored under "realm_access.roles"
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");

            // Convert each role to a SimpleGrantedAuthority with the prefix "ROLE_"
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toSet());

            // principal (3rd param) is typically the subject
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        }

        // If no realm_access, fallback to empty authorities but still produce an authentication token
        return new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject());
    }
}
