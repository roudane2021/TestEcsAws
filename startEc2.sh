#!/bin/bash

# 📁 Dossier de logs
LOG_DIR="/var/log/setup-script"
mkdir -p "$LOG_DIR"

TIMESTAMP=$(date +'%Y-%m-%d_%Hh%M')

APP_LOG="$LOG_DIR/${TIMESTAMP}_app.log"
SYS_LOG="$LOG_DIR/${TIMESTAMP}_system.log"

# 🔵 FD 3 → logs applicatifs
exec 3> >(tee -a "$APP_LOG")

# 🔴 stdout + stderr → logs système
exec > >(tee -a "$SYS_LOG") 2>&1

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" >&3
}

log "=== DEBUT DU SCRIPT ==="

# Mise à jour
log "Mise à jour du système"
yum update -y

# Java
log "Installation Java 7"
yum install -y java-1.7.0-openjdk java-1.7.0-openjdk-devel

log "Vérification Java"
java -version

# Docker
log "Installation Docker"
yum install -y docker

log "Démarrage Docker"
systemctl start docker
systemctl enable docker

log "Ajout ec2-user au groupe docker"
usermod -aG docker ec2-user

# AWS CLI
log "Installation AWS CLI v2"
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install

# Vérifications
log "Vérification Docker"
docker --version

log "Vérification AWS CLI"
aws --version

# 🐳 Docker pull + run
log "Pull de l'image Docker"
docker pull roudane/gestion_commande:latest

log "Lancement du container"
docker run -d -p 80:80 --name gestion_commande roudane/gestion_commande:latest

log "=== FIN DU SCRIPT ==="