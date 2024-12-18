# Working with Kafka

This demo showcases common scenarios when working with Kafka.

It's based in a fictitious food delivery service, called Orbit Eats.

## Architecture
Our main two components are our Kafka topics, `OrderPlaced` events and `DeliveryUpdates`.

```schemaDiagram
{
   "members" : {
      "com.orbitEats.orders.OrderService" : {},
      "com.orbitEats.delivery.DeliveryService" : {}
   }
}
```

## Joining streams
We have two separate streams of events:

**Orders:**
New orders being placed:

```taxiql
stream { OrderPlaced }
```

**Delivery updates:**
Updates from the drivers as they collect and deliver orders to customers:

```taxiql
stream { DeliveryUpdate }
```

### Union types - each message as it arrives
Streams are joined based on linked IDs - we can see patchy updates by requesting a union
of the two types:

```taxiql
stream { OrderPlaced | DeliveryUpdate }
```

This shows each message as it arrives. This query is stateless by default, so there's no
way to link messages.

We can add a state store (which, by default uses the build-in Hazelcast store):

```taxiql
@StateStore
stream { OrderPlaced | DeliveryUpdate }
```

Messages are still streamed as they arrive, but we hold state, so that data from earlier
messages is joined with newer messages as they arrive.

### Intersection types - wait until a message from each source
Intersection types wait until all sources have emitted a message before emitting.

```taxiql
stream { OrderPlaced & DeliveryUpdate } 
```

Intersection types force the usage of state - so there's no need to define a `@StateStore`, unless
you wish to customize the behaviour.

## Projecting the data
Here, we'll join the data from both sources, and enrich with a REST API call to gather
customer details

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

## TODO:
 - Stream results into a cache
   - Requires a Hazelcast (or Redis) Nebula connector
 - Expose the cache on a rest API
