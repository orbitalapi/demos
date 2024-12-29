## Overview
This demo stack showcases a series of read queries joining data from microservices, and a database. 

In this demo, we'll cover:
 * Reading and joining data
 * Enabling a cache (Hazelcast)
 * Deploying our query as an HTTP endpoint
 * Parameterizing that HTTP endpoint with customer details


> [!NOTE]
> This demo deploys docker containers with a Postgres db, a Hazelcast cache, and an HTTP server.
> 
> This is powered by our test framework, [Nebula](https://nebula.orbitalhq.com), which needs to be enabled. Check out the [stubs](/stubs) page to ensure
> that everything is running properly. (If you're using our official [Docker Compose](https://start.orbitalhq.com), then you should be fine).
> 
> For more information, see our docs on [enabling Nebula](https://orbitalhq.com/docs/testing/stubbing-services), and to see the actual stubs configured
> for this project, check the source in [`stack.nebula.kts`](/projects/com.lunarbank:insights-portal:0.1.0/source?selectedFile=orbital%2Fnebula%2Fstack.nebula.kts)

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

Our demo consists of:
* A database with tables for [Customer](/catalog/com.lunarbank.account.Customer) and [Account](/catalog/com.lunarbank.account.Account)
* A [Transactions REST API](/services/com.lunarbank.transactions.TransactionService), with endpoints for fetching [a list of transactions](/catalog/com.lunarbank.transactions.Transaction), and [account balances](/services/com.lunarbank.transactions.TransactionService/getAccountBalance)
* There's also an [Insights API](/services/com.lunarbank.insights.InsightsService) which provides richer information about customer spending

> [!NOTE]
> The above links will take you to the relevant pages in Orbital's catalog.
> Take a moment to look around and explore how everything hangs together.

We'll be linking data from these data services together, to build out an API for powering a UI.

## Key queries:

**Fetch accounts (db query):**

This query simply reads all accounts from the DB -- nothing fancy here.
```taxiql
find { Account[] }
```


**Fetch a single account:**
Similary, this query fetches a single account, using the `AccountId`. Note how criteria are passed into
Taxi queries - using constraints on types. There's more examples showing querying with constraints in the [Taxi Playground](https://playground.taxilang.org/examples/querying-with-criteria)

```taxiql
find { Account(AccountId == 'ACC-1') }
```

**Fetch accounts, combined with customer details:**
This time, we'll start combining data from multiple sources.

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId : AccountId
    // We're choosing to nest customer details in a JSON object.
    // You can rework this query to flatted the id and name as top level fields, if you wish
    customer: {
        id: CustomerId
        // An expression, combining the first and last names
        name : FirstName + ' ' + LastName
    }
}
```

Add account balances:

```taxiql
find { Account(AccountId == 'ACC-1') } as {
    accountId : AccountId
    customer: {
        id: CustomerId
        name : FirstName + ' ' + LastName
    }
    balance: AccountBalance as {
        // This is using a shorthand, to select the fields from AccountBalance 
        // that we're interested in.
        balance,
        limit
    }
}
```

> [!NOTE]  
> As you're running these queries, be sure to check out the Profiler tab of the query results panel,
> to see what's actually going on.
>
> The profiler tab gives you a high level overview of the query plan,
> along with detailed tracing of the individual requests that were performed


Finally, add transcations:

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

Let's reshape the response, and add in spending insights:

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

At this point, the query is joining data from multiple API calls and database queries.
Be sure to check out the Profiler tab in the query response panel to see what's going on.

## Query as an endpoint

The below query has already been saved to the schema - check it out in the [endpoints](/endpoints/PortalQuery). It publishes
our query as an HTTP endpoint, where the account id is parameterized. It's published at `/api/q/spendingInsights/{accountId}`.

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

You can invoke this query by running:

```bash
curl -X GET "http://localhost:9022/api/q/spendingInsightsCache/ACC-2" | jq
```

## Querying with a cache
An alternative version of the query is available which makes heavy use of caching - you can monitor it's usage in it's dedicated [endpoints page](/endpoints/CachingPortalQuery)

```taxi
[[ This is the same query as the PortalQuery, but it has a cache enabled.
Operation calls are automatically cached in a Hazelcast cache, defined in connections ]]
@HttpOperation(method = "GET", url = "/api/q/spendingInsightsCache/{accountId}")
@Cache(connection = "myHazelcast")
query CachingPortalQuery(@PathVariable("accountId") accountId: AccountId) {
   // accountId is a parameter passed into the query from a PathVariable
   find { Account(AccountId == accountId) } as {
       accountId: AccountId
       balance: Balance
       transactions: Transaction[]
       insights: SpendingInsights
   }
}

```
