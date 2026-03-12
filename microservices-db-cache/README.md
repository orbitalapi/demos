## Overview

This demo shows how Orbital joins data across multiple microservices and a database using [TaxiQL](https://orbitalhq.com/docs/querying/writing-queries) — without writing any glue code or maintaining resolvers.

The core idea: each field in your query is a [semantic type](https://taxilang.org/docs/language/semantic-types). Orbital understands which services expose and consume those types, and automatically orchestrates the calls needed to satisfy your query. Adding a field to a query is all it takes to pull in data from another service.

By the end of this demo, you'll have a live HTTP endpoint that returns account details, balances, transactions, and spending insights in a single call — assembled from four different data sources.

## Key Services

```schemaDiagram
{
  "members" : {
    "com.lunarbank.insights.InsightsService": {},
    "com.lunarbank.account.AccountService": {},
    "com.lunarbank.account.CustomerService": {},
    "com.lunarbank.transactions.TransactionService": {}
  }
}
```

The demo uses:
* A database with [Customer](/catalog/com.lunarbank.account.Customer) and [Account](/catalog/com.lunarbank.account.Account) tables
* A [Transactions REST API](/services/com.lunarbank.transactions.TransactionService) — for fetching [transactions](/catalog/com.lunarbank.transactions.Transaction) and [account balances](/services/com.lunarbank.transactions.TransactionService/getAccountBalance)
* An [Insights API](/services/com.lunarbank.insights.InsightsService) — for richer customer spending data

> [!NOTE]
> This demo deploys Docker containers with a Postgres database, a Hazelcast cache, and an HTTP server,
> powered by [Nebula](https://nebula.orbitalhq.com). Check the [stubs](/stubs) page to confirm everything
> is running. For setup help, see the [Nebula docs](https://orbitalhq.com/docs/testing/stubbing-services)
> or the [`stack.nebula.kts`](/projects/com.lunarbank:insights-portal:0.1.0/source?selectedFile=orbital%2Fnebula%2Fstack.nebula.kts) source.

## Building the query

The steps below build up progressively.

> [!NOTE]
> **As you run each query, open the Profiler tab in the results panel** — it shows exactly which services were called, in what order, and why. This is the best way to see Orbital's query planning in action.

---

**1. Fetch all accounts**

A simple database read to start:

```taxiql
find { Account[] }
```

---

**2. Fetch a single account**

Criteria are expressed as type constraints rather than query parameters. Orbital resolves which service or table exposes `Account` and applies the filter accordingly — there's no need to know the underlying endpoint.

For more on querying with constraints, see the [Taxi Playground](https://playground.taxilang.org/examples/querying-with-criteria).

```taxiql
find { Account(AccountId == 'ACC-1') }
```

---

**3. Add customer details**

This is where Orbital's type-driven resolution starts to shine. By adding `CustomerId`, `FirstName`, and `LastName` to the output shape, Orbital sees that those types aren't available on `Account` directly — and automatically looks up which service can provide them, calling it with the `AccountId` it already has.

You don't configure this join anywhere. Orbital infers it from the type signatures in your schema.

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId : AccountId
    customer: {
        id: CustomerId
        // An expression combining first and last name
        name : FirstName + ' ' + LastName
    }
}
```

---

**4. Add account balance**

Adding `AccountBalance` to the query causes Orbital to call the [Transactions API](/services/com.lunarbank.transactions.TransactionService/getAccountBalance), passing the `AccountId` it already resolved. The nested `as { ... }` block selects just the fields we want from the response.

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId : AccountId
    customer: {
        id: CustomerId
        name : FirstName + ' ' + LastName
    }
    balance: AccountBalance as {
        balance,
        limit
    }
}
```

---

**5. Add transactions**

Adding `Transaction[]` triggers a call to the transactions list endpoint, again using the `AccountId` already in scope. Orbital handles the sequencing — it knows this call depends on data from a previous step.

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId : AccountId
    customer: {
        id: CustomerId
        name : FirstName + ' ' + LastName
    }
    balance: AccountBalance as {
        balance,
        limit
    }
    transactions : Transaction[]
}
```

---

**6. Add spending insights and reshape the response**

Finally, adding `SpendingInsights` triggers a call to the [Insights API](/services/com.lunarbank.insights.InsightsService). The full query now joins data from four sources — two REST APIs, one database, and one insights service — with Orbital handling all the orchestration.

The `Transaction[] as { ... }[]` syntax reshapes each transaction in the list, keeping only the fields we need.

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId: AccountId
    customerName: FirstName + ' ' + LastName
    customerId : CustomerId
    balance: Balance
    transactions: Transaction[] as {
        amount: Amount
        description: Description
        timestamp: TransactionTimestamp
    }[]
    insights: SpendingInsights
}
```

Check the Profiler tab here — you'll see the full call graph, including which calls could be parallelised and which had to be sequenced.

---

## Publishing as an HTTP endpoint

Rather than running this query ad-hoc, we can save it as a named, parameterised HTTP endpoint. The `accountId` path variable is typed as `AccountId`, so Orbital knows how to thread it through the query plan.

The endpoint below is already saved to the schema — view it on the [endpoints page](/endpoints/PortalQuery).

```taxi
@HttpOperation(method = "GET", url = "/api/q/spendingInsights/{accountId}")
query PortalQuery(@PathVariable("accountId") accountId: AccountId) {
   find { Account(AccountId == accountId) } as {
       accountId: AccountId
       balance: Balance
       transactions: Transaction[]
       insights: SpendingInsights
   }
}
```

```bash
curl -X GET "http://localhost:9022/api/q/spendingInsights/ACC-2" | jq
```

## Adding a cache

The same query is available with Hazelcast caching enabled. The `@Cache` annotation is all that's needed — Orbital automatically caches the results of individual operation calls, not just the final response. This means cache hits are applied at the service-call level, even when queries are composed differently.

Monitor cache behaviour from the [CachingPortalQuery endpoint page](/endpoints/CachingPortalQuery).

```taxi
@HttpOperation(method = "GET", url = "/api/q/spendingInsightsCache/{accountId}")
@Cache(connection = "myHazelcast")
query CachingPortalQuery(@PathVariable("accountId") accountId: AccountId) {
   find { Account(AccountId == accountId) } as {
       accountId: AccountId
       balance: Balance
       transactions: Transaction[]
       insights: SpendingInsights
   }
}
```

```bash
curl -X GET "http://localhost:9022/api/q/spendingInsightsCache/ACC-2" | jq
```

## Video walkthrough

For a guided tour of this demo:

[![Youtube walkthrough](https://img.youtube.com/vi/URrOQZ6MVpg/0.jpg)](https://www.youtube.com/watch?v=URrOQZ6MVpg)
