FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build
WORKDIR /workspace

COPY .tools/apache-maven-3.9.9-bin.zip /tmp/apache-maven-3.9.9-bin.zip
RUN mkdir -p /opt/maven \
    && cd /opt/maven \
    && jar -xf /tmp/apache-maven-3.9.9-bin.zip \
    && chmod +x /opt/maven/apache-maven-3.9.9/bin/mvn \
    && rm /tmp/apache-maven-3.9.9-bin.zip
ENV PATH="/opt/maven/apache-maven-3.9.9/bin:${PATH}"

COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
WORKDIR /workspace/backend
RUN mvn -q dependency:go-offline
RUN mvn -q clean package -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
WORKDIR /app

COPY --from=build /workspace/backend/target/canteen-mvp-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
