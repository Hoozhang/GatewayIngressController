package com.zhao.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesConfig.class);

	@Bean
	// KubernetesClient: 用于访问K8S中的所有资源
	// Workaround: https://github.com/fabric8io/kubernetes-client
	public KubernetesClient kubernetesClient() {
		// 创建一个config对象来配置KubernetesClient
		Config config = new ConfigBuilder().build();
		// 创建一个默认的KubernetesClient
		logger.info("Bean named KubernetesClient created!");
		return new DefaultKubernetesClient(config);
	}

}
