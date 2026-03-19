FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build
WORKDIR /workspace

# Install basic tools and certificates
RUN apt-get update && apt-get install -y \
    ca-certificates \
    wget \
    tar \
    curl \
    && update-ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install Maven
RUN wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -O /tmp/maven.tar.gz \
    && mkdir -p /opt/maven \
    && tar -xzf /tmp/maven.tar.gz -C /opt/maven \
    && ln -s /opt/maven/apache-maven-3.9.9/bin/mvn /usr/bin/mvn \
    && rm -f /tmp/maven.tar.gz

# Prefer IPv4 to reduce network handshake issues in some environments
ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true -Dhttps.protocols=TLSv1.2"
ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"

# Maven mirror config
COPY settings.xml /root/.m2/settings.xml

# Copy source
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src

WORKDIR /workspace/backend

# Build
RUN mvn -s /root/.m2/settings.xml -q dependency:go-offline
RUN mvn -s /root/.m2/settings.xml -q clean package -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
WORKDIR /app

COPY --from=build /workspace/backend/target/canteen-mvp-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]