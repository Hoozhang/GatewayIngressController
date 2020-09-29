package com.zhao.config;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IngressTlsLoader {

    private final KubernetesClient kubernetesClient;

    private final List<IngressTLS> ingressTlsList;

    private static final String INGRESS_CLASS = "spring-cloud-gateway";

    public IngressTlsLoader() {
        kubernetesClient = new DefaultKubernetesClient(new ConfigBuilder().build());
        ingressTlsList = new ArrayList<>();
        loadIngressTls();
    }

    public void loadIngressTls() {
        IngressList ingressList = kubernetesClient.extensions().ingresses().list();
        for (Ingress ingress : ingressList.getItems()) {
            Map<String, String> annotations = ingress.getMetadata().getAnnotations();
            if (annotations == null || !annotations.containsKey("ingress.controller.type")
                    || !annotations.get("ingress.controller.type").equals(INGRESS_CLASS)) {
                continue;
            }
            ingressTlsList.addAll(ingress.getSpec().getTls());
        }
    }

    public IngressTLS getOneIngressTls() {
        Assert.isTrue(ingressTlsList != null && ingressTlsList.size() > 0, "Ingress TLS Not Found!");
        return ingressTlsList.get(0);
    }

    public List<IngressTLS> getIngressTlsList() {
        return ingressTlsList;
    }
}
