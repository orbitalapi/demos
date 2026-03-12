## Overview

This demo shows how Orbital connects S3, a REST API, a Postgres database, and Kafka — using a single query model to read, enrich, and route data across all four, without writing any transformation or integration code.

We'll use fictitious cinema ticket sales data to walk through:

* Reading CSV data from an S3 bucket
* Enriching it with ticket prices from a REST API
* Writing the enriched data to Postgres
* Publishing the same data onto a Kafka topic
* Reading it back off Kafka as a stream

At each step, Orbital detects what transformation is needed between source and target types, and handles it automatically.

> [!NOTE]
> This demo runs stub services via [Nebula](https://nebula.orbitalhq.com). Check the [stubs panel](/stubs) to confirm everything is running. If stubs haven't started automatically, see the docs on [enabling stubs](https://orbitalhq.com/docs/testing/stubbing-services#enabling-stubs). The stub configuration lives in [`stack.nebula.kts`](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fnebula%2Fstack.nebula.kts).

## Key services

```components
{
   "members" : {
      "TicketsS3Bucket" : {},
      "TicketPricesApi" : {},
      "MyPostgresService" : {},
      "MyKafkaConnectionService" : {}
   }
}
```

* [TicketsS3Bucket](/services/TicketsS3Bucket) — reads a CSV of ticket sales from an S3 bucket
* [TicketPricesApi](/services/TicketPricesApi) — a REST API returning ticket prices per cinema
* [MyPostgresService](/services/MyPostgresService) — a Postgres-backed table for persisting enriched records
* [MyKafkaConnectionService](/services/MyKafkaConnectionService) — a Kafka topic for streaming ticket sales messages

Connection details for all four are defined in [`connections.conf`](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf).

## Reading ticket sales from S3

The [TicketsS3Bucket](/services/TicketsS3Bucket) is declared in the schema as returning `VenueTicketSales[]` from a CSV file:

```taxi
@S3Service(connectionName = "my-aws-account")
service TicketsS3Bucket {
   @S3Operation(bucket = "cinema-ticket-sales")
   operation readBucket(filename: FilenamePattern = "ticket-sales.csv"): VenueTicketSales[]
}
```

To read from it:

```taxiql
find { VenueTicketSales[] }
```

Orbital resolves `VenueTicketSales` to the S3 bucket, connects using the credentials in `connections.conf`, and parses the CSV automatically.

## Enriching with ticket prices

The [TicketPricesApi](/services/TicketPricesApi) exposes ticket prices per cinema. Because `Price` is a semantic type that the API is known to provide, adding it to the output shape is enough to trigger the call — Orbital figures out which service to call and what to pass it:

```taxiql
find { VenueTicketSales[] } as {
    ticketPrice: Price
    ...
}[]
```

Orbital calls the REST API once per record, using the cinema identifier already in scope, and merges the result into the response. Check the **Profiler tab** to see this call in the query plan.

## Writing enriched data to Postgres

The [MyPostgresService](/services/MyPostgresService) is declared with an upsert write operation:

```taxi
@DatabaseService(connection = "my-postgres-db")
service MyPostgresService {
   table ticketSales : VenueTicketSalesRecord[]

   @UpsertOperation
   write operation saveTicketSalesRecord(VenueTicketSalesRecord): VenueTicketSalesRecord
}
```

To ingest ticket sales into the database:

```taxiql
find { VenueTicketSales[] }
call MyPostgresService::saveTicketSalesRecord
```

`VenueTicketSales` (the S3 CSV format) and `VenueTicketSalesRecord` (the database model) are different types — the database record includes a generated primary key and the `Price` field from the REST API, which isn't in the CSV:

```taxi
@Table(connection = "my-postgres-db", schema = "public", table = "ticketSales")
closed parameter model VenueTicketSalesRecord {
   @Id @GeneratedId
   recordId: RecordId?       // generated primary key
   title: Title
   filmId: FilmId
   screeningDate: ScreeningDate
   cinemaName: CinemaName
   cinemaId: CinemaId
   tickets: TicketsSold
   ticketPrice: Price        // enriched from the REST API
   revenue: TicketRevenue
}
```

Orbital detects the type mismatch between source and target, automatically enriches the data by calling the `TicketPricesApi`, and transforms the result into the `VenueTicketSalesRecord` shape before writing. No mapping code required.

> [!NOTE]
> This demo doesn't use a pre-existing database schema, so Orbital creates the table on first run. If you're working with an existing schema, see the [database docs](https://orbitalhq.com/docs/describing-data-sources/databases#describing-tables-in-taxi).

## Publishing to Kafka

The [MyKafkaConnectionService](/services/MyKafkaConnectionService) declares both a readable stream and a write operation on the `ticket-sales` topic:

```taxi
@KafkaService(connectionName = "my-kafka-connection")
service MyKafkaConnectionService {
   @KafkaOperation(topic = "ticket-sales", offset = "earliest")
   stream venueTicketSales: Stream<VenueTicketSalesMessage>

   @KafkaOperation(topic = "ticket-sales", offset = "earliest")
   write operation publishTicketSalesMessage(VenueTicketSalesMessage): VenueTicketSalesMessage
}
```

To publish the S3 data onto Kafka:

```taxiql
find { VenueTicketSales[] }
call MyKafkaConnectionService::publishTicketSalesMessage
```

As with the database write, Orbital detects that `VenueTicketSales` doesn't match `VenueTicketSalesMessage`, automatically enriches each record via the REST API, and transforms into the target type before publishing. `VenueTicketSalesMessage` doesn't declare a format annotation, so Orbital writes it as JSON by default. Avro, Protobuf, and other formats are also supported.

## Reading back from Kafka

To consume the messages back off the topic as a stream:

```taxiql
stream { VenueTicketSalesMessage }
```

Orbital connects to the `ticket-sales` topic and streams messages as they arrive, deserialising from JSON into `VenueTicketSalesMessage` automatically.

## What's next

Other areas worth exploring from here:

* [Publishing query results as a REST API](https://orbitalhq.com/docs/querying/queries-as-endpoints)
* [Building streaming data pipelines](https://orbitalhq.com/docs/querying/streaming-data)
* [Adding data security policies](https://orbitalhq.com/docs/data-policies/data-policies)

Questions? Find us on [Slack](https://join.slack.com/t/orbitalapi/shared_invite/zt-697laanr-DHGXXak5slqsY9DqwrkzHg) or [GitHub](https://github.com/orbitalapi/orbital).
