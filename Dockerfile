FROM eclipse-temurin:21.0.10_7-jre-jammy@sha256:3d6cc4b896cbde42af4b3c4d150d64e554611100b52085d829a0ead893891215
LABEL org.opencontainers.image.title="fake-esfinge" org.opencontainers.image.version="0.1.0" simulator.contract="2026/V6/6.06"
RUN groupadd --gid 10001 simulator && useradd --uid 10001 --gid 10001 --no-create-home simulator \
    && mkdir -p /opt/simulator /data && chown 10001:10001 /opt/simulator /data
WORKDIR /opt/simulator
COPY --chown=10001:10001 target/fake-esfinge-0.1.0.jar app.jar
ENV SIMULATOR_DATABASE=/data/simulator.sqlite
USER 10001:10001
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/simulator/app.jar"]
