## Overview

This demo shows how Orbital accepts a CSV file over HTTP and forwards it to a third-party REST API as JSON — automatically translating between the two formats, including differences in field names and structure.

## Running the demo

1. In Orbital, create a workspace (or use an empty one), choose **Play with a demo**, and pick **Consume a CSV file and publish to a 3rd party**.
2. Orbital loads this project and asks [Nebula](https://nebula.orbitalhq.com/) to start an HTTP server that stands in for the third-party
   stock monitoring API. It logs every request it receives.
3. Open the [Local environments](/stubs) page and wait until the `http` component shows as running.
   If you started Orbital with the Docker Compose file from [start.orbitalhq.com](https://start.orbitalhq.com), Nebula is already set up.
   See the [Nebula docs](https://orbitalhq.com/docs/testing/stubbing-services) for more.

## The endpoint

The entry point is a single HTTP endpoint that accepts a CSV payload and forwards it to the [StockMonitorApi](/services/StockMonitorApi):

```taxi
@HttpOperation(method = "POST", url = "/api/q/submit")
query submitCsv(@RequestBody body: StockReportCsv[]) {
   given { body }
   call StockMonitorApi::submitStockLevels
}
```

The `@RequestBody` is typed as `StockReportCsv[]` — a Taxi model annotated with `@Csv`, which tells Orbital to parse the incoming request body as CSV:

```taxi
@Csv
model StockReportCsv {
   part: PartId
   quantity: Quantity
   lastUpdated: LastUpdated
}
```

## Try it

Orbital serves the endpoint inside your workspace, so the full URL includes your organisation and workspace names.
The [endpoint page](/endpoints/submitCsv) shows the exact URL for your workspace:

```bash
curl --location 'http://localhost:9022/api/<organisation>/<workspace>/q/submit' \
--header 'Content-Type: text/plain' \
--data 'part,quantity,lastUpdated
ABC-01,1000,2025-01-06
ABC-02,2000,2025-01-06
'
```

The response is the stub API's reply:

```json
{"message":"Great, thanks"}
```

> [!NOTE]
> If your Orbital asks you to sign in, its endpoints need that sign-in too, so a plain `curl` call gets a `401`.
> Pass an API key or access token with `--header 'Authorization: Bearer <token>'`, or run this demo on an Orbital
> that doesn't require sign-in.

To see what was forwarded, check the Nebula container's logs. With the Docker Compose setup, run this from the directory
holding `docker-compose.yml`:

```bash
docker compose logs nebula | grep -A1 "Request recevied"
```

You should see the forwarded JSON payload logged by the stub server:

```json
[{"id":"ABC-01","stockLevel":1000,"timestamp":"2025-01-06"},{"id":"ABC-02","stockLevel":2000,"timestamp":"2025-01-06"}]
```

## How the transformation works

The target API's [submitStockLevels](/services/StockMonitorApi/submitStockLevels) operation accepts a `StockLevelReport[]` — a JSON format with different field names from the CSV:

```taxi
parameter model StockLevelReport {
   id: PartId
   stockLevel: Quantity
   timestamp: LastUpdated
}

service StockMonitorApi {
   @HttpOperation(method = "POST", url = "http://stockMonitor/report")
   write operation submitStockLevels(@RequestBody report: StockLevelReport[]): StockUpdate
}
```

`StockReportCsv` and `StockLevelReport` share the same semantic types — `PartId`, `Quantity`, and `LastUpdated` — even though their field names differ. Orbital uses those shared types to map between the two models automatically, converting from CSV to JSON in the process.

> [!TIP]
> Because this transformation runs through Orbital's standard query engine, it can do more than simple field mapping — it can also enrich the data by calling other services before forwarding, using exactly the same mechanism shown in the other demos.

## Troubleshooting

| Symptom | What to check |
|---|---|
| `Query was supposed to produce a StockUpdate but nothing was emitted` | Orbital couldn't call the stub API. Check that the `http` component is running on the [Local environments](/stubs) page. The **Activity** page and Orbital's logs show the underlying error. |
| `(line 1) invalid char between encapsulated token and delimiter` | The request body isn't valid CSV. Send the CSV text itself, with a header row. |
| `401` from `curl` | Your Orbital needs sign-in. See the note under [Try it](#try-it). |
