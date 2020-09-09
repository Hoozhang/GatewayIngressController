package com.zhao;

import org.checkerframework.checker.units.qual.A;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class GatewayAsIngressApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayAsIngressApplication.class, args);

		/*
		// set server.ssl properties program
		Properties properties = new Properties();
		properties.put("server.ssl.key-alias", "1");
		properties.put("server.ssl.key-password", "changeit");
		properties.put("server.ssl.key-store", "classpath:self-signed-cert.p12");
		properties.put("server.ssl.key-store-password", "changeit");
		properties.put("server.ssl.key-store-type", "PKCS12");
		new SpringApplicationBuilder(GatewayAsIngressApplication.class).properties(properties).run(args);
		*/
	}

}
