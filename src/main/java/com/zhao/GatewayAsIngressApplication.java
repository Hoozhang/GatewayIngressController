package com.zhao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class GatewayAsIngressApplication {

	private static final Logger logger = LoggerFactory.getLogger(GatewayAsIngressApplication.class);

	public static void main(String[] args) {
		/*
		 * set server.ssl properties program
		 * for self-signed-cert.jks, key-store-password: "changeit"
		 * for self-signed-cert.p12, key-store-password: "changeit"
		 * for self-signed-cert.pfx, key-store-password: "changeit"
		 * for ascdevkvshareda-customer-https-dev-20200911.pfx, key-store-password: ""
		 * form https://docs.microsoft.com/en-us/azure/key-vault/certificates/how-to-export-certificate?tabs=azure-cli
		 */
		Properties properties = new Properties();
		StringBuilder keyPath = new StringBuilder();
		// detect environment based on hostname, null is Minikube, notNull is local env
		if (System.getenv("COMPUTERNAME") == null) {
			keyPath.append("/etc/tls/");
			logger.info("Minikube Env detected!");
		} else {
			keyPath.append("classpath:");
			logger.info("Local Env detected!");
		}
		properties.put("server.ssl.key-store", keyPath + "self-signed-cert.pfx");
		properties.put("server.ssl.key-store-password", "changeit");
		properties.put("server.ssl.key-store-type", "PKCS12");

		new SpringApplicationBuilder(GatewayAsIngressApplication.class).properties(properties).run(args);

	}

}
