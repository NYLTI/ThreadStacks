package com.threadstacks.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthConverter implements Converter<Jwt, Mono<JwtAuthenticationToken>> {

    @Override
    public Mono<JwtAuthenticationToken> convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);
        System.err.println(authorities);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    private Set<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        return extractRealmRoles(jwt);
    }


    private Set<SimpleGrantedAuthority> extractRealmRoles(Jwt jwt) {
	    return Optional.ofNullable(jwt.getClaim("realm_access"))
	            .filter(Map.class::isInstance)
	            .map(realmAccess -> ((Map<?, ?>) realmAccess).get("roles"))
	            .filter(List.class::isInstance)
	            .map(roles -> ((List<?>) roles).stream()
	                    .filter(String.class::isInstance)
	                    .map(String.class::cast)
	                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
	                    .collect(Collectors.toSet()))
	            .orElse(Collections.emptySet());
	}

}
