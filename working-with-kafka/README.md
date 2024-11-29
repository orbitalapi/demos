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
