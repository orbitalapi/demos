import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.*

enum class TransactionCategory {
   FOOD_AND_DINING,    // Coffee shops, restaurants, grocery stores
   TRANSPORTATION,     // Gas, public transport
   RETAIL,            // Online shopping, bookstores
   ENTERTAINMENT,      // Movie theater
   UTILITIES,         // Utility bills
   HEALTH             // Pharmacy

}


stack {

   postgres {
      table(
         "accounts", """
         CREATE TABLE accounts (
             account_id VARCHAR PRIMARY KEY,
             customer_id VARCHAR NOT NULL,
             account_number VARCHAR(20) NOT NULL,
             daily_limit DECIMAL(15, 2) NOT NULL
         )
         """,
         data = listOf(
            mapOf(
               "account_id" to "ACC-1",
               "customer_id" to "CUST-1",
               "account_number" to "1234567890",
               "daily_limit" to BigDecimal("1000.00"),
            ),
            mapOf(
               "account_id" to "ACC-2",
               "customer_id" to "CUST-2",
               "account_number" to "0987654321",
               "daily_limit" to BigDecimal("2000.00"),
            )
         )

      )
      table(
         "customers", """
         CREATE TABLE customers (
             customer_id VARCHAR PRIMARY KEY,
             first_name VARCHAR NOT NULL,
             last_name VARCHAR NOT NULL,
             date_of_birth DATE NOT NULL
         )
         """,
         data = listOf(
            mapOf(
               "customer_id" to "CUST-1",
               "first_name" to "Jimmy",
               "last_name" to "Schmitts",
               "date_of_birth" to LocalDate.parse("1979-05-10")
            ),
            mapOf(
               "customer_id" to "CUST-2",
               "first_name" to "Jane",
               "last_name" to "Splatts",
               "date_of_birth" to LocalDate.parse("1982-05-10")
            )
         )

      )
   }


   data class Transaction(
      val transactionId: String,
      val accountId: String,
      // Having issues getting Jackson serialization working with Instant's inside Nebula runtime
      val timestamp: String,
      val amount: BigDecimal,
      val description: String,
      val category: TransactionCategory,
   )



   fun generateTransactions(accountId: String): List<Transaction> {
      val merchantData = mapOf(
         "Coffee Shop" to TransactionCategory.FOOD_AND_DINING,
         "Grocery Store" to TransactionCategory.FOOD_AND_DINING,
         "Restaurant" to TransactionCategory.FOOD_AND_DINING,
         "Gas Station" to TransactionCategory.TRANSPORTATION,
         "Public Transport" to TransactionCategory.TRANSPORTATION,
         "Online Shopping" to TransactionCategory.RETAIL,
         "Bookstore" to TransactionCategory.RETAIL,
         "Movie Theater" to TransactionCategory.ENTERTAINMENT,
         "Utility Bill" to TransactionCategory.UTILITIES,
         "Pharmacy" to TransactionCategory.HEALTH
      )

      // Function to generate a random amount between -200 and 500
      fun randomAmount() = Random.nextDouble(-700.0, -1.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP)

      // Generate base timestamp - start from 30 days ago
      val baseTimestamp = Instant.now().minus(Period.ofDays(30))

      return (1..10).map { index ->
         val (description,category) = merchantData.entries.random()
         Transaction(
            transactionId = UUID.randomUUID().toString(),
            accountId = accountId,
            // Add random minutes to create a sequence of transactions
            timestamp = baseTimestamp.plusSeconds((index * Random.nextLong(3600, 86400))).toString(),
            amount = randomAmount(),
            description = description,
            category = category,
         )
      }.sortedByDescending { it.timestamp } // Sort by timestamp descending
   }

   val accountIds = listOf("ACC-1", "ACC-2")
   val mapper = jacksonObjectMapper()
   val transactions: Map<String, List<Transaction>> = accountIds.associateWith {
      generateTransactions(it)
   }

   data class AccountBalance(
      val accountId: String,
      val balance: BigDecimal,
      val limit: BigDecimal,
   )

   val accountBalances = accountIds.associateWith {
      AccountBalance(
         accountId = it,
         balance = Random.nextDouble(400.0, 12_000.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP),
         limit = 1_000.toBigDecimal()
      )
   }


   http {
      get("/transactions/account/{id}/balance") { call ->
         val accountId = call.parameters["id"]!!
         call.respondText(
            mapper.writeValueAsString(accountBalances[accountId]),
            ContentType.parse("application/json")
         )
      }
      get("/transactions/account/{id}") { call ->
         val accountId = call.parameters["id"]!!
         val transactionList = transactions[accountId]
         try {
            val text = mapper.writeValueAsString(transactionList)
            println(text)
            call.respondText(
               text,
               ContentType.parse("application/json")
            )
         } catch (e: Exception) {
            println("Error: ${e.message}")
            throw e
         }
      }

      get("/insights/spending/{id}") { call ->
         val accountId = call.parameters["id"]!!
         val transactionList = transactions[accountId]!!
         val mostRecent = transactionList.subList(0,3)
         val monthlySpending: List<Map<String, Any>> = transactionList.groupBy { it.category }
            .map { (category,transactions) ->
               mapOf(
                  "category" to category,
                  "totalSpent" to transactions.sumOf { it.amount }
               )
            }
         val result = mapOf(
            "monthlySpending" to monthlySpending,
            "topTransactions" to mostRecent
         )
         call.respondText(mapper.writeValueAsString(result))
      }

   }

   hazelcast {}

}
