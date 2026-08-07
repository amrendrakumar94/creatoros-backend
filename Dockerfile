FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -ntp clean package -DskipTests

RUN java -Djarmode=tools -jar target/creatoros-backend-*.jar \
        extract --layers --launcher --destination /build/layers


FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
 && apt-get install --no-install-recommends -y curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system --gid 1001 creatoros \
 && useradd --system --uid 1001 --gid creatoros --home-dir /app --shell /usr/sbin/nologin creatoros \
 && mkdir -p /app /var/log/creatoros \
 && chown -R creatoros:creatoros /app /var/log/creatoros

WORKDIR /app

COPY --from=builder --chown=creatoros:creatoros /build/layers/dependencies/ ./
COPY --from=builder --chown=creatoros:creatoros /build/layers/spring-boot-loader/ ./
COPY --from=builder --chown=creatoros:creatoros /build/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=creatoros:creatoros /build/layers/application/ ./

USER creatoros

ENV SERVER_PORT=8082 \
    LOG_DIR=/var/log/creatoros \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=5 \
    CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
