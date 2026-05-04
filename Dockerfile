# =============================================================
# Bank Simulator — Dockerfile (Security-Hardened)
# =============================================================

# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend

# FIX: Copy lockfile separately to leverage Docker layer cache
COPY frontend/package*.json ./
# FIX: Use ci instead of install for reproducible builds
RUN npm ci --omit=dev

COPY frontend/ ./
RUN npm run build

# Stage 2: Build Java Backend
# FIX: Use Java 17 to match pom.xml <java.version>17</java.version>
#      Original used java 22 in image but pom.xml targets 17 — mismatch.
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/ ./

# Copy built frontend into webapp
COPY --from=frontend-build /app/frontend/dist ./src/main/webapp/

RUN mvn clean package -DskipTests

# Stage 3: Run with Tomcat
# FIX: Pinned to specific digest-stable tag (update periodically)
FROM tomcat:10.1-jdk17-temurin

WORKDIR /usr/local/tomcat/webapps/

# FIX: Remove default Tomcat webapps to reduce attack surface
RUN rm -rf ROOT manager host-manager examples docs

COPY --from=backend-build /app/target/*.war ROOT.war

# FIX: Run as non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup -s /sbin/nologin appuser \
    && chown -R appuser:appgroup /usr/local/tomcat
USER appuser

EXPOSE 8080

# FIX: Use exec form (avoids shell wrapper, proper signal handling)
CMD ["catalina.sh", "run"]