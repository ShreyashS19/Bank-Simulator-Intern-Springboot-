# Step 1: Build React Frontend
FROM node:20 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Step 2: Build Java Backend with Maven (Java 22)
FROM maven:3.9.6-eclipse-temurin-22 AS backend-build
WORKDIR /app
COPY backend/ ./

# Copy built frontend files into webapp directory
COPY --from=frontend-build /app/frontend/build ./src/main/webapp/

RUN mvn clean package -DskipTests

# Step 3: Run WAR using Tomcat with Java 22
FROM tomcat:10.1-jdk22
WORKDIR /usr/local/tomcat/webapps/

# Remove default ROOT app
RUN rm -rf ROOT

# Copy the built WAR file and rename to ROOT.war
COPY --from=backend-build /app/target/*.war ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
