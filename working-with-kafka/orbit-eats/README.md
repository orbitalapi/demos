## Overview

This demo shows how Orbital works with Kafka streams — subscribing to topics, joining events from multiple streams, enriching the results with data from a REST API, and writing the joined stream to MongoDB and Hazelcast.

It's built around a fictitious food delivery service, Orbit Eats, with two event streams: orders being placed and delivery updates from drivers.

## Running the demo

1. In Orbital, create a workspace (or use an empty one), choose **Play with a demo**, and pick **Joining and enriching Kafka streams, writing to Mongo**.
2. Orbital loads this project and asks [Nebula](https://nebula.orbitalhq.com) to start the demo's infrastructure:
   * a Kafka broker with two topics, `orders` and `deliveries`. New orders are published every 750 ms, and each order then moves
     through `DRIVER_ASSIGNED` → `PICKED_UP` → `IN_TRANSIT` → `DELIVERED`
   * an HTTP server for the customer API
   * a MongoDB database and a Hazelcast cache, which the joined stream is written to
3. Open the [Local environments](/stubs) page and wait until all components show as running. The Kafka image is
   large, so the first start can take a minute.
4. Open the **Code editor** and run the queries below. Stream queries keep running and show results as they arrive.
   Click **Cancel** (where the **Run** button was) when you've seen enough.

You don't need to configure anything. Connection details are read from variables that Nebula publishes once the
containers are up. See [`connections.conf`](/projects/com.orbitEats:working-with-kafka:0.1.0/source?selectedFile=orbital%2Fconfig%2Fconnections.conf).

## Key services

```schemaDiagram
{
   "members" : {
      "com.orbitEats.orders.OrderService" : {},
      "com.orbitEats.delivery.DeliveryService" : {}
   }
}
```

* [OrderService](/services/com.orbitEats.orders.OrderService) — publishes `OrderPlaced` events when a new order is received
* [DeliveryService](/services/com.orbitEats.delivery.DeliveryService) — publishes `DeliveryUpdate` events as drivers collect and deliver orders

## Reading individual streams

Each topic can be subscribed to independently. TaxiQL uses `stream { }` rather than `find { }` for streaming sources — the query stays open and emits each message as it arrives.

**Orders:**
```taxiql
import com.orbitEats.orders.OrderPlaced
stream { OrderPlaced }
```

**Delivery updates:**
```taxiql
import com.orbitEats.delivery.DeliveryUpdate
stream { DeliveryUpdate }
```

## Joining streams

### Union types — each message as it arrives

The `|` operator subscribes to multiple topics and emits each message as it arrives, regardless of source:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced
stream { OrderPlaced | DeliveryUpdate }
```

By default this is stateless, so there's no way to correlate messages from one topic with messages from the other. Adding a `@StateStore` annotation enables Orbital to hold state across messages, so that data from an earlier `OrderPlaced` event is automatically joined with a later `DeliveryUpdate` that shares the same order ID:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced

@StateStore
stream { OrderPlaced | DeliveryUpdate }
```

Messages are still emitted as they arrive — the difference is that Orbital now has enough context to enrich each message with data from earlier events in the stream.

### Intersection types — wait for a match from each source

The `&` operator waits until a matching message has arrived from *all* sources before emitting. This is useful when you want a complete view — for example, waiting until you have both the original order and at least one delivery update before processing:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced

stream { OrderPlaced & DeliveryUpdate }
```

Intersection types implicitly require state (to hold messages until a match is found), so there's no need to add `@StateStore` explicitly.

## Projecting and enriching

The `as { }` block works the same way as in standard TaxiQL queries — Orbital maps fields from both stream sources into the target shape, and automatically calls any additional services needed to populate fields that aren't present in the stream data.

Here, `customerName: CustomerName` isn't available in either Kafka topic, so Orbital calls the relevant REST API using the customer identifier already in scope:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced

stream { OrderPlaced & DeliveryUpdate } as {
    orderId,
    items,
    status,
    timestamp,
    currentLocation,
    customerName: CustomerName
}[]
```

> [!NOTE]
> `CUST-1` deliberately has no name in the customer API, so its orders show `customerName: null`.
> This shows how Orbital handles gaps in enrichment data.

## Writing the stream to MongoDB

Rather than just watching the joined stream, we can write it somewhere. [`orders.mongo.taxi`](/projects/com.orbitEats:working-with-kafka:0.1.0/source?selectedFile=src%2Forders.mongo.taxi)
defines a Mongo collection, `order-updates`, and an upsert operation keyed on `orderId`:

```taxi
@Collection(connection = "orbit-eats-mongo", collection = "order-updates")
parameter model OrderDeliveryStatusMongoRecord {
   @UniqueIndex
   orderId: OrderId
   items: OrderItem[]
   customerId : CustomerId
   deliveryStatus : DeliveryStatus
   customerName: CustomerName
}
```

The `call` clause sends each joined message to the upsert operation. Orbital builds each `OrderDeliveryStatusMongoRecord` from the
order, the delivery update and the customer API:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced

stream { OrderPlaced & DeliveryUpdate }
call OrderDeliveryMongoService::upsert
```

Because the record is keyed on `orderId`, each order has one document, updated as its delivery status changes.
Leave the query running for a few seconds, cancel it, then read back what was written:

```taxiql
find { OrderDeliveryStatusMongoRecord[] }
```

The same query is saved in the project as [`streamToMongo`](/endpoints/streamToMongo). To keep it running in the background,
open it on the [Endpoints](/endpoints) page and switch on **Run stream**.

## Writing to Hazelcast, and serving it over HTTP

[`orders.cache.taxi`](/projects/com.orbitEats:working-with-kafka:0.1.0/source?selectedFile=src%2Forders.cache.taxi) does the same with a Hazelcast map:

```taxiql
import com.orbitEats.delivery.DeliveryUpdate
import com.orbitEats.orders.OrderPlaced

stream { OrderPlaced & DeliveryUpdate }
call HazelcastService::upsert
```

(Also saved as [`streamToHazelcast`](/endpoints/streamToHazelcast).)

Two HTTP endpoints in [`CurrentOrders.taxi`](/projects/com.orbitEats:working-with-kafka:0.1.0/source?selectedFile=src%2FCurrentOrders.taxi)
read from that map, returning orders that haven't been delivered yet:

* [`AllCurrentOrders`](/endpoints/AllCurrentOrders): `GET /api/<organisation>/<workspace>/q/currentOrders`
* [`CurrentOrdersForCustomer`](/endpoints/CurrentOrdersForCustomer): `GET /api/<organisation>/<workspace>/q/currentOrders/{customerId}` (try `CUST-2`)

## Publishing a stream over HTTP

Stream queries can be published over HTTP too. [`orderStream`](/endpoints/orderStream) serves the enriched stream from above, and
[`liveOrders`](/endpoints/liveOrders) serves a smaller projection:

```taxi
@HttpOperation(method = "GET", url = "/api/q/orders")
query orderStream {
   stream { OrderPlaced & DeliveryUpdate } as { ... }[]
}
```

> [!IMPORTANT]
> Published streams start **disabled**. Before calling the URL, open the stream on the [Endpoints](/endpoints) page and switch on
> **Run stream**. Results are sent as [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events),
> one per message.

## Calling the endpoints

Orbital serves endpoints inside your workspace, so the full URL includes your organisation and workspace names, as above.
Each endpoint's page on the [Endpoints](/endpoints) page shows its exact URL.

If your Orbital asks you to sign in, its endpoints need that sign-in too, so a plain `curl` call gets a `401`.
Open the URL in the browser you're signed in with instead, or pass an API key or access token to `curl` with
`-H 'Authorization: Bearer <token>'`. If your Orbital doesn't require sign-in, `curl` works directly:

```bash
curl "http://localhost:9022/api/<organisation>/<workspace>/q/currentOrders" | jq
curl -N -H "Accept: text/event-stream" "http://localhost:9022/api/<organisation>/<workspace>/q/orders"
```

## Troubleshooting

| Symptom | What to check |
|---|---|
| A stream query shows no results | The Kafka broker is still starting. Check the [Local environments](/stubs) page. |
| `Cannot fetch Hazelcast connection … no default Hazelcast connections configured` | The Hazelcast container isn't reachable yet. Check that `orbit-eats-hazelcast` is healthy on the **Data sources** page. Stateful queries (`@StateStore`, `&`) need it. |
| Calling `/q/orders` or `/q/liveOrders` hangs | The stream is disabled. Switch on **Run stream** on its endpoint page. |
| `401` when calling an endpoint with `curl` | Your Orbital needs sign-in. See [Calling the endpoints](#calling-the-endpoints). |
