## Overview

This demo shows how Orbital works with Kafka streams — subscribing to topics, joining events from multiple streams, and enriching the results with data from a REST API.

It's built around a fictitious food delivery service, Orbit Eats, with two event streams: orders being placed and delivery updates from drivers.

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
stream { OrderPlaced }
```

**Delivery updates:**
```taxiql
stream { DeliveryUpdate }
```

## Joining streams

### Union types — each message as it arrives

The `|` operator subscribes to multiple topics and emits each message as it arrives, regardless of source:

```taxiql
stream { OrderPlaced | DeliveryUpdate }
```

By default this is stateless, so there's no way to correlate messages from one topic with messages from the other. Adding a `@StateStore` annotation enables Orbital to hold state across messages, so that data from an earlier `OrderPlaced` event is automatically joined with a later `DeliveryUpdate` that shares the same order ID:

```taxiql
@StateStore
stream { OrderPlaced | DeliveryUpdate }
```

Messages are still emitted as they arrive — the difference is that Orbital now has enough context to enrich each message with data from earlier events in the stream.

### Intersection types — wait for a match from each source

The `&` operator waits until a matching message has arrived from *all* sources before emitting. This is useful when you want a complete view — for example, waiting until you have both the original order and at least one delivery update before processing:

```taxiql
stream { OrderPlaced & DeliveryUpdate }
```

Intersection types implicitly require state (to hold messages until a match is found), so there's no need to add `@StateStore` explicitly.

## Projecting and enriching

The `as { }` block works the same way as in standard TaxiQL queries — Orbital maps fields from both stream sources into the target shape, and automatically calls any additional services needed to populate fields that aren't present in the stream data.

Here, `customerName: CustomerName` isn't available in either Kafka topic, so Orbital calls the relevant REST API using the customer identifier already in scope:

```taxiql
stream { OrderPlaced & DeliveryUpdate } as {
    orderId,
    items,
    status,
    timestamp,
    currentLocation,
    customerName: CustomerName
}[]
```
