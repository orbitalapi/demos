# Orbital Petflix Films Demo - Basic Services

> **Status (Orbital 0.39):** the compose file follows the Orbital start-script layout (`./orbital_data`,
> `orbital.*` options). On Apple Silicon, Orbital runs natively; the demo images
> (`ghcr.io/orbitalapi/demos-films-pagila`, `ghcr.io/orbitalapi/demos-films-ui-importing-listings-api`)
> and the pinned `confluentinc/cp-*:7.3.1` Kafka images are published for `linux/amd64` only, so they
> carry `platform: linux/amd64` and Docker Desktop runs them under emulation. They take longer to
> start (give Kafka a minute or two) but work. Rebuilding the demo images multi-arch is tracked in ORB-1251.

This repo contains services required for the Orbital [APIs, databases and Kafka tutorial](https://orbitalhq.com/docs/guides/apis-databases-kafka).

Specifically:
 * A database containing films
 * A Rest API containing listings, telling us where we can watch these films
 * A Kafka topic, that tells use when new releases are announced

This demo is "naked" - the services don't contain any Orbital or Taxi code or concepts.

Throughout the demo, services and data sources are imported through the UI, and then edited in the UI to add semantic data.

There are other ways to import data sources, not covered in this demo, such as:
 * Embedding metadata in OpenAPI and Protobuf specs
 * Publishing schemas directly to Orbital (from applications)

These are covered in other demos.

## Running

```bash
mkdir -p orbital_data   # so it's owned by you, not root
docker compose up -d
```

then open http://localhost:9022. Orbital asks you to sign in (or create a free account) the first time;
that's how it fetches its license.

Orbital keeps everything it writes - including the `petflix` project you create during the tutorial -
under `./orbital_data`, laid out the same way as a stack started from https://start.orbitalhq.com:
projects live under `orbital_data/orgs/workspaces/default/default/orbital/workspace/projects/`.

Orbital runs as your user so those files stay editable. On Linux, if your user isn't `1000:1000`,
write your ids to a `.env` file before starting:

```bash
printf 'UID=%s\nGID=%s\n' "$(id -u)" "$(id -g)" > .env
```

| Service   | Host port | Notes |
|-----------|-----------|-------|
| Orbital   | 9022      | UI and API |
| Postgres  | 35432     | Pagila (`pagila` / `root` / `admin`); inside the compose network it's `pg-pagila:5432` |
| films-api | 9981      | OpenAPI spec at http://localhost:9981/v3/api-docs |
| Kafka     | 9092      | `kafka:29092` from inside the network |

Postgres is mapped to 35432 rather than 25432, so this stack can sit next to one started by the start script.
