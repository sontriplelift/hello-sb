package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			CustomAuthenticationEntryPoint authenticationEntryPoint,
			CustomAccessDeniedHandler accessDeniedHandler,
			JwtAuthenticationConverter jwtAuthenticationConverter)
			throws Exception {

		http
				.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// Swagger: cho phép mọi người truy cập
						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**")
						.permitAll()

						// Error endpoint: cho phép mọi người truy cập
						.requestMatchers("/error").permitAll()

						// endpoint login phải được phép truy cập mà không cần token
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

						// GET API: cho phép mọi người truy cập
						.requestMatchers(HttpMethod.GET, "/api/**").permitAll()

						// POST, PUT, DELETE API: chỉ ADMIN
						.requestMatchers(
								HttpMethod.POST, "/api/**")
						.hasRole("ADMIN")
						.requestMatchers(
								HttpMethod.PUT, "/api/**")
						.hasRole("ADMIN")
						.requestMatchers(
								HttpMethod.DELETE, "/api/**")
						.hasRole("ADMIN")

						// Các request còn lại phải đăng nhập
						.anyRequest().authenticated())

				// Đăng ký xử lý lỗi xác thực và phân quyền
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))

				// Sử dụng HTTP Basic Authentication
				// .httpBasic(basic -> basic
				// 	.authenticationEntryPoint(authenticationEntryPoint)
				// );

				// Sử dụng JWT
				.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(jwt -> jwt
						.jwtAuthenticationConverter(jwtAuthenticationConverter)
					)
					.authenticationEntryPoint(authenticationEntryPoint)
				);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(
			LibraryProperties properties,
			PasswordEncoder passwordEncoder) {
		LibraryProperties.Security security = properties.security();

		UserDetails librarian = User.builder()
				.username("librarian")
				.password(passwordEncoder.encode(security.adminPassword()))
				.roles("ADMIN")
				.build();

		UserDetails reader = User.builder()
				.username("reader")
				.password(passwordEncoder.encode(security.readerPassword()))
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(librarian, reader);
	}
}