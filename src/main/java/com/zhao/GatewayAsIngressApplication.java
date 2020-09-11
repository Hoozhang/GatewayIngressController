package com.zhao;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class GatewayAsIngressApplication {

	public static void main(String[] args) {
		//SpringApplication.run(GatewayAsIngressApplication.class, args);

		// set server.ssl properties program
		Properties properties = new Properties();

		/*
		 * for self-signed-cert.jks, key-store-password: "changeit"
		 * for self-signed-cert.p12, key-store-password: "changeit"
		 * for self-signed-cert.pfx, key-store-password: "changeit"
		 * for ascdevkvshareda-customer-https-dev-20200911.pfx, key-store-password: ""
		 * refer form https://docs.microsoft.com/en-us/azure/key-vault/certificates/how-to-export-certificate?tabs=azure-cli
		 */
		properties.put("server.ssl.key-store", "classpath:self-signed-cert.pfx");
		properties.put("server.ssl.key-store-password", "changeit");
		properties.put("server.ssl.key-store-type", "PKCS12");

		new SpringApplicationBuilder(GatewayAsIngressApplication.class).properties(properties).run(args);

	}

}
