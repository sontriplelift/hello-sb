package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT")
public class OpenApiConfig {

	@Bean
	public OpenAPI libraryOpenAPI(LibraryProperties properties) {
		LibraryProperties.Api api = properties.api();

		return new OpenAPI()
				.info(new Info()
						.title(api.title())
						.version(api.version())
						.description(api.description()));
	}
}