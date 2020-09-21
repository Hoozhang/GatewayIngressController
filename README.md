# gateway as ingress
Implementation of Spring Cloud Gateway as K8S Ingress Controller based on similar example by [nlighten](https://github.com/nlighten/spring-cloud-gateway-ingress).

### How to run it

package your application.

```
$ mvn clean package -DskipTests=true
```

build and push a docker image. \
Notice: replace 'zhaodockerhub' with your DockerHub username

```
$ docker build -t zhaodockerhub/gateway-as-ingress .
$ docker tag zhaodockerhub/gateway-as-ingress zhaodockerhub/gateway-as-ingress:v3.0
$ docker push zhaodockerhub/gateway-as-ingress:v3.0
```

basic setting for Kubernetes, e.g., (Namespace and ServiceAccount)
```
$ kubectl apply -f k8s/namespace.yml
```

add TLS setting into Kubernetes Secret.
```
$ kubectl create secret generic asc-test-net --from-file=src/main/resources/asc-test-net.pfx
```

deploy some routes into Kubernetes.

```
$ kubectl apply -f k8s/ingress.yml
```

deploy the 'gateway-as-ingress' image into Kubernetes.

```
$ kubectl apply -f k8s/gateway-ingress-controller.yml
```

deploy a service example into Kubernetes.

```
$ kubectl apply -f k8s/simple-example.yml
```

### sample ingress definition

```yaml
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: simple-fanout
  namespace: gateway-ingress-v1
  annotations:
    kubernetes.io/ingress.class: spring.cloud.gateway
spec:
  rules:
  - host: simple.fanout.com
    http:
      paths:
      - path: /anything/foo
        backend:
          serviceName: simple-fanout
          servicePort: 80
      - path: /anything/bar
        backend:
          serviceName: simple-fanout
          servicePort: 80
```

see [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/) for more examples in detail.

### Notes/limitations
* Just a test. Do not run this in production.
* Both gateway and backends must run in same namespace.
* Relies on Spring Cloud Kubernetes for backend discovery and ribbon load balancing.
* Annotations must contain the item **"kubernetes.io/ingress.class: spring.cloud.gateway"**.
