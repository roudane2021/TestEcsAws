# --- ÉTAPE 1 : BUILD (La cuisine) ---
# On utilise une image complète avec Maven pour compiler
FROM maven:3.8.5-eclipse-temurin-17 AS build

# Répertoire de travail pour la compilation
WORKDIR /build

# 1. Optimisation du cache : on télécharge les dépendances d'abord
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. On copie le code et on compile
COPY src ./src
RUN mvn clean package -DskipTests

# --- ÉTAPE 2 : RUN (Le service) ---
# On repart d'une image JRE minuscule (pas de Maven, pas de compilateur)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# On récupère UNIQUEMENT le fichier .jar généré à l'étape précédente
# On le renomme "app.jar" pour simplifier la commande finale
COPY --from=build /build/target/TestEcsAws-0.0.1-SNAPSHOT.jar app.jar

# Configuration pour ECS
EXPOSE 8081

# Commande optimisée pour la production
ENTRYPOINT ["java", "-jar", "app.jar"]