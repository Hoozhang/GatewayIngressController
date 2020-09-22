package com.zhao;

import com.zhao.config.IngressTlsLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.net.ssl.SSLContext;
import java.util.Properties;

@SpringBootApplication
public class GatewayAsIngressApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(GatewayAsIngressApplication.class).properties(PropertiesConfig()).run(args);
	}

	public static Properties PropertiesConfig() {
		// detect environment based on hostname, null for Minikube, notNull for LocalEnv
		String keyPath = System.getenv("COMPUTERNAME") == null? "/etc/tls/" : "classpath:";
		// load ssl cert filename from Kubernetes, to init SSL for gateway
		String keyFile = new IngressTlsLoader().getOneIngressTls().getSecretName() + ".pfx";

		// set server.ssl properties
		Properties properties = new Properties();
		properties.setProperty("server.ssl.key-store", keyPath + keyFile);
		properties.setProperty("server.ssl.key-store-password", "");
		properties.setProperty("server.ssl.key-store-type", "PKCS12");
		return properties;
	}
}
