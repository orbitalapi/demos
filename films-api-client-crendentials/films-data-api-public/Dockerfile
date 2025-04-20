FROM eclipse-temurin:21-jammy

RUN addgroup --system worker --gid 1000 && \
    adduser --system worker --ingroup worker --uid 1001

RUN mkdir -p /opt/service/
RUN chown worker:worker /opt/service/

USER worker
WORKDIR /opt/service/

COPY --chown=worker target/appassembler /opt/service/

CMD $BEFORE_START_COMMAND && ./bin/films-data-api-public $OPTIONS