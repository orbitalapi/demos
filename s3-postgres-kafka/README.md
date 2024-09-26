# S3 / Postgres / Kafka

This project shows integration between S3, Postgres and Kafka.

In this demo, we'll cover:

 * Reading from S3
 * Enriching S3 data against an API
 * Writing enriched data into a database
 * Writing enriched data out onto Kafka
 * Reading enriched data from Kafka

We use some fictitious cinema ticket sale information for our demo.

## Importing this project into Orbital

> [!NOTE]
> If you're reading this within Orbital, you can skip this section - you've already done it :)
 * Open Orbital, and head to Projects -> Add New Project -> Git Repository
 * Fill in the form:
 * * **Repository URL:** `https://github.com/orbitalapi/demos`
   * Click Test Connection to populate the defaults.
   * **Path to taxi project** `demos/s3-postgres/kafka`
* Click Create


> [!NOTE]
> The rest of this README is intended to be read from within Orbital - Links are relative within Orbital, and interactive architecture charts only render within Orbital

## Stub services
This demo project has a number of stub services deployed.

You can see the details of the stubbed services in the [stubs panel](/stubs). The source file that drives this is 
part of this project - see [orbital/nebula/stack.nebula.kts](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fnebula%2Fstack.nebula.kts).

> [!NOTE]
> The stub services should start automatically. However, if not, check the docs on [enabling stubs](https://orbitalhq.com/docs/testing/stubbing-services#enabling-stubs)

## Reading ticket sales from S3
Ticket sales are returned from an S3 bucket

You can read them with this query:

```taxiql
find { VenueTicketSales[] }
```

### How this works
 * The [TicketsS3Bucket](/services/TicketsS3Bucket) ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=ticketSales.s3.taxi)) is defined returning a CSV containing [VenueTicketSales](/catalog/VenueTicketSales):
 * The `connections.conf` ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf)) defines how to connect to AWS

```taxi
@S3Service(connectionName = "my-aws-account")
service TicketsS3Bucket {
   @S3Operation(bucket = "cinema-ticket-sales")
   operation readBucket(filename: FilenamePattern = "ticket-sales.csv"):VenueTicketSales[]
}
```

## Enrich ticket sales with venue data
The [TicketPricesApi](/services/TicketPricesApi) is a REST API that returns ticket prices for each cinema.

This diagram shows the relationships between the two sevices:

```components
{
   "members" : {
      "TicketsS3Bucket" : {},
      "VenueTicketSales" : {},
      "TicketPricesApi" : {}
   }
}
```

We can enrich our existing query to add this data:

```taxiql
find { VenueTicketSales[] } as {
    ticketPrice: Price
    ...
}[]
```

Orbital calls the REST API to fetch the Ticket Price for each cinema, and include it in our response.

## Ingesting Ticket sales to a database
Our project has a Postgres database - [MyPostgresService](/services/MyPostgresService), which exposes a table for
reading [VenueTicketSalesRecord](/catalog/VenueTicketSalesRecord)'s - the model of our database.

> [!NOTE]
> In this example, we're not using an existing schema, so Orbital will create the database table
> the first time for us. 
> 
> More commonly, you'd be using an existing database schema. We cover how to work with
> database schemas in our [docs](https://orbitalhq.com/docs/describing-data-sources/databases#describing-tables-in-taxi)


```taxi
@DatabaseService(connection = "my-postgres-db")
service MyPostgresService {

   // table declares read operations, such as querying
   table ticketSales : VenueTicketSalesRecord[]
   
   // An upsert operation, for performing upsert queries
   @UpsertOperation
   write operation saveTicketSalesRecord(VenueTicketSalesRecord):VenueTicketSalesRecord
}
```

We can update our query to write to our database:

```taxiql
find { VenueTicketSales[] }
call MyPostgresService::saveTicketSalesRecord
```

### Automatic transformation (CSV -> Database record)
The [VenueTicketSales csv format](/catalog/VenueTicketSales) read from the S3 bucket isn't the same format
as the [VenueTicketSalesRecord database record](/catalog/VenueTicketSalesRecord):

```taxi
@Table(connection = "my-postgres-db", schema = "public" , table = "ticketSales" )
closed parameter model VenueTicketSalesRecord {
   @Id @GeneratedId
   recordId : RecordId? // <-- added a primary key record
   title : Title
   filmId: FilmId
   screeningDate : ScreeningDate
   cinemaName: CinemaName
   cinemaId: CinemaId
   tickets: TicketsSold
   ticketPrice: Price // <-- added the price, coming from a REST API
   revenue: TicketRevenue 
}
```

Orbital detects that the S3 bucket's type of `VenueTicketSales` doesn't match the required data of `VenueTicketSalesRecord`, 
so automatically transforms the data.

As we need to load some data from a REST API (the [TicketPricesApi](/services/TicketPricesApi)), Orbital automatically invokes
the REST API as before, enriching the data prior to persisting.

### How this works

* The `connections.conf` ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf)) defines how to connect to Postgres (`my-postgres-db`)


## Writing to Kafka
We can also write the messages from our S3 files to Kafka.

Our schema contains a Kafka service declared named [MyKafkaConnectionService](/services/MyKafkaConnectionService) ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=ticketSales.kafka.taxi))
which publishes a stream of `VenueTicketSalesMessage`:

```taxi
@KafkaService(connectionName = "my-kafka-connection")
service MyKafkaConnectionService {

   // A stream of messages, we can read from
   @KafkaOperation(topic = "ticket-sales" , offset = "earliest")
   stream venueTicketSales : Stream<VenueTicketSalesMessage>

   // A publishing operation, allowing us to write messages to the topic
   @KafkaOperation( topic = "ticket-sales", offset = "earliest" )
   write operation publishTicketSalesMessage(VenueTicketSalesMessage):VenueTicketSalesMessage
}
```

We can issue a query to read data from our S3 bucket, and write to Kafka - again, enriching the data using our REST API
to add cinema ticket prices.

```taxiql
import MyKafkaConnectionService
find { VenueTicketSales[] } 
call MyKafkaConnectionService::publishTicketSalesMessage
```

### How this works:
 * The `connections.conf` file ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf)) defines how to connect to Kafka
 * Orbital connects to S3 (as discussed above), and reads the data from the CSV file
 * Orbital detects that the S3 bucket's type of `VenueTicketSales` doesn't match the required data of `VenueTicketSalesMessage`, so automatically transforms the data, enriching the message by calling our REST API.
 * Orbital writes each message onto Kafka.
   * Note:  `VenueTicketSalesMessage` doesn't declare a message format (unlike `VenueTicketSales` - which is annotated with `@Csv`). Therefore, it's assumed to be JSON
   * Orbital supports writing to Kafka in multiple different formats, like Avro, Protobuf, and more. 

## Reading back off from Kafka
To read the messages back off of Kafka, we can issue another query:

```taxiql
import VenueTicketSalesMessage
stream { VenueTicketSalesMessage }
```

This streams data directly from Kafka.

## What's next
That's all for this tutorial.

Other things we didn't explore are:
 * [Publishing our data as an API](https://orbitalhq.com/docs/querying/queries-as-endpoints)
 * [Creating a streaming pipeline of data](https://orbitalhq.com/docs/querying/streaming-data)
 * [Adding security policies](https://orbitalhq.com/docs/data-policies/data-policies)

To find out more, reach out to the Orbital team on [Slack](https://join.slack.com/t/orbitalapi/shared_invite/zt-697laanr-DHGXXak5slqsY9DqwrkzHg) or on [Github](https://github.com/orbitalapi/orbital)
