
# Building & pushing the demo-services image

The compose file pulls `ghcr.io/orbitalapi/demos-composing-apis`, which is currently published for
`linux/amd64` only. To build it yourself (eg., natively for Apple Silicon):

1. Build the jar. The reactor builds the generated taxonomy (`taxonomy/`) first, then the Spring Boot app (`services/`):

```bash
mvn install
```

2. Build the image. The Dockerfile copies the flattened pom from `target/`, plus `services/` and `taxonomy/`:

```bash
docker build . -t ghcr.io/orbitalapi/demos-composing-apis --load
```

Then `docker compose up -d` uses your local image. Remove the `platform: linux/amd64` line from
`docker-compose.yml` if you've built for arm64.

3. To publish (needs a GitHub access token with container registry access), log in and push. Use
`buildx` with `--platform linux/amd64,linux/arm64` to publish a multi-arch image:

```bash
echo $GITHUB_ACCESS_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
docker buildx build . --platform linux/amd64,linux/arm64 -t ghcr.io/orbitalapi/demos-composing-apis --push
```
