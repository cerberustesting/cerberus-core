/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.config.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 * @author bcivel
 */
@Configuration
@EnableWebSecurity
@Profile("keycloak")
public class WebSecurityKeycloakConfiguration {

	private static final Logger LOG = LogManager.getLogger(WebSecurityKeycloakConfiguration.class);

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {

		String keycloakUrl  = System.getProperty("org.cerberus.keycloak.url");
		String realm        = System.getProperty("org.cerberus.keycloak.realm");
		String clientId     = System.getProperty("org.cerberus.keycloak.client");
		String clientSecret = System.getProperty("org.cerberus.keycloak.secret");

		String baseUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect";

		ClientRegistration registration =
				ClientRegistration.withRegistrationId("keycloak")
						.clientId(clientId)
						.clientSecret(clientSecret)
						.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
						.redirectUri("{baseUrl}/login/oauth2/code/keycloak")
						.scope("openid", "profile", "email")
						.authorizationUri(baseUrl + "/auth")
						.tokenUri(baseUrl + "/token")
						.userInfoUri(baseUrl + "/userinfo")
						.jwkSetUri(baseUrl + "/certs")
						.userNameAttributeName("preferred_username")
						.clientName("Keycloak")
						.build();

		return new InMemoryClientRegistrationRepository(registration);
	}

	@Bean
	public GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {
		String clientId = System.getProperty("org.cerberus.keycloak.client");
		return authorities -> authorities.stream()
				.flatMap(a -> {
					LOG.debug("[KEYCLOAK ORIGINAL] {} : {}", a.getClass().getSimpleName(), a.getAuthority());

					if (a instanceof OidcUserAuthority oidcAuthority) {
						Map<String, Object> attrs = oidcAuthority.getIdToken().getClaims();
						LOG.debug("[KEYCLOAK ATTRIBUTES OIDC] {}", attrs);
						return extractRoles(attrs, clientId, "");

					} else if (a instanceof OAuth2UserAuthority userAuthority) {
						Map<String, Object> attrs = userAuthority.getAttributes();
						LOG.debug("[KEYCLOAK ATTRIBUTES FALLBACK] {}", attrs);
						return extractRoles(attrs, clientId, " FALLBACK");

					} else {
						return Stream.of(a);
					}
				})
				.peek(a -> LOG.debug("[MAPPED ROLE] {}", a.getAuthority()))
				.collect(Collectors.toSet());
	}

	private Stream<SimpleGrantedAuthority> extractRoles(Map<String, Object> attrs, String clientId, String logSuffix) {
		Collection<SimpleGrantedAuthority> roles = KeycloakRoleMapper.extractRoles(attrs, clientId);
		roles.forEach(r -> LOG.debug("[KEYCLOAK ROLE{}] {}", logSuffix, r.getAuthority()));
		return roles.stream();
	}

	//Ignore static content
	@Bean
	// TODO: Replace AntPathRequestMatcher when Spring Security migration is finalized.
	@SuppressWarnings({"deprecation", "removal"})
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(
				new AntPathRequestMatcher("/js/**"),
				new AntPathRequestMatcher("/css/**"),
				new AntPathRequestMatcher("/img/**"),
				new AntPathRequestMatcher("/fonts/**"),
				new AntPathRequestMatcher("/favicon.ico"),
				new AntPathRequestMatcher("/webjars/**"),
				new AntPathRequestMatcher("/dependencies/**")
		);
	}

	@Bean
	public JwtDecoder mcpJwtDecoder() {
		String keycloakUrl = System.getProperty("org.cerberus.keycloak.url");
		String realm       = System.getProperty("org.cerberus.keycloak.realm");
		// Optional : expected audience the token must be issued for (RFC 8707).
		String audience    = System.getProperty("org.cerberus.keycloak.mcp.audience");

		String issuer    = keycloakUrl + "/realms/" + realm;
		String jwkSetUri = issuer + "/protocol/openid-connect/certs";

		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

		// Default validators (signature + expiry) + issuer binding, plus audience when configured.
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
		OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
			if (audience == null || jwt.getAudience().contains(audience)) {
				return OAuth2TokenValidatorResult.success();
			}
			return OAuth2TokenValidatorResult.failure(
					new OAuth2Error("invalid_token", "Required audience '" + audience + "' is missing", null));
		};
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));

		return decoder;
	}

	@Bean
	public JwtAuthenticationConverter mcpJwtAuthenticationConverter() {
		String clientId = System.getProperty("org.cerberus.keycloak.client");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setPrincipalClaimName("preferred_username");
		converter.setJwtGrantedAuthoritiesConverter(jwt ->
				new ArrayList<GrantedAuthority>(KeycloakRoleMapper.extractRoles(jwt.getClaims(), clientId)));
		return converter;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http,
			McpApiKeyAuthFilter mcpApiKeyAuthFilter,
			JwtDecoder mcpJwtDecoder,
			JwtAuthenticationConverter mcpJwtAuthenticationConverter) throws Exception {
		http
				.securityMatcher("/mcp")
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				// Bearer JWT issued by Keycloak, validated against the realm JWK set.
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.decoder(mcpJwtDecoder)
								.jwtAuthenticationConverter(mcpJwtAuthenticationConverter)))
				// Runs after Bearer auth so it can reuse the resolved principal, X-API-KEY otherwise.
				.addFilterBefore(mcpApiKeyAuthFilter, AuthorizationFilter.class);
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable());

		WebSecurityRules.applyRules(http);

		http.oauth2Login(oauth2 -> oauth2
				.defaultSuccessUrl("/Homepage.jsp", false)
				.failureHandler((request, response, exception) -> {
					LOG.warn("[OAUTH2 FAILURE] " + exception.getClass().getName());
					LOG.warn("[OAUTH2 FAILURE] " + exception.getMessage());
					exception.printStackTrace();
					response.sendRedirect("/cerberus_core_war/login?error");
				})
				.userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(keycloakAuthoritiesMapper()))
		);

		http.logout(logout -> logout
						.logoutUrl("/Logout.jsp")
						.logoutSuccessUrl("/")
						.permitAll()
		);

		return http.build();
	}
}