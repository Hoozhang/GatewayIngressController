package com.zhao.ingress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zhao.utils.MapUtil;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.extensions.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
// RouteDefinitionLocator: 读取路由的配置信息(RouteDefinition)
// Watcher: 监听Kubernetes的内部事件
public class IngressRouteDefinitionLocator implements RouteDefinitionLocator, Watcher<Ingress> {

    private static final Logger logger = LoggerFactory.getLogger(IngressRouteDefinitionLocator.class);

    private static final String INGRESS_CLASS = "spring.cloud.gateway";

    private static final ConcurrentMap<String, RouteDefinition> routeDefinitions = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher eventPublisher;

    private final KubernetesClient kubernetesClient;

    private final ObjectMapper objectMapper;

    public IngressRouteDefinitionLocator(ApplicationEventPublisher eventPublisher,
                                         KubernetesClient kubernetesClient) {
        this.eventPublisher = eventPublisher;
        this.kubernetesClient = kubernetesClient;
        this.objectMapper = new Jackson2ObjectMapperBuilder().factory(new YAMLFactory()).build();
        logger.info("IngressRouteDefinitionLocator init successfully!");
    }

    @Override
    // for RouteDefinitionLocator Interface
    public Flux<RouteDefinition> getRouteDefinitions() {
        // 从迭代对象routeDefinitions中读取路由配置信息
        return Flux.fromIterable(routeDefinitions.values());
    }

    @PostConstruct
    // PostConstruct注解作用: 在依赖注入之后执行该方法, 注册监听Ingress资源的Listener
    public void watch() {
        this.kubernetesClient.extensions().ingresses().inAnyNamespace().watch(this);
    }

    @Override
    // for Watcher Interface, watch events happened on Ingress
    public void eventReceived(Action action, Ingress ingress) {
        try {
            ObjectMeta metadata = ingress.getMetadata();
            IngressSpec spec = ingress.getSpec();
            String idPrefix = metadata.getNamespace() + "/" + metadata.getName();

            if (action == Action.ADDED) {
                Map<String, String> annotations = metadata.getAnnotations();
                // 注解中应包含 "kubernetes.io/ingress.class: spring.cloud.gateway"
                if (annotations == null || !annotations.containsKey("kubernetes.io/ingress.class")
                        || !annotations.get("kubernetes.io/ingress.class").equals(INGRESS_CLASS)) {
                    logger.warn("No 'kubernetes.io/ingress.class' found in Annotations. " +
                            "Ignoring this ingress definition.");
                    return;
                }
                IngressBackend defaultBackend = spec.getBackend();
                List<IngressRule> rules = spec.getRules();
                // ingress must have a default backend or rules
                if (defaultBackend == null && rules == null) {
                    logger.warn("No {}/routes found on ingress definition {}/{}. " +
                                    "Ignoring this ingress definition.",
                            INGRESS_CLASS, metadata.getNamespace(), metadata.getName());
                    return;
                }
                if (rules != null) {
                    for (IngressRule rule : rules) {
                        String host = rule.getHost();
                        for (HTTPIngressPath pathBackend : rule.getHttp().getPaths()) {
                            String path = pathBackend.getPath();
                            IngressBackend backend = pathBackend.getBackend();
                            URI uri = UriComponentsBuilder.newInstance()
                                    .scheme("lb")
                                    .host(backend.getServiceName())
                                    .build().toUri();
                            // construct a RouteDefinition with text
                            StringBuilder text = new StringBuilder();
                            text.append(idPrefix).append(path).append("=").append(uri);
                            if (host != null) {
                                text.append(",Host=").append(host);
                            }
                            if (path != null) {
                                text.append(",Path=").append(path);
                            }
                            RouteDefinition routeDefinition = new RouteDefinition(text.toString());
                            routeDefinitions.put(routeDefinition.getId(), routeDefinition);
                            String yaml = this.objectMapper.writeValueAsString(routeDefinition);
                            logger.info("Add ingress rule: {}\t{}", idPrefix + path, yaml);
                        }
                    }
                }
                if (defaultBackend != null) {
                    URI uri = UriComponentsBuilder.newInstance()
                            .scheme("lb")
                            .host(defaultBackend.getServiceName())
                            .build().toUri();
                    String text = idPrefix + "=" + uri;
                    RouteDefinition routeDefinition = new RouteDefinition(text);
                    routeDefinitions.put(routeDefinition.getId(), routeDefinition);
                    String yaml = this.objectMapper.writeValueAsString(routeDefinition);
                    logger.info("Add ingress rule: {}\t{}", idPrefix, yaml);
                }
                this.eventPublisher.publishEvent(new RefreshRoutesEvent(this));
            } else if (action == Action.MODIFIED) {
                List<String> deleteKeys = routeDefinitions.keySet().stream().filter((String key) -> key.startsWith(idPrefix)).collect(Collectors.toList());
                MapUtil.removeEntries(routeDefinitions, deleteKeys);

                Map<String, String> annotations = metadata.getAnnotations();
                // 注解中应包含 "kubernetes.io/ingress.class: spring.cloud.gateway"
                if (annotations == null || !annotations.containsKey("kubernetes.io/ingress.class")
                        || !annotations.get("kubernetes.io/ingress.class").equals(INGRESS_CLASS)) {
                    logger.warn("No 'kubernetes.io/ingress.class' found in Annotations. " +
                            "Ignoring this ingress definition.");
                    return;
                }
                IngressBackend defaultBackend = spec.getBackend();
                List<IngressRule> rules = spec.getRules();
                // ingress must have a default backend or rules
                if (defaultBackend == null && rules == null) {
                    logger.warn("No {}/routes found on ingress definition {}/{}. " +
                                    "Ignoring this ingress definition.",
                            INGRESS_CLASS, metadata.getNamespace(), metadata.getName());
                    return;
                }
                if (rules != null) {
                    for (IngressRule rule : rules) {
                        String host = rule.getHost();
                        for (HTTPIngressPath pathBackend : rule.getHttp().getPaths()) {
                            String path = pathBackend.getPath();
                            IngressBackend backend = pathBackend.getBackend();
                            URI uri = UriComponentsBuilder.newInstance()
                                    .scheme("lb")
                                    .host(backend.getServiceName())
                                    .build().toUri();
                            // construct a RouteDefinition with text
                            StringBuilder text = new StringBuilder();
                            text.append(idPrefix).append(path).append("=").append(uri);
                            if (host != null) {
                                text.append(",Host=").append(host);
                            }
                            if (path != null) {
                                text.append(",Path=").append(path);
                            }
                            RouteDefinition routeDefinition = new RouteDefinition(text.toString());
                            routeDefinitions.put(routeDefinition.getId(), routeDefinition);
                            String yaml = this.objectMapper.writeValueAsString(routeDefinition);
                            logger.info("Add ingress rule: {}\t{}", idPrefix + path, yaml);
                        }
                    }
                }
                if (defaultBackend != null) {
                    URI uri = UriComponentsBuilder.newInstance()
                            .scheme("lb")
                            .host(defaultBackend.getServiceName())
                            .build().toUri();
                    String text = idPrefix + "=" + uri;
                    RouteDefinition routeDefinition = new RouteDefinition(text);
                    routeDefinitions.put(routeDefinition.getId(), routeDefinition);
                    String yaml = this.objectMapper.writeValueAsString(routeDefinition);
                    logger.info("Add ingress rule: {}\t{}", idPrefix, yaml);
                }
                this.eventPublisher.publishEvent(new RefreshRoutesEvent(this));

            } else if (action == Action.DELETED) {
                List<String> deleteKeys = routeDefinitions.keySet().stream().filter((String key) -> key.startsWith(idPrefix)).collect(Collectors.toList());
                MapUtil.removeEntries(routeDefinitions, deleteKeys);
                logger.info("Delete ingress {}", idPrefix);
                this.eventPublisher.publishEvent(new RefreshRoutesEvent(this));
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    // for Watcher Interface
    public void onClose(KubernetesClientException e) {
        logger.debug("Kubernetes client closed!");
    }
}
