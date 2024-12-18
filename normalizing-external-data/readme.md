
## Overview
This project explores a standard ETL task: Ingesting, normalizing and enriching data from multiple
different feeds.

Here's what we'll cover:

 * Reading multiple CSV files, in different formats
 * Normalizing the data:
   * Handling differences in date formats
   * Enriching it against a REST API
   * Applying some business logic to calcualte two derived fields

## Raw data


### Sydney warehouse report:
```taxiql
find { SydneyWarehouseStockReport[] }
```
| itemId | count | price  | location | reorderPoint | minOrder | timestamp              | lastDelivery |
|--------|-------|--------|----------|--------------|----------|------------------------|--------------|
| 1001   | 132   | 305.99 | Z-101    | 95           | 45       | 12-15-2024 02:32:15 pm | 12-10-2024   |
| 1002   | 82    | 915.99 | Y-205    | 75           | 20       | 12-15-2024 02:32:15 pm | 12-08-2024   |

### London warehouse report:
```taxiql
find { LondonWarehouseStockReport[] }
```
| SKU        | Stock_Level | Unit_Price | Bin_Location | Reorder_Level | Min_Order_Qty | Last_Updated        | Last_Received |
|------------|-------------|------------|--------------|---------------|---------------|---------------------|---------------|
| THRUST-001 | 145         | 299.99     | A-101        | 100           | 50            | 15/12/2024 14:32:15 | 10/12/2024    |
| SHIELD-002 | 78          | 899.99     | B-205        | 80            | 25            | 15/12/2024 14:32:15 | 08/12/2024    |



There's a handful of key differences here:

 * Sydney uses their own internal product number, whereas London uses our standard proudct SKU's
 * Column names differ between each dataset
 * London and Sydney both use different date formats (neither are standard)
 * Neither provides product names, only identifiers

## Normalizing
We want to present the data from both feeds in the same standard format.

We can use a TaxiQL query for this - here it is for Sydney:

**Sydney:**

```taxiql
// Define a common calculation to use in our queries
type RemainingBeforeReorder = StockLevel - ReorderLevel
find { SydneyWarehouseStockReport[] } as {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue : StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}[]
```

The same query, just with a different `find { }` clause also works for London:

**London:**

```taxiql
// Define a common calculation to use in our queries
type RemainingBeforeReorder = StockLevel - ReorderLevel
find { LondonWarehouseStockReport[] } as {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue : StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}[]
```

## Calculated fields
There's two calculated fields here.

### StockValue
The stock value is calculated as `StockLevel * Price`:

```taxiql
find { LondonWarehouseStockReport[] } as {
    // other fields omitted
    stockValue : StockLevel * Price
}[]
```

### Reorder level
The reorder status is a little more complex - we're using a `when` clause:

```taxiql
find { LondonWarehouseStockReport[] } as {
    // other fields omitted
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}[]
```

### Lineage
In each of these calculated fields, be sure to click on a cell in the result table, and
see the lineage of how the data was calculated

## Extracting the report to it's own type
There's a bit of duplication here, so let's factor the target format out to it's own type:

```taxi
model NormalizedReport  {
    sku: SKU
    stockLevel: StockLevel
    reorderLevel: ReorderLevel
    price: Price
    lastUpdated: WarehouseReportLastUpdated
    productName: ProductName
    stockValue : StockLevel * Price
    reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}
```

We can then simplify our reports down to:

**Sydney:**
```taxiql
find { SydneyWarehouseStockReport[] } as StandardWarehouseReport[]
```

**London:**
```taxiql
find { LondonWarehouseStockReport[] } as StandardWarehouseReport[]
```

When running these, be sure to check the profiler tab, to see that the integration plan is different for each.

## Persisting to a database
We have a model and service defined for a database format:

```taxi
@Table(connection = "my-postgres-db", schema = "public" , table = "warehouseReport" )
closed parameter model DbStandardWarehouseReport {
   @Id @GeneratedId
   recordId : RecordId?
   location: WarehouseLocation
   sku: SKU
   stockLevel: StockLevel
   reorderLevel: ReorderLevel
   price: Price
   lastUpdated: WarehouseReportLastUpdated
   productName: ProductName
   stockValue : StockLevel * Price
   reorderStatus: String = when {
        RemainingBeforeReorder < 10 && RemainingBeforeReorder > 0 -> "Reorder soon"
        RemainingBeforeReorder < 0 -> "Time to reorder"
        else -> "Sufficient Stock"
    }
}


@DatabaseService(connection = "my-postgres-db")
service MyPostgresService {
   table warehouseReport : DbStandardWarehouseReport[]
   @UpsertOperation
   write operation saveWarehouseReport(DbStandardWarehouseReport):DbStandardWarehouseReport
}
```

### Table creation
Orbital takes care of creating the table if it doesn't already exist (as is the case the 
first time you run this demo).


### Inserting data
**Sydney:**
```taxiql
given { WarehouseLocation = 'Sydney'}
find { SydneyWarehouseStockReport[] } 
call MyPostgresService::saveWarehouseReport
```


**London:**
```taxiql
given { WarehouseLocation = 'London'}
find { LondonWarehouseStockReport[] } 
call MyPostgresService::saveWarehouseReport
```

### Reading data from our DB
After running the above queries a couple of times, you can read the data back out of the database:

```taxiql
find { DbStandardWarehouseReport[] }
```

## Setting up a trigger
To allow us to run this, we're going to expose a REST endpoint triggering the updates:

**London:**
```taxi
@HttpOperation(method = "POST", url = "/api/q/updateLondonWarehouse")
query UpdateLondonWarehouse {
   given { WarehouseLocation = 'London'}
   find { LondonWarehouseStockReport[] } 
   call MyPostgresService::saveWarehouseReport
}
```

**Sydney:**
```taxi
@HttpOperation(method = "POST", url = "/api/q/updateSydneyWarehouse")
query UpdateSydneyWarehouse {
   given { WarehouseLocation = 'Sydney'}
   find { SydneyWarehouseStockReport[] }
   call MyPostgresService::saveWarehouseReport
}
```

These queries are already defined, so we're capturing analytics for them - check it out in the Endpoints page

## Exposing the db data from a REST API
Finally, let's publish the normalized data out of our database on a REST API:

```taxi
@HttpOperation(method = "GET", url = "/api/q/warehouseReport")
query GetWarehouseReport {
   find { DbStandardWarehouseReport[] }
}
```

This is already set up, so you can curl this now:

```bash
curl -X GET "http://localhost:9022/api/q/warehouseReport"
```
