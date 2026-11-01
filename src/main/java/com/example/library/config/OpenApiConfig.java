package com.example.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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