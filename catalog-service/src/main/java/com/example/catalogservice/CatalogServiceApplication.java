package com.example.catalogservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CatalogServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		Map<String, Object> envMap = new HashMap<>();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
			envMap.put(entry.getKey(), entry.getValue());
		});

		System.out.println("DB_USERNAME: " + System.getProperty("DB_USERNAME"));
		System.out.println("DB_PASSWORD: " + System.getProperty("DB_PASSWORD"));
		System.out.println("DB_HOST: " + System.getProperty("DB_HOST"));
		System.out.println("DB_PORT: " + System.getProperty("DB_PORT"));
		System.out.println("DB_NAME: " + System.getProperty("DB_NAME"));
		System.out.println("SWAGGER_ENABLED: " + System.getProperty("SWAGGER_ENABLED"));

		SpringApplication app = new SpringApplication(CatalogServiceApplication.class);
		ConfigurableApplicationContext context = app.run(args);

		ConfigurableEnvironment environment = context.getEnvironment();
		environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
	}

}
