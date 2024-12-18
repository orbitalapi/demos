import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*

stack {
   s3 {
      bucket("london-warehouse") {
         file(
            "london-warehouse.csv",
            """SKU,Stock_Level,Unit_Price,Bin_Location,Reorder_Level,Min_Order_Qty,Last_Updated,Last_Received
THRUST-001,145,299.99,A-101,100,50,15/12/2024 14:32:15,10/12/2024
SHIELD-002,78,899.99,B-205,80,25,15/12/2024 14:32:15,08/12/2024
NAV-003,25,1499.99,C-312,30,10,15/12/2024 14:32:15,01/12/2024
FUEL-004,230,199.99,A-102,150,100,15/12/2024 14:32:15,14/12/2024
RADAR-005,56,749.99,B-103,60,20,15/12/2024 14:32:15,05/12/2024
DOCK-006,12,2999.99,D-401,15,5,15/12/2024 14:32:15,20/11/2024
COMM-007,89,459.99,C-201,75,30,15/12/2024 14:32:15,12/12/2024
LIFE-008,167,399.99,A-103,120,40,15/12/2024 14:32:15,13/12/2024
TOOL-009,445,89.99,B-301,400,200,15/12/2024 14:32:15,15/12/2024
SPARE-010,34,1299.99,D-102,40,15,15/12/2024 14:32:15,07/12/2024
"""
         )
      }
      bucket("sydney-warehouse") {
         file(
            "sydney-warehouse.csv", """itemId,count,price,location,reorderPoint,minOrder,timestamp,lastDelivery
1001,132,305.99,Z-101,95,45,12-15-2024 02:32:15 PM,12-10-2024
1002,82,915.99,Y-205,75,20,12-15-2024 02:32:15 PM,12-08-2024
1003,28,1525.99,X-312,35,12,12-15-2024 02:32:15 PM,12-01-2024
1004,225,204.99,Z-102,140,95,12-15-2024 02:32:15 PM,12-14-2024
1005,61,765.99,Y-103,65,25,12-15-2024 02:32:15 PM,12-05-2024
1006,15,3050.99,W-401,18,8,12-15-2024 02:32:15 PM,11-20-2024
1007,93,469.99,X-201,70,25,12-15-2024 02:32:15 PM,12-12-2024
1008,158,408.99,Z-103,110,35,12-15-2024 02:32:15 PM,12-13-2024
1009,432,92.99,Y-301,380,180,12-15-2024 02:32:15 PM,12-15-2024
1010,39,1325.99,W-102,45,18,12-15-2024 02:32:15 PM,12-07-2024
"""
         )
      }
   }


   postgres(databaseName = "warehouse-reports") { }

   http {
      val sydneyItemToSkuMap = mapOf(
         1001 to "THRUST-001",
         1002 to "SHIELD-002",
         1003 to "NAV-003",
         1004 to "FUEL-004",
         1005 to "RADAR-005",
         1006 to "DOCK-006",
         1007 to "COMM-007",
         1008 to "LIFE-008",
         1009 to "TOOL-009",
         1010 to "SPARE-010"
      )


      val mapper = jacksonObjectMapper()
      get("/skuLookup/{itemId}") { call ->
         val itemId = call.parameters["itemId"]!!.toInt()
         call.respondText(
            mapper.writerWithDefaultPrettyPrinter()
               .writeValueAsString(mapOf("itemId" to itemId, "sku" to sydneyItemToSkuMap[itemId]!!)),
            ContentType.parse("application/json")
         )
      }

      data class ProductDetails(
         val productName: String,
         val category: String,
         val manufacturer: String
      )

      val skuProductDetails = mapOf(
         "THRUST-001" to ProductDetails(
            productName = "Quantum Thruster X1", category = "Propulsion",
            manufacturer = "PropulseTech"
         ),
         "SHIELD-002" to ProductDetails(
            productName = "Nebula Shield Generator",
            category = "Defense",
            manufacturer = "ShieldCorp"
         ),
         "NAV-003" to ProductDetails(
            productName = "StarPath Navigation System",
            category = "Navigation",
            manufacturer = "NaviSpace"
         ),
         "FUEL-004" to ProductDetails(
            productName = "Plasma Fuel Cell",
            category = "Power",
            manufacturer = "EnergyTech"
         ),
         "RADAR-005" to ProductDetails(
            productName = "HyperScan Radar Array",
            category = "Sensors",
            manufacturer = "ScanTech"
         ),

         "DOCK-006" to ProductDetails(
            productName = "Universal Docking Module",
            category = "Infrastructure",
            manufacturer = "DockWorks"
         ),
         "COMM-007" to ProductDetails(
            productName = "DeepSpace Communicator",
            category = "Communications",
            manufacturer = "CommSys"
         ),
         "LIFE-008" to ProductDetails(
            productName = "Bio-Environmental Unit",
            category = "LifeSupport",
            manufacturer = "BioTech"
         ),
         "TOOL-009" to ProductDetails(
            productName = "Zero-G Tool Kit",
            category = "Maintenance",
            manufacturer = "SpaceTools"
         ),
         "SPARE-010" to ProductDetails(
            productName = "Emergency Repair Module",
            category = "Safety",
            manufacturer = "RepairTech"
         )
      )
      get("/products/{sku}") { call ->
         val sku = call.parameters["sku"]!!
         call.respondText(
            mapper.writerWithDefaultPrettyPrinter()
               .writeValueAsString(skuProductDetails[sku]!!),
            ContentType.parse("application/json")
         )
      }
   }

}
