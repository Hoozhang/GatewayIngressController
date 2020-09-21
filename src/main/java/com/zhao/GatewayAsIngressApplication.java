package com.zhao;

import com.zhao.config.IngressTlsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Properties;

@SpringBootApplication
public class GatewayAsIngressApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(GatewayAsIngressApplication.class).properties(PropertiesConfig()).run(args);
	}

	public static Properties PropertiesConfig() {
		Properties properties = new Properties();
		// detect environment based on hostname, null for Minikube, notNull for LocalEnv
		String keyPath = System.getenv("COMPUTERNAME") == null? "/etc/tls/" : "classpath:";
		// load ssl cert filename from Kubernetes, to init SSL for gateway
		String keyFile = new IngressTlsLoader().getOneIngressTls().getSecretName() + ".pfx";
		properties.put("server.ssl.key-store", keyPath + keyFile);
		properties.put("server.ssl.key-store-password", "");
		properties.put("server.ssl.key-store-type", "PKCS12");
		return properties;
	}
}
