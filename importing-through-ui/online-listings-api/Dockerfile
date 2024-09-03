FROM eclipse-temurin:17-jammy

RUN addgroup --system worker --gid 1000 && \
    adduser --system worker --ingroup worker --uid 1001

RUN mkdir -p /opt/service/
RUN chown worker:worker /opt/service/

USER worker
WORKDIR /opt/service/

EXPOSE 80

HEALTHCHECK --interval=10s \
            --timeout=3s \
            --start-period=30s \
            CMD curl -s http://localhost/actuator/ping > /dev/null || exit 1

COPY --chown=worker target/appassembler /opt/service/

CMD $BEFORE_START_COMMAND && ./bin/online-listings-api --server.port=80 $OPTIONS
