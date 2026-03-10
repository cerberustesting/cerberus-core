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

	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.context.annotation.Profile;
	import org.springframework.security.authentication.AuthenticationManager;
	import org.springframework.security.authentication.ProviderManager;
	import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
	import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
	import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.security.web.SecurityFilterChain;
	import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
	import org.cerberus.core.config.security.WebSecurityRules;

	/**
	 *
	 * @author bcivel
	 */
	@Configuration
	@EnableWebSecurity
	@Profile("local")
	public class WebSecurityLocalConfiguration {

		@Bean
		public PasswordEncoder passwordEncoder() {
			String algorithmName = System.getProperty("cerberus.password.encoding", "SHA1");
			AuthenticationPasswordEncoder.Algorithm algorithm =
					AuthenticationPasswordEncoder.Algorithm.valueOf(algorithmName.toUpperCase());
			return new AuthenticationPasswordEncoder(algorithm);
		}

		@Bean
		public AuthenticationManager authenticationManager(
				AuthenticationUserDetailsService userDetailsService,
				PasswordEncoder passwordEncoder) throws Exception {
			DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
			provider.setPasswordEncoder(passwordEncoder);
			return new ProviderManager(provider);
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
					PathPatternRequestMatcher.withDefaults().matcher("/webjars/**")
			);
		}


		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

			http.csrf(csrf -> csrf.disable());

			WebSecurityRules.applyRules(http);

					http.formLogin(form -> form
							.loginPage("/Login.jsp")
							.loginProcessingUrl("/j_security_check")
							.defaultSuccessUrl("/Homepage.jsp", false)
							.failureUrl("/Login.jsp?error=1")
							.usernameParameter("j_username")
							.passwordParameter("j_password")
							.permitAll()
					);
					http.logout(logout -> logout
							.logoutUrl("/Logout.jsp")
							.permitAll()
					);

			return http.build();
		}
	}