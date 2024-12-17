package com.keyloack.integrationkeyloack.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthenticationConverterWrapper implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;

    public JwtAuthenticationConverterWrapper() {
        this.keycloakRealmRoleConverter = new KeycloakRealmRoleConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            Collection<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toSet());

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        }
        return null;
    }

}
