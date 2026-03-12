## Overview

This demo shows how Orbital accepts a CSV file over HTTP and forwards it to a third-party REST API as JSON — automatically translating between the two formats, including differences in field names and structure.

> [!NOTE]
> This demo uses [Nebula](https://nebula.orbitalhq.com/) to stub out the target API. Check the [stubs panel](/stubs) to confirm the HTTP server is running. If you started Orbital using the Docker Compose from [start.orbitalhq.com](https://start.orbitalhq.com), Nebula should be configured automatically. See the [Nebula docs](https://orbitalhq.com/docs/testing/stubbing-services) for more.

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

With Orbital running on `localhost:9022`:

```bash
curl --location 'localhost:9022/api/q/submit' \
--header 'Content-Type: text/plain' \
--data 'part,quantity,lastUpdated
ABC-01,1000,2025-01-06
ABC-02,2000,2025-01-06
'
```

After running this, check the Nebula logs in Docker Desktop. You should see the forwarded JSON payload logged by the stub server:

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
