// orbital/nebula/stack.nebula.kts
// ════════════════════════════════════════════════════════════════════
//  CMS payer demo — Nebula stack
//  --------------------------------------------------------------------
//  Stands up the three back-ends the demo composes over, all locally:
//
//    • fhir-api      (HTTP)      — the EHR's FHIR R4 server. Implements
//                                  every operation in the annotated
//                                  OpenAPI spec (Patient / Encounter /
//                                  Condition / Coverage / EOB).
//    • snowflake_pg  (Postgres)  — stands in for the Snowflake claims
//                                  warehouse: member_coverage + claim,
//                                  plus the patient_encounter_summary
//                                  write-back target (Stage 2).
//    • databricks-api(HTTP)      — the model-serving endpoint Orbital
//                                  POSTs feature vectors to, matching
//                                  the contract in databricks.taxi.
//    • databricks_pg (Postgres)  — risk_scores reference table backing
//                                  the scoring endpoint.
//
//  One cohort of 5 patients is defined once at the top and shared by the
//  Postgres seeders AND the HTTP handlers, so every system agrees on the
//  same MRNs — that's the join key the whole demo turns on.
//
//  Migration note: snowflake_pg and databricks_pg are Postgres for now,
//  for fast local iteration. When you move to real Snowflake / Databricks
//  only the connection.conf entries change; the table/column contracts
//  below are the ones the .taxi @Table models already point at (in the
//  default `public` schema here — restore the CLAIMS / ANALYTICS schema
//  qualifiers when you cut over to Snowflake).
// ════════════════════════════════════════════════════════════════════

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

// ── Cohort (single source of truth) ─────────────────────────────────

data class PatientRec(
    val mrn: String, val fhirId: String,
    val given: String, val family: String,
    val gender: String, val birthDate: String
)

data class EncounterRec(
    val id: String, val mrn: String, val status: String,
    val classCode: String, val classDisplay: String,
    val cmsCode: String, val cmsCategory: String,
    val periodStart: String, val serviceProvider: String
)

data class ConditionRec(
    val id: String, val mrn: String, val encounterId: String,
    val icd10: String, val description: String,
    val clinicalStatus: String, val onset: String
)

data class CoverageRec(
    val id: String, val mrn: String, val status: String,
    val planType: String, val planName: String, val payor: String,
    val periodStart: String, val claimsLast12Months: Int
)

data class ClaimRec(
    val claimId: String, val mrn: String, val claimType: String,
    val serviceDate: String, val amount: BigDecimal, val currency: String
)

data class RiskRec(val mrn: String, val riskScore: BigDecimal, val riskBand: String)

val patients = listOf(
    PatientRec("MRN-00124", "pat-00124", "Jane", "Doe", "female", "1958-03-12"),
    PatientRec("MRN-00125", "pat-00125", "Robert", "Chen", "male", "1972-07-21"),
    PatientRec("MRN-00126", "pat-00126", "Maria", "Garcia", "female", "1990-11-03"),
    PatientRec("MRN-00127", "pat-00127", "James", "Wilson", "male", "1945-02-28"),
    PatientRec("MRN-00128", "pat-00128", "Aisha", "Patel", "female", "1983-06-15")
)

val encounters = listOf(
    EncounterRec("enc-0057", "MRN-00124", "in-progress", "EMER", "emergency",
        "183452005", "Emergency", "2024-03-15T08:23:00Z", "Metro General Hospital"),
    EncounterRec("enc-0058", "MRN-00125", "finished", "AMB", "ambulatory",
        "185349003", "Office/Outpatient Visit", "2024-02-10T14:00:00Z", "Riverside Family Practice"),
    EncounterRec("enc-0059", "MRN-00126", "finished", "AMB", "ambulatory",
        "185349003", "Office/Outpatient Visit", "2024-01-22T09:30:00Z", "Riverside Family Practice"),
    EncounterRec("enc-0060", "MRN-00127", "finished", "IMP", "inpatient encounter",
        "32485007", "Inpatient Hospital", "2024-03-01T07:00:00Z", "Metro General Hospital"),
    EncounterRec("enc-0061", "MRN-00128", "finished", "AMB", "ambulatory",
        "185349003", "Office/Outpatient Visit", "2024-03-12T11:15:00Z", "Lakeside OB-GYN")
)

val conditions = listOf(
    ConditionRec("cond-001", "MRN-00124", "enc-0057", "E11.9",
        "Type 2 diabetes mellitus without complications", "active", "2019-06-01"),
    ConditionRec("cond-002", "MRN-00124", "enc-0057", "I10",
        "Essential (primary) hypertension", "active", "2018-02-15"),
    ConditionRec("cond-003", "MRN-00125", "enc-0058", "J45.909",
        "Unspecified asthma, uncomplicated", "active", "2010-09-12"),
    ConditionRec("cond-004", "MRN-00126", "enc-0059", "E66.9",
        "Obesity, unspecified", "active", "2021-03-20"),
    ConditionRec("cond-005", "MRN-00127", "enc-0060", "I50.9",
        "Heart failure, unspecified", "active", "2022-11-05"),
    ConditionRec("cond-006", "MRN-00127", "enc-0060", "N18.3",
        "Chronic kidney disease, stage 3 (moderate)", "active", "2023-01-18"),
    ConditionRec("cond-007", "MRN-00128", "enc-0061", "O80",
        "Encounter for full-term uncomplicated delivery", "resolved", "2024-03-12")
)

val coverages = listOf(
    CoverageRec("cov-00124", "MRN-00124", "active", "PPO", "Medicare Advantage PPO", "UnitedHealthcare", "2022-01-15", 8),
    CoverageRec("cov-00125", "MRN-00125", "active", "PPO", "BCBS PPO", "Blue Cross Blue Shield", "2021-06-01", 3),
    CoverageRec("cov-00126", "MRN-00126", "active", "HMO", "Aetna HMO", "Aetna", "2023-01-01", 2),
    CoverageRec("cov-00127", "MRN-00127", "active", "HMO", "Medicare Advantage HMO", "Humana", "2020-09-01", 14),
    CoverageRec("cov-00128", "MRN-00128", "active", "PPO", "Cigna PPO", "Cigna", "2022-03-01", 5)
)

val claims = listOf(
    ClaimRec("CLM-1001", "MRN-00124", "institutional", "2024-03-15", BigDecimal("4200.00"), "USD"),
    ClaimRec("CLM-1002", "MRN-00124", "professional", "2024-03-15", BigDecimal("850.00"), "USD"),
    ClaimRec("CLM-1003", "MRN-00125", "professional", "2024-02-10", BigDecimal("220.00"), "USD"),
    ClaimRec("CLM-1004", "MRN-00126", "professional", "2024-01-22", BigDecimal("180.00"), "USD"),
    ClaimRec("CLM-1005", "MRN-00127", "institutional", "2024-03-01", BigDecimal("18500.00"), "USD"),
    ClaimRec("CLM-1006", "MRN-00127", "professional", "2024-03-01", BigDecimal("1200.00"), "USD"),
    ClaimRec("CLM-1007", "MRN-00127", "pharmacy", "2024-03-05", BigDecimal("340.00"), "USD"),
    ClaimRec("CLM-1008", "MRN-00128", "institutional", "2024-03-12", BigDecimal("6800.00"), "USD")
)

val riskScores = listOf(
    RiskRec("MRN-00124", BigDecimal("0.720"), "high"),
    RiskRec("MRN-00125", BigDecimal("0.210"), "low"),
    RiskRec("MRN-00126", BigDecimal("0.150"), "low"),
    RiskRec("MRN-00127", BigDecimal("0.880"), "high"),
    RiskRec("MRN-00128", BigDecimal("0.340"), "medium")
)

val mrnToFhirId = patients.associate { it.mrn to it.fhirId }

// ── MRN resolution ──────────────────────────────────────────────────
// The FHIR search params are tagged as PatientMrn, but real clients also
// send "Patient/{id}" references — accept both, and bare logical ids too.
fun resolveMrn(raw: String?): String? {
    if (raw == null) return null
    val tail = raw.substringAfterLast('/')      // "Patient/pat-00124" -> "pat-00124"
    patients.firstOrNull { it.mrn == raw || it.mrn == tail }?.let { return it.mrn }
    patients.firstOrNull { it.fhirId == tail }?.let { return it.mrn }
    return raw
}


// ── Meridian (legacy provider) — same cohort, re-encoded ─────────────
// Opaque member IDs, unrelated to the MRN — the MPI crosswalk is the
// only way back to it.
val meridianMemberIds = mapOf(
    "MRN-00124" to "8842-J",
    "MRN-00125" to "7193-R",
    "MRN-00126" to "5561-M",
    "MRN-00127" to "9024-J",
    "MRN-00128" to "6680-A"
)

// FHIR gender string -> ISO 5218 numeric sex code.
fun iso5218(gender: String): Int = when (gender) {
    "male" -> 1
    "female" -> 2
    else -> 0
}

// A flat, non-FHIR roster record: yyyyMMdd dates, numeric sex, no MRN.
fun meridianRecord(p: PatientRec): Map<String, Any> = mapOf(
    "memberId" to meridianMemberIds.getValue(p.mrn),
    "lastName" to p.family.uppercase(),
    "firstName" to p.given.uppercase(),
    "dateOfBirth" to p.birthDate.replace("-", ""),   // 1958-03-12 -> 19580312
    "genderCode" to iso5218(p.gender),
    "enrollmentStatus" to "A"
)

// ── FHIR resource builders ──────────────────────────────────────────

fun patientResource(p: PatientRec): Map<String, Any> = mapOf(
    "resourceType" to "Patient",
    "id" to p.fhirId,
    "identifier" to listOf(
        mapOf(
            "use" to "usual",
            "type" to mapOf(
                "coding" to listOf(
                    mapOf(
                        "system" to "http://terminology.hl7.org/CodeSystem/v2-0203",
                        "code" to "MR",
                        "display" to "Medical Record Number"
                    )
                )
            ),
            "system" to "http://hospital.example.org/mrn",
            "value" to p.mrn
        )
    ),
    "active" to true,
    "name" to listOf(mapOf("use" to "official", "family" to p.family, "given" to listOf(p.given))),
    "gender" to p.gender,
    "birthDate" to p.birthDate
)

fun encounterResource(e: EncounterRec): Map<String, Any> = mapOf(
    "resourceType" to "Encounter",
    "id" to e.id,
    "status" to e.status,
    "class" to mapOf(
        "system" to "http://terminology.hl7.org/CodeSystem/v3-ActCode",
        "code" to e.classCode,
        "display" to e.classDisplay
    ),
    "type" to listOf(
        mapOf(
            "coding" to listOf(
                mapOf(
                    "system" to "http://snomed.info/sct",
                    "code" to e.cmsCode,
                    "display" to e.cmsCategory
                )
            ),
            "text" to e.cmsCategory
        )
    ),
    "subject" to mapOf("reference" to "Patient/${mrnToFhirId[e.mrn]}"),
    "period" to mapOf("start" to e.periodStart),
    "serviceProvider" to mapOf("display" to e.serviceProvider)
)

fun conditionResource(c: ConditionRec): Map<String, Any> = mapOf(
    "resourceType" to "Condition",
    "id" to c.id,
    "clinicalStatus" to mapOf(
        "coding" to listOf(
            mapOf(
                "system" to "http://terminology.hl7.org/CodeSystem/condition-clinical",
                "code" to c.clinicalStatus
            )
        ),
        "text" to c.clinicalStatus
    ),
    "code" to mapOf(
        "coding" to listOf(
            mapOf(
                "system" to "http://hl7.org/fhir/sid/icd-10-cm",
                "code" to c.icd10,
                "display" to c.description
            )
        ),
        "text" to c.description
    ),
    "subject" to mapOf("reference" to "Patient/${mrnToFhirId[c.mrn]}"),
    "encounter" to mapOf("reference" to "Encounter/${c.encounterId}"),
    "onsetDateTime" to c.onset
)

fun coverageResource(cov: CoverageRec): Map<String, Any> = mapOf(
    "resourceType" to "Coverage",
    "id" to cov.id,
    "status" to cov.status,
    "type" to mapOf(
        "coding" to listOf(
            mapOf(
                "system" to "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                "code" to cov.planType
            )
        ),
        "text" to cov.planName
    ),
    "beneficiary" to mapOf("reference" to "Patient/${mrnToFhirId[cov.mrn]}"),
    "period" to mapOf("start" to cov.periodStart),
    "payor" to listOf(mapOf("display" to cov.payor))
)

fun eobId(c: ClaimRec): String = "eob-" + c.claimId.removePrefix("CLM-")

fun eobResource(c: ClaimRec): Map<String, Any> = mapOf(
    "resourceType" to "ExplanationOfBenefit",
    "id" to eobId(c),
    "status" to "active",
    "type" to mapOf(
        "coding" to listOf(
            mapOf(
                "system" to "http://terminology.hl7.org/CodeSystem/claim-type",
                "code" to c.claimType
            )
        )
    ),
    "use" to "claim",
    "patient" to mapOf("reference" to "Patient/${mrnToFhirId[c.mrn]}"),
    "billablePeriod" to mapOf("start" to c.serviceDate),
    "created" to "${c.serviceDate}T10:00:00Z",
    "outcome" to "complete",
    "total" to listOf(
        mapOf(
            "category" to mapOf("coding" to listOf(mapOf("code" to "submitted"))),
            "amount" to mapOf("value" to c.amount, "currency" to c.currency)
        )
    )
)

// A searchset Bundle wrapping a SINGLE resource — e.g. a lookup by a unique
// key like MRN. Pass an empty map for "no match": total 0, no entries.
// (The whole resource becomes one entry. The earlier bug was treating this
// single resource map as the collection — .size counted its fields and
// .map iterated its entries, so each field came back as its own resource.)
fun searchBundle(resource: Map<String, Any>): Map<String, Any> {
    val entries = if (resource.isEmpty()) emptyMap() else mapOf("resource" to resource)
    return mapOf(
        "resourceType" to "Bundle",
        "type" to "searchset",
        "total" to entries.size,
        "entry" to entries
    )
}

// A searchset Bundle wrapping MANY resources — genuinely 1:many per patient
// (Conditions, ExplanationOfBenefits). Each resource becomes one entry.
fun searchBundle(resources: List<Map<String, Any>>): Map<String, Any> = mapOf(
    "resourceType" to "Bundle",
    "type" to "searchset",
    "total" to resources.size,
    "entry" to resources.map { mapOf("resource" to it) }
)

fun operationOutcome(diagnostics: String): String =
    """{"resourceType":"OperationOutcome","issue":[{"severity":"error","code":"not-found","diagnostics":"$diagnostics"}]}"""

// ── Readmission risk "model" ────────────────────────────────────────
// Serves the seeded score for a known MRN; for an unknown MRN it derives
// a plausible score from the request features so the endpoint still
// behaves like a model. Matches the flat RiskScoringRequest/Response
// contract in databricks.taxi (NOT the native Databricks serving
// envelope — swap that in when you point at the real endpoint).
fun scoreReadmission(req: Map<String, Any?>): Map<String, Any> {
    val mrn = req["mrn"]?.toString()
    riskScores.firstOrNull { it.mrn == mrn }?.let {
        return mapOf("mrn" to it.mrn, "riskScore" to it.riskScore, "riskBand" to it.riskBand)
    }
    var score = 0.10
    when (req["recentEncounterClass"]?.toString()) {
        "IMP" -> score += 0.45
        "EMER" -> score += 0.35
        "AMB" -> score += 0.05
    }
    score += ((req["activeDiagnosisCount"] as? Number)?.toInt() ?: 0) * 0.07
    score += ((req["claimsLast12Months"] as? Number)?.toInt() ?: 0) * 0.02
    score = score.coerceIn(0.0, 0.99)
    val band = when {
        score < 0.30 -> "low"
        score < 0.60 -> "medium"
        else -> "high"
    }
    return mapOf(
        "mrn" to (mrn ?: "unknown"),
        "riskScore" to BigDecimal(score).setScale(3, RoundingMode.HALF_UP),
        "riskBand" to band
    )
}

// ════════════════════════════════════════════════════════════════════
//  Stack
// ════════════════════════════════════════════════════════════════════

stack {
    val mapper = jacksonObjectMapper()
    val fhirJson = ContentType.parse("application/fhir+json")
    val appJson = ContentType.parse("application/json")

    // ── snowflake_pg — claims & coverage warehouse ────────────────────
    postgres(componentName = "snowflake_pg", databaseName = "snowflake_pg") {

        table(
            "member_coverage",
            """
         CREATE TABLE member_coverage (
            mrn                   VARCHAR PRIMARY KEY,
            coverage_status       VARCHAR NOT NULL,
            plan_name             VARCHAR NOT NULL,
            payor                 VARCHAR NOT NULL,
            claims_last_12_months INT     NOT NULL
         )
         """.trimIndent(),
            data = coverages.map {
                mapOf(
                    "mrn" to it.mrn,
                    "coverage_status" to it.status,
                    "plan_name" to it.planName,
                    "payor" to it.payor,
                    "claims_last_12_months" to it.claimsLast12Months
                )
            }
        )

        table(
            "claim",
            """
         CREATE TABLE claim (
            claim_id     VARCHAR PRIMARY KEY,
            mrn          VARCHAR NOT NULL,
            claim_type   VARCHAR NOT NULL,
            service_date DATE    NOT NULL,
            amount       NUMERIC(12,2) NOT NULL,
            currency     VARCHAR NOT NULL
         )
         """.trimIndent(),
            data = claims.map {
                mapOf(
                    "claim_id" to it.claimId,
                    "mrn" to it.mrn,
                    "claim_type" to it.claimType,
                    "service_date" to LocalDate.parse(it.serviceDate),
                    "amount" to it.amount,
                    "currency" to it.currency
                )
            }
        )

        // Stage 2 write-back target — created empty; Orbital upserts into it.
        // Mirrors snowflake.taxi PatientEncounterSummary (diagnoses -> JSONB).
        table(
            "patient_encounter_summary",
            """
         CREATE TABLE patient_encounter_summary (
            mrn                   VARCHAR PRIMARY KEY,
            patient_name          VARCHAR,
            encounter_id          VARCHAR,
            encounter_class       VARCHAR,
            service_category      VARCHAR,
            diagnoses             JSONB,
            coverage_status       VARCHAR,
            plan_name             VARCHAR,
            claims_last_12_months INT,
            risk_score            NUMERIC(4,3),
            risk_band             VARCHAR
         )
         """.trimIndent(),
            data = emptyList()
        )
    }

    // ── databricks_pg — risk score reference table ────────────────────
    postgres(componentName = "databricks_pg", databaseName = "databricks_pg") {
        table(
            "risk_scores",
            """
         CREATE TABLE risk_scores (
            mrn        VARCHAR PRIMARY KEY,
            risk_score NUMERIC(4,3) NOT NULL,
            risk_band  VARCHAR NOT NULL
         )
         """.trimIndent(),
            data = riskScores.map {
                mapOf("mrn" to it.mrn, "risk_score" to it.riskScore, "risk_band" to it.riskBand)
            }
        )
    }

    // ── fhir-api — the EHR's FHIR R4 server ───────────────────────────
    http(componentName = "fhir-api") {


        get("/Patients/all") { call ->
            val result = patients.map { patientResource(it) }
            call.respondText(mapper.writeValueAsString(result), fhirJson)
        }
        // ---- Patient ----
        get("/Patient") { call ->
            val identifier = call.request.queryParameters["identifier"]
            val family = call.request.queryParameters["family"]
            val given = call.request.queryParameters["given"]
            val birthdate = call.request.queryParameters["birthdate"]

            var matches = patients
            if (identifier != null) {
                val mrn = resolveMrn(identifier)
                matches = matches.filter { it.mrn == mrn }
            }
            if (family != null) matches = matches.filter { it.family.equals(family, ignoreCase = true) }
            if (given != null) matches = matches.filter { it.given.equals(given, ignoreCase = true) }
            if (birthdate != null) matches = matches.filter { it.birthDate == birthdate }


            call.respondText(
                mapper.writeValueAsString(searchBundle(matches.map { patientResource(it) }.firstOrNull() ?: emptyMap())),
                fhirJson
            )
        }

        get("/Patient/{id}") { call ->
            val id = call.parameters["id"]!!
            val p = patients.firstOrNull { it.fhirId == id || it.mrn == id }
            if (p == null) {
                call.respondText(operationOutcome("Patient $id not found"), fhirJson, HttpStatusCode.NotFound)
            } else {
                call.respondText(mapper.writeValueAsString(patientResource(p)), fhirJson)
            }
        }

        // ---- Encounter ----
        get("/Encounter") { call ->
            val patient = call.request.queryParameters["patient"]
            val status = call.request.queryParameters["status"]
            val klass = call.request.queryParameters["class"]

            var matches = encounters
            if (patient != null) {
                val mrn = resolveMrn(patient)
                matches = matches.filter { it.mrn == mrn }
            }
            if (status != null) matches = matches.filter { it.status == status }
            if (klass != null) matches = matches.filter { it.classCode.equals(klass, ignoreCase = true) }

            call.respondText(
                mapper.writeValueAsString(searchBundle(matches.map { encounterResource(it) }.firstOrNull() ?: emptyMap())),
                fhirJson
            )
        }

        get("/Encounter/{id}") { call ->
            val id = call.parameters["id"]!!
            val e = encounters.firstOrNull { it.id == id }
            if (e == null) {
                call.respondText(operationOutcome("Encounter $id not found"), fhirJson, HttpStatusCode.NotFound)
            } else {
                call.respondText(mapper.writeValueAsString(encounterResource(e)), fhirJson)
            }
        }

        // ---- Condition ----
        get("/Condition") { call ->
            val patient = call.request.queryParameters["patient"]
            val encounter = call.request.queryParameters["encounter"]
            val clinicalStatus = call.request.queryParameters["clinical-status"]

            var matches = conditions
            if (patient != null) {
                val mrn = resolveMrn(patient)
                matches = matches.filter { it.mrn == mrn }
            }
            if (encounter != null) matches = matches.filter { it.encounterId == encounter }
            if (clinicalStatus != null) matches = matches.filter { it.clinicalStatus == clinicalStatus }

            call.respondText(
                mapper.writeValueAsString(searchBundle(matches.map { conditionResource(it) })),
                fhirJson
            )
        }

        get("/Condition/{id}") { call ->
            val id = call.parameters["id"]!!
            val c = conditions.firstOrNull { it.id == id }
            if (c == null) {
                call.respondText(operationOutcome("Condition $id not found"), fhirJson, HttpStatusCode.NotFound)
            } else {
                call.respondText(mapper.writeValueAsString(conditionResource(c)), fhirJson)
            }
        }

        // ---- Coverage ----
        get("/Coverage") { call ->
            val patient = call.request.queryParameters["patient"]
                ?: call.request.queryParameters["beneficiary"]
            val status = call.request.queryParameters["status"]

            var matches = coverages
            if (patient != null) {
                val mrn = resolveMrn(patient)
                matches = matches.filter { it.mrn == mrn }
            }
            if (status != null) matches = matches.filter { it.status == status }

            call.respondText(
                mapper.writeValueAsString(searchBundle(matches.map { coverageResource(it) }.firstOrNull() ?: emptyMap())),
                fhirJson
            )
        }

        get("/Coverage/{id}") { call ->
            val id = call.parameters["id"]!!
            val cov = coverages.firstOrNull { it.id == id }
            if (cov == null) {
                call.respondText(operationOutcome("Coverage $id not found"), fhirJson, HttpStatusCode.NotFound)
            } else {
                call.respondText(mapper.writeValueAsString(coverageResource(cov)), fhirJson)
            }
        }

        // ---- ExplanationOfBenefit (derived from claims) ----
        get("/ExplanationOfBenefit") { call ->
            val patient = call.request.queryParameters["patient"]

            var matches = claims
            if (patient != null) {
                val mrn = resolveMrn(patient)
                matches = matches.filter { it.mrn == mrn }
            }

            call.respondText(
                mapper.writeValueAsString(searchBundle(matches.map { eobResource(it) })),
                fhirJson
            )
        }

        get("/ExplanationOfBenefit/{id}") { call ->
            val id = call.parameters["id"]!!
            val c = claims.firstOrNull { eobId(it) == id }
            if (c == null) {
                call.respondText(operationOutcome("ExplanationOfBenefit $id not found"), fhirJson, HttpStatusCode.NotFound)
            } else {
                call.respondText(mapper.writeValueAsString(eobResource(c)), fhirJson)
            }
        }
    }

    // ── databricks-api — model-serving endpoint ───────────────────────
    http(componentName = "databricks-api") {
        post("/serving-endpoints/readmission-risk/invocations") { call ->
            val req = mapper.readValue<Map<String, Any?>>(call.receiveText())
            call.respondText(mapper.writeValueAsString(scoreReadmission(req)), appJson)
        }
    }


    // ── meridian — legacy non-FHIR provider (Stage 3 jumping-off point) ──
    http(componentName = "meridian") {

        // Flat roster — replaces listAllPatients. Bare array, no Bundle.
        get("/v2/roster") { call ->
            call.respondText(
                mapper.writeValueAsString(patients.map { meridianRecord(it) }),
                appJson
            )
        }

        // MPI crosswalk — the opaque memberId resolves to the MRN here, and
        // only here (it can't be derived from the roster record).
        get("/v2/mpi/{memberId}") { call ->
            val memberId = call.parameters["memberId"]!!
            val mrn = meridianMemberIds.entries.firstOrNull { it.value == memberId }?.key
            if (mrn == null) {
                call.respondText(
                    """{"error":"unknown memberId","memberId":"$memberId"}""",
                    appJson, HttpStatusCode.NotFound
                )
            } else {
                call.respondText(
                    mapper.writeValueAsString(mapOf("memberId" to memberId, "mrn" to mrn)),
                    appJson
                )
            }
        }
    }
}