FROM eclipse-temurin:17.0.10_7-jre-focal

RUN addgroup --system worker --gid 1000 && \
    adduser --system worker --ingroup worker --uid 1001

RUN mkdir -p /opt/service/
RUN chown worker:worker /opt/service/

USER worker
WORKDIR /opt/service/

COPY --chown=worker esg-service/target/esg-service.jar /opt/service/

# disableJarChecking=true is needed when using a security manager...
CMD $BEFORE_START_COMMAND && java $JVM_OPTS -jar esg-service.jar $OPTIONS
