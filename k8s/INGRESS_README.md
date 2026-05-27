# Documentation - Configuration Ingress AWS ALB

## Prérequis
- AWS Load Balancer Controller installé sur le cluster EKS
- Certificat SSL/TLS dans AWS ACM
- IAM permissions pour créer/gérer ALB

## Installation du AWS Load Balancer Controller
```bash
helm repo add eks https://aws.github.io/eks-charts
helm repo update
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<YOUR_CLUSTER_NAME>
```

## Points à adapter

### 1. Certificat SSL (ARN)
Remplacez `arn:aws:acm:eu-west-1:585008070520:certificate/your-certificate-id` par votre ARN ACM réel.

Pour obtenir votre ARN :
```bash
aws acm list-certificates --region eu-west-1
```

### 2. Domaine
Remplacez `api.example.com` par votre domaine personnalisé.

### 3. VPC et Subnets
Optionnellement, spécifiez les subnets :
```yaml
annotations:
  alb.ingress.kubernetes.io/subnets: subnet-1a2b3c4d,subnet-5e6f7g8h
```

## Déploiement
```bash
kubectl apply -f k8s/deplloyment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

## Vérification
```bash
# Voir l'ingress
kubectl get ingress

# Détails de l'ingress
kubectl describe ingress my-api-ingress

# URL du ALB (récupérer après quelques minutes)
kubectl get ingress -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}'
```

## Configuration Health Check
- Chemin : `/api/clients`
- Protocole : HTTP
- Intervalle : 15 secondes
- Timeout : 5 secondes
- Seuil sain : 2 pings
- Seuil mauvais : 2 pings

## Tags AWS
Les ressources créées seront taguées avec :
- Environment: dev
- Application: gestion-commande
