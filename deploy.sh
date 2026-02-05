#!/bin/bash

# Accountia Kubernetes Deployment Script
# Usage: ./deploy.sh [build|deploy|undeploy|status|logs]

set -e

# Configuration
DOCKER_REGISTRY="${DOCKER_REGISTRY:-localhost:5000}"
NAMESPACE="accountia"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="${SCRIPT_DIR}/k8s"
PROJECT_DIR="${SCRIPT_DIR}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Build all Docker images
build_images() {
    log_info "Building Docker images..."
    
    services=("api-gateway" "auth-ms" "business-ms" "invoice-ms" "expense-ms" "client-ms")
    
    for service in "${services[@]}"; do
        if [ -d "${PROJECT_DIR}/${service}" ]; then
            log_info "Building ${service}..."
            docker build -t "${DOCKER_REGISTRY}/accountia/${service}:latest" "${PROJECT_DIR}/${service}"
        else
            log_warn "Directory ${service} not found, skipping..."
        fi
    done
    
    # Build frontend
    if [ -d "${PROJECT_DIR}/frontend" ]; then
        log_info "Building frontend..."
        docker build -t "${DOCKER_REGISTRY}/accountia/frontend:latest" "${PROJECT_DIR}/frontend"
    fi
    
    # Build reporting-ms (Python)
    if [ -d "${PROJECT_DIR}/reporting-ms" ]; then
        log_info "Building reporting-ms..."
        docker build -t "${DOCKER_REGISTRY}/accountia/reporting-ms:latest" "${PROJECT_DIR}/reporting-ms"
    fi
    
    log_info "All images built successfully!"
}

# Push images to registry
push_images() {
    log_info "Pushing Docker images to ${DOCKER_REGISTRY}..."
    
    services=("api-gateway" "auth-ms" "business-ms" "invoice-ms" "expense-ms" "client-ms" "frontend" "reporting-ms")
    
    for service in "${services[@]}"; do
        log_info "Pushing ${service}..."
        docker push "${DOCKER_REGISTRY}/accountia/${service}:latest" || log_warn "Failed to push ${service}"
    done
    
    log_info "All images pushed successfully!"
}

# Deploy to Kubernetes
deploy() {
    log_info "Deploying Accountia to Kubernetes..."
    
    # Create namespace if not exists
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
    
    # Use kustomize with image overrides for the registry
    log_info "Applying Kustomize with registry: ${DOCKER_REGISTRY}..."
    
    pushd "${K8S_DIR}" > /dev/null
    
    # Set image names with the configured registry
    kustomize edit set image \
        accountia/api-gateway=${DOCKER_REGISTRY}/accountia/api-gateway:latest \
        accountia/auth-ms=${DOCKER_REGISTRY}/accountia/auth-ms:latest \
        accountia/business-ms=${DOCKER_REGISTRY}/accountia/business-ms:latest \
        accountia/invoice-ms=${DOCKER_REGISTRY}/accountia/invoice-ms:latest \
        accountia/expense-ms=${DOCKER_REGISTRY}/accountia/expense-ms:latest \
        accountia/client-ms=${DOCKER_REGISTRY}/accountia/client-ms:latest
    
    popd > /dev/null
    
    # Apply using kustomize
    kubectl apply -k "${K8S_DIR}"
    
    # Wait for infrastructure to be ready
    log_info "Waiting for infrastructure to be ready..."
    kubectl wait --for=condition=ready pod -l app=mysql -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=postgres -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=redis -n ${NAMESPACE} --timeout=120s || true
    kubectl wait --for=condition=ready pod -l app=rabbitmq -n ${NAMESPACE} --timeout=180s || true
    
    log_info "Deployment complete! Use 'kubectl get pods -n ${NAMESPACE}' to check status."
}

# Undeploy from Kubernetes
undeploy() {
    log_info "Removing Accountia from Kubernetes..."
    
    kubectl delete -k "${K8S_DIR}" --ignore-not-found || true
    
    log_info "Undeploy complete!"
}

# Check deployment status
status() {
    log_info "Checking Accountia deployment status..."
    
    echo ""
    echo "=== Pods ==="
    kubectl get pods -n ${NAMESPACE} -o wide
    
    echo ""
    echo "=== Services ==="
    kubectl get services -n ${NAMESPACE}
    
    echo ""
    echo "=== Deployments ==="
    kubectl get deployments -n ${NAMESPACE}
    
    echo ""
    echo "=== Ingress ==="
    kubectl get ingress -n ${NAMESPACE} 2>/dev/null || echo "No ingress configured"
}

# Show logs for a service
show_logs() {
    service="${2:-api-gateway}"
    log_info "Showing logs for ${service}..."
    kubectl logs -f -l app=${service} -n ${NAMESPACE} --all-containers=true
}

# Port forward for local access
port_forward() {
    log_info "Setting up port forwarding..."
    log_info "API Gateway: http://localhost:8080"
    log_info "Eureka: http://localhost:8761"
    log_info "Keycloak: http://localhost:8180"
    log_info "RabbitMQ Management: http://localhost:15672"
    
    # Run port forwards in background
    kubectl port-forward svc/api-gateway 8080:80 -n ${NAMESPACE} &
    kubectl port-forward svc/eureka-server 8761:8761 -n ${NAMESPACE} &
    kubectl port-forward svc/keycloak 8180:8080 -n ${NAMESPACE} &
    kubectl port-forward svc/rabbitmq 15672:15672 -n ${NAMESPACE} &
    
    log_info "Press Ctrl+C to stop port forwarding"
    wait
}

# Main
case "${1:-help}" in
    build)
        build_images
        ;;
    push)
        push_images
        ;;
    deploy)
        deploy
        ;;
    undeploy)
        undeploy
        ;;
    status)
        status
        ;;
    logs)
        show_logs "$@"
        ;;
    forward)
        port_forward
        ;;
    all)
        build_images
        push_images
        deploy
        ;;
    *)
        echo "Accountia Kubernetes Deployment Script"
        echo ""
        echo "Usage: $0 <command>"
        echo ""
        echo "Commands:"
        echo "  build     - Build all Docker images"
        echo "  push      - Push images to Docker registry"
        echo "  deploy    - Deploy to Kubernetes"
        echo "  undeploy  - Remove from Kubernetes"
        echo "  status    - Show deployment status"
        echo "  logs      - Show logs (usage: $0 logs <service-name>)"
        echo "  forward   - Setup port forwarding for local access"
        echo "  all       - Build, push, and deploy"
        ;;
esac
