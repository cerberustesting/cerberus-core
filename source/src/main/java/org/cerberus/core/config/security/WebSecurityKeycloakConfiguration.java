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
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.stream.Collectors;


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
		Stream<SimpleGrantedAuthority> realmRoles = Stream.empty();
		Stream<SimpleGrantedAuthority> clientRoles = Stream.empty();

		// --- Realm roles ---
		if (attrs.get("realm_access") instanceof Map<?, ?> realmAccess
				&& realmAccess.get("roles") instanceof Collection<?> roles) {
			roles.forEach(r -> LOG.debug("[KEYCLOAK REALM ROLE{}] {}", logSuffix, r));
			realmRoles = roles.stream()
					.filter(String.class::isInstance)
					.map(r -> new SimpleGrantedAuthority("ROLE_" + r));
		}

		// --- Client roles ---
		if (attrs.get("resource_access") instanceof Map<?, ?> resourceAccess
				&& resourceAccess.get(clientId) instanceof Map<?, ?> client
				&& client.get("roles") instanceof Collection<?> roles) {
			roles.forEach(r -> LOG.debug("[KEYCLOAK CLIENT ROLE{}] {}", logSuffix, r));
			clientRoles = roles.stream()
					.filter(String.class::isInstance)
					.map(r -> new SimpleGrantedAuthority("ROLE_" + r));
		}

		return Stream.concat(realmRoles, clientRoles);
	}

	//Ignore static content
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(
				PathPatternRequestMatcher.withDefaults().matcher("/js/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/css/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/img/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/fonts/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/favicon.ico"),
				PathPatternRequestMatcher.withDefaults().matcher("/webjars/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/dependencies/**")
		);
	}

	@Bean
	@Order(1)
	public SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/mcp")
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
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