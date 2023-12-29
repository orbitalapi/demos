## Streams

* Order Stream

```orbital_query
import com.lunarbank.demo.Order
stream {Order}
```

* Trade Stream

```orbital_query
import com.lunarbank.demo.LastTradeEvent
stream { LastTradeEvent}
```

* Enriching Orders Against Database (Traders, Desks and Instruments)


```orbital_query
import com.lunarbank.demo.TraderName
import com.lunarbank.demo.OrderType
import com.lunarbank.demo.QuantityRequested
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Order

stream {Order} as {
    id: OrderId
    qtyRequested: QuantityRequested
    orderType: OrderType
    traderName: TraderName
    deskName: DeskName,
    isin: Isin
    issuerName: InstrumentName
}[]
```

* Enriching Orders Against rest service

```orbital_query
import com.lunarbank.demo.CarbonIntensityWeightedAverage
import com.lunarbank.demo.GovernancePillarScore
import com.lunarbank.demo.SocialPillarScore
import com.lunarbank.demo.EnvironmentalPillarScore
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Order

stream {Order} as {
    orderId: OrderId
    envScore: EnvironmentalPillarScore
    socialScore: SocialPillarScore
    govScore: GovernancePillarScore
    carbonIntensity: CarbonIntensityWeightedAverage
}[]
```

* Enriching Orders Against rest service with Caching

```orbital_query
import com.lunarbank.demo.CarbonIntensityWeightedAverage
import com.lunarbank.demo.GovernancePillarScore
import com.lunarbank.demo.SocialPillarScore
import com.lunarbank.demo.EnvironmentalPillarScore
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Order

@Cache(connection = "integrationHazelcast")
stream {Order} as {
orderId: OrderId
envScore: EnvironmentalPillarScore
socialScore: SocialPillarScore
govScore: GovernancePillarScore
carbonIntensity: CarbonIntensityWeightedAverage
}[]
```

* Streaming union of orders and last trades

```orbital_query
import com.lunarbank.demo.Venue
import com.lunarbank.demo.LastTradedPrice
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Order
import com.lunarbank.demo.LastTradeEvent
import com.lunarbank.demo.TimeInForce


stream {Order | LastTradeEvent} as {
    orderId: OrderId
    instrumentId: Isin
    lastTradedPrice: LastTradedPrice
    venue: Venue
    timeInForce: TimeInForce

}[]
```

* Streaming State

```orbital_query
import com.lunarbank.demo.Venue
import com.lunarbank.demo.LastTradedPrice
import com.lunarbank.demo.OrderId
import com.lunarbank.demo.Order
import com.lunarbank.demo.LastTradeEvent
import com.lunarbank.demo.TimeInForce


@Cache(connection = "integrationHazelcast")
@StateStore(connection = "integrationHazelcast")
stream {Order | LastTradeEvent} as {
    orderId: OrderId
    instrumentId: Isin
    lastTradedPrice: LastTradedPrice
    venue: Venue
    timeInForce: TimeInForce

}[]
```
