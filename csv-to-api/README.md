## CSV to API
This demo shows using Orbital to accept a CSV file, then publish it to a 3rd party server as JSON.

> [!TIP]
> This demo uses [Nebula](https://nebula.orbitalhq.com/) to provide the stubbed
> out API.
>
> Make sure Nebula is running (if you started Orbital using the docker composer from [start.orbitalhq.com](https://start.orbitalhq.com), then it should be configured automatically for you). 
> 
> You see the HTTP server running in the [Stub servers](/stubs) panel.
> 
> Read more about Nebula in our [docs](https://orbitalhq.com/docs/testing/stubbing-services)

## Importing this project into Orbital

> [!NOTE]
> If you're reading this within Orbital, you can skip this section - you've already done it :)
 * Open Orbital, and head to Projects -> Add New Project -> Git Repository
 * Fill in the form:
 * * **Repository URL:** `https://github.com/orbitalapi/demos`
   * Click Test Connection to populate the defaults.
   * **Path to taxi project** `/csv-to-api`
* Click Create

> [!NOTE]
> The rest of this README is intended to be read from within Orbital - Links are relative within Orbital, and interactive architecture charts only render within Orbital


### Orbital endpoint
The entry point to the demo is `endpoint.taxi`, which declares a query with an endpoint:

```taxi
@HttpOperation(method = "POST", url = "/api/q/submit")
query submitCsv(@RequestBody body: StockReportCsv[]) {
   given { body }
   call StockMonitorApi::submitStockLevels
}
```

This is callable at `http://orbitalUrl/api/q/submit`. Assuming that Orbital
is running on `localhost:9022`, you can curl this right now:

```bash
curl --location 'localhost:9022/api/q/submit' \
--header 'Content-Type: text/plain' \
--data 'part,quantity,lastUpdated
ABC-01,1000,2025-01-06
ABC-02,2000,2025-01-06
'
```

After running this, check the logs in docker desktop for Nebula. You should see the following logged out by the http server:


```
Request recevied - Body: 
 [{"id":"ABC-01","stockLevel":1000,"timestamp":"2025-01-06"},{"id":"ABC-02","stockLevel":2000,"timestamp":"2025-01-06"}]
 ```

 ### How this works
The `@RequestBody` for this query is a csv format: [StockReportCsv](/catalog/StockReportCsv), which is a simple Taxi model containing a `@Csv` annotation:

```taxi
@Csv
model StockReportCsv {
   part: PartId
   quantity: Quantity
   lastUpdated: LastUpdated
}
```

The query calls [submitStockLevels](/services/StockMonitorApi/submitStockLevels), which accpets a JSON payload of `StockLevelReport[]`:

```taxi
// JSON by default
// Note that field names differ from our CSV
parameter model StockLevelReport {
   id: PartId
   stockLevel: Quantity
   timestamp: LastUpdated
}

service StockMonitorApi {
   @HttpOperation(method = "POST", url = "http://stockMonitor/report")
   write operation submitStockLevels(@RequestBody report: StockLevelReport[]):StockUpdate
}
```

Orbital handles the translation from CSV -> JSON automatically.

> [!TIP]
> This transformation is using the standard Orbital query engine,
> and could therefore be performing enrichments (loading data from other services) 
> as well as simple data transformation.

