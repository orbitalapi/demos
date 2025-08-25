## Warehouse delivery

Two different types of delivery updates:

**Surepost**
```taxiql
stream { SurePostDeliveryEvent }
```

**FastShip**
stream { FastShipDeliveryEvent }


```schemaDiagram
{
  "members" : {
    "SurePostKafkaService": {},
    "FastShipKafkaService": {},
    "FastShipDeliveryEvent":{},
    "SurePostDeliveryEvent" : {}
  }
}
```


### SurePost delivery updates

```taxiql
stream { SurePostDeliveryEvent } as {
    productName: ProductName
    ...
}[]

```


### Fast ship delivery updates

```taxiql
stream { FastShipDeliveryEvent } as {
    productName: ProductName
    ...
}[]
```

 * Returning `productName` requires extra hop, as FastShip data includes SKU, not ProductId

## Tracking over / under

```taxiql
stream { FastShipDeliveryEvent } as {
    productName: ProductName
    orderedQuantity: OrderedQuantity
    ...
}[]
```


Add in some text:

```taxiql
stream { FastShipDeliveryEvent } as {
    productName: ProductName
    orderedQuantity: OrderedQuantity
    orderDiscrepancy: String = when {
        DeliveredQuantity > OrderedQuantity -> "Excess stock delivered"
        DeliveredQuantity < OrderedQuantity -> "Insufficient stock delivered"
        else -> "Correct quantity"
    }
    quantityDelta: Int = DeliveredQuantity - OrderedQuantity
    ...
}[]
```

Join everything together:


```taxiql
stream { FastShipDeliveryEvent | SurePostDeliveryEvent } as {
    productName: ProductName
    orderedQuantity: OrderedQuantity
    orderDiscrepancy: String = when {
        DeliveredQuantity > OrderedQuantity -> "Excess stock delivered"
        DeliveredQuantity < OrderedQuantity -> "Insufficient stock delivered"
        else -> "Correct quantity"
    }
    quantityDelta: Int = DeliveredQuantity - OrderedQuantity
    supplier: SupplierId
    deliveryDate: DeliveryDate
}[]
```

## Updating the stock API:

```taxiql
stream { FastShipDeliveryEvent | SurePostDeliveryEvent } as {
      productName: ProductName
      productId: ProductId
      orderedQuantity: OrderedQuantity
      orderDiscrepancy: String = when {
         DeliveredQuantity > OrderedQuantity -> "Excess stock delivered"
         DeliveredQuantity < OrderedQuantity -> "Insufficient stock delivered"
         else -> "Correct quantity"
      }
      quantityDelta: Int = DeliveredQuantity - OrderedQuantity
      supplier: SupplierId
      deliveryDate: DeliveryDate
      deliveredQuantity: DeliveredQuantity
   }[]
   call DeliveriesApi::updateStockLevels
```