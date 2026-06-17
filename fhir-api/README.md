## US FHIR API Aggregation demo

This demo explores working with the US Gov FHIR API Standard.

The goal is to:
 - Read from the FHIR Hospital API, pulling:
   - Patient Details
   - Updated encounters

This needs to be enriched by calling the following services:

 - Snowflake: Fetch coverage and claims counts
 - Databricks: Fetch risk score

Then this data needs to be upserted to Snowflake, in a summary table.

```schemaDiagram
{
  "members" : {
    "snowflake.PatientSummaryStore": {},
    "databricks.ReadmissionRiskStore": {},
    "gov.us.fhir.r4.PatientService": {},
    "gov.us.fhir.r4.PatientsAllService": {}
  }
}
```

This is possible by a query:

```taxiql
// This query is partially commented out, to allow building up throughout the demo.
// Adding more elements introduces more API and Database calls

import fhir.extensions.DefaultPatientMrn
import fhir.Encounter
import fhir.Patient
import gov.us.fhir.r4.Identifier
import claims.CoverageStatus

find { Patient[] } as  (Patient, mrn: Patient::DefaultPatientMrn) -> {
   mrn: DefaultPatientMrn
   familyName : (PatientFamilyName) OfficialPatientFamilyName
   givenName: (PatientGivenName) OfficialPatientGivenName
   fullName : PatientName = concat(this.givenName, ' ', this.familyName)
//   encounterId : EncounterBundle::EncounterId
//   encounterClass : EncounterBundle::EncounterClassCode
//   serviceCategory : EncounterBundle::CmsServiceCategory
//   riskScore : PatientRiskScore::RiskScore
//   riskBand : PatientRiskScore::RiskBand
//   coverageStatus : MemberCoverage::CoverageStatus
//   planName : MemberCoverage::CoveragePlanName
//   claimsLast12Months : MemberCoverage::ClaimCount
}[] // as PatientEncounterSummary[]
// call PatientSummaryStore::saveSummary
```

## Publishing via an API

There are three endpoints published to read / trigger these queries as HTTP endpoints.

View them in the [Endpoints](/endpoints) tab:

### [PopulatePatientSummary](/endpoints/PopulatePatientSummary)
Populates the Snowflake database with summary data:

 - Reads the FHIR Patients update api, enriches against:
   - FHIR Encounters
   - Databricks Risk scores
   - Snowflake Coverage
 - Then UPSERTS to Snowflake 

### [ListPatients](/endpoints/ListPatients)
Reads the Snowflake PatientEncounterSummaryData, returning all records

### [GetPatientEncounterSummary](/endpoints/GetPatientEncounterSummary)
Reads a single Snowflake record for a given MRN.

Takes an MRN in the path as a variable

To demo, try with `MRN-00124`: [/api/q/patients/MRN-00124](http://localhost:9022/api/q/patients/MRN-00124)

## Swapping to a different data provider
 - A legacy data provider - "Meridian" is providing data over an HTTP endpoint using a non-standard format

 - Different field names (Solved via embedding semantic types)
 - Different date formats  (Solved via `@Format(...)` on the taxi type)
 - Different enum values (Solved via Enum Synonyms)
 - Different Id schemes (Solved via an extra HTTP lookup )

```taxiql
find { MeridianMember[] }
```

API is annotated with same semantic types as FHIR OpenAPI.

So, populating data store requires changing the data source only:

```taxi
// For FHIR:
find { Patient[] } as  (Patient, mrn: Patient::DefaultPatientMrn) -> {
 // ... continues
}

// For Meridian:
find { MeridianMember[] } as  (Patient, mrn: Patient::DefaultPatientMrn) -> {
 // ... continues
}
```

Full Meridian query:

```taxiql
import fhir.PatientFamilyName
// This query is partially commented out, to allow building up throughout the demo.
// Adding more elements introduces more API and Database calls

import fhir.extensions.DefaultPatientMrn
import fhir.Encounter
import fhir.Patient
import gov.us.fhir.r4.Identifier
import claims.CoverageStatus

find { MeridianMember[] } as  (Patient, mrn: Patient::DefaultPatientMrn) -> {
  mrn: DefaultPatientMrn
  familyName : ((PatientFamilyName) OfficialPatientFamilyName) 
  givenName: (PatientGivenName) OfficialPatientGivenName
  encounterId : EncounterBundle::EncounterId
  encounterClass : EncounterBundle::EncounterClassCode
  serviceCategory : EncounterBundle::CmsServiceCategory
  riskScore : PatientRiskScore::RiskScore
  riskBand : PatientRiskScore::RiskBand
  coverageStatus : MemberCoverage::CoverageStatus
  planName : MemberCoverage::CoveragePlanName
  claimsLast12Months : MemberCoverage::ClaimCount
}[] // as PatientEncounterSummary[]
//call PatientSummaryStore::saveSummary


```

## Internal notes:

 - This readme
   - README.md in root of project
   - Supports Github flavored Markdown
   - `taxiql` becomes runnable queries
   - Diagrams are embedded via custom `schemaDiagram` tag 
 - Show `connections.conf` and `services.conf` for defining connections.
 - Explain services.conf domain resolution (service discovery)
 - Powered by Nebula
   - Snowflake and Databricks are stubbed to Postgres
 - OpenAPI spec annotated with taxi types
   - Base taxonomy defined
   - Lives in the same project
   - Companion .taxi.conf file
   - Annotated into OpenAPI spec

## Modelling challenges

### Patient name

Names are complexly modelled:

```json
 {
    "resourceType": "Patient",
    "id": "pat-00124",
    "name": [
      {
        "use": "official",
        "family": "Doe",
        "given": [
          "Jane"
        ]
      }
    ],
    "gender": "female",
    "birthDate": "1958-03-12"
  }


```

 - A Patient can have many names, for different purposes (official, usual, nickname, etc..)
 - Within that name, there are many given names (So cardinality of Given name is `A..*..*`)
 - We need a single given name, and a single family name, which match the "official" use

Here's how that was modelled:

```taxi
type OfficialPatientFamilyName inherits PatientFamilyName = HumanName[].single( (HumanNameUse) -> HumanNameUse == HumanNameUse.official )::PatientFamilyName
type OfficialPatientGivenName inherits PatientGivenName =  HumanName[].single( (HumanNameUse) -> HumanNameUse == HumanNameUse.official )::PatientGivenName[].joinToString(" ")
```

### DefaultPatientMrn
Similar challenge to names, MRN's are deeply nested, there can be many.
The "correct" one is determined by the uri `  "system": "http://hospital.example.org/mrn"`

```json
{
  "identifier": [
    {
      "use": "usual",
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
            "code": "MR",
            "display": "Medical Record Number"
          }
        ]
      },
      "system": "http://hospital.example.org/mrn",
      "value": "MRN-00125"
    }
  ]
}
```

Here's how that's modelled:

```taxi
type DefaultPatientMrn inherits PatientMrn = 
    Identifier[].single((identifier:Identifier) -> identifier.system == "http://hospital.example.org/mrn")::PatientMrn
```