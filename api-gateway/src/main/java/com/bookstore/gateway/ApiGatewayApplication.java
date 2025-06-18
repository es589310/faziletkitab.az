package com.bookstore.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class ApiGatewayApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		Properties properties = new Properties();

		dotenv.entries().forEach(entry -> {
			System.out.println("Env: " + entry.getKey() + "=" + entry.getValue());
			System.setProperty(entry.getKey(), entry.getValue());
			properties.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication app = new SpringApplication(ApiGatewayApplication.class);
		app.setDefaultProperties(properties);
		app.run(args);
	}
}