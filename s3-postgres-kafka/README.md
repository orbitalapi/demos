# S3 / Postgres / Kafka

This project shows integration between S3, Postgres and Kafka

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

### how this works

* The `connections.conf` ([source](/projects/com.petflix:s3-postgres-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf)) defines how to connect to Postgres (`my-postgres-db`)


## Writing to Kafka
