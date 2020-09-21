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

public class IngressTlsLoader {

    private final KubernetesClient kubernetesClient;

    private final List<IngressTLS> ingressTlsList;

    public IngressTlsLoader() {
        kubernetesClient = new DefaultKubernetesClient(new ConfigBuilder().build());
        ingressTlsList = new ArrayList<>();
        loadIngressTls();
    }

    public void loadIngressTls() {
        IngressList ingressList = kubernetesClient.extensions().ingresses().list();
        for (Ingress ingress : ingressList.getItems()) {
            ingressTlsList.addAll(ingress.getSpec().getTls());
        }
    }

    public IngressTLS getOneIngressTls() {
        return ingressTlsList.get(0);
    }

    public List<IngressTLS> getIngressTlsList() {
        return ingressTlsList;
    }
}
