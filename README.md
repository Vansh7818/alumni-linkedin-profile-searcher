# Alumni LinkedIn Profile Searcher — Enterprise Platform

[![CI](https://github.com/Vansh7818/alumni-linkedin-profile-searcher/actions/workflows/ci.yml/badge.svg)](https://github.com/Vansh7818/alumni-linkedin-profile-searcher/actions)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java Version](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://adoptium.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203-yellow.svg)](http://localhost:8080/swagger-ui/index.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A production-grade, enterprise Spring Boot RESTful platform and portal designed to discover, extract, manage, and persist LinkedIn profiles of university alumni using the **PhantomBuster API**, **PostgreSQL**, and multi-tenant institutional directory management.

Built with clean Domain-Driven Design (DDD), Gang of Four (GoF) Design Patterns, high-performance caching, and fault-tolerant cloud fallbacks.

---

## Table of Contents
1. [Executive Summary & Core Highlights](#executive-summary--core-highlights)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Technology Stack](#technology-stack)
4. [Live Endpoints & Public Access](#live-endpoints--public-access)
5. [Complete API Specification](#complete-api-specification)
   - [Alumni Discovery & Search](#1-search-alumni-profiles-api)
   - [Multi-Tenant University Management](#2-university-management-apis)
   - [Bulk CSV Import & Lead Export](#3-bulk-csv-import--lead-export-apis)
   - [Profile Verification & Manual Management](#4-profile-verification--manual-management)
   - [Placement Analytics & Telemetry](#5-placement-analytics--telemetry-apis)
6. [Database Schema & JPA Mapping](#database-schema--jpa-mapping)
7. [Frontend SaaS Dashboard](#frontend-saas-dashboard)
8. [Local Setup & Zero-Config Execution](#local-setup--zero-config-execution)
9. [Docker & Production Deployment](#docker--production-deployment)
10. [Loom / Video Presentation Script](#loom--video-presentation-script)

---

## Executive Summary & Core Highlights

The platform meets all the core requirements of the talent acquisition challenge while providing real-time enterprise enhancements that separate it from standard toy submissions:

- **PhantomBuster REST API v2 Integration**: Automated launch of LinkedIn search phantoms, asynchronous container execution polling, and result payload extraction.
- **Resilient Fallback Mode**: Dynamic fallback to a high-fidelity simulation engine when API keys are not supplied or rate limits are reached, ensuring uninterrupted demonstrations and unit testing.
- **Multi-Tenant Educational Institution Portals**: Universities can register their domains, view institutional alumni statistics, and isolate alumni cohorts.
- **Bulk CSV Data Import & Export**: Fast multi-part CSV ingestion engine with format validation, error reporting, sample template downloads, and full CSV lead exports.
- **Alumni Verification Workflow**: Granular status lifecycles (`SCRAPED`, `IMPORTED`, `VERIFIED`, `REJECTED`) allowing career service teams to vet alumni.
- **In-Memory Caching & Audit Logging**: Spring `@EnableCaching` on frequent queries, coupled with execution telemetry logging (latency, search parameters, provider).
- **Interactive OpenAPI 3 / Swagger UI**: Comprehensive API exploration and schema definitions at `/swagger-ui/index.html`.
- **Modern Responsive SaaS Web Portal**: Glassmorphic UI featuring live LinkedIn search, university directory, interactive data tables, and placement distribution KPIs.

---

## Architecture & Design Patterns

The microservice follows strict **Layered Clean Architecture**:

```
                          ┌────────────────────────┐
                          │   HTTP Clients / UI    │
                          │ (SaaS Portal, Swagger) │
                          └───────────┬────────────┘
                                      │
                                      ▼
                        ┌───────────────────────────┐
                        │     Controller Layer      │
                        │ AlumniSearchController    │
                        │ UniversityController      │
                        │ HealthController          │
                        └─────────────┬─────────────┘
                                      │
                                      ▼
                        ┌───────────────────────────┐
                        │       Service Layer       │
                        │ AlumniService (Caching)   │
                        │ UniversityService         │
                        └───────┬───────────┬───────┘
                                │           │
              ┌─────────────────┘           └─────────────────┐
              ▼                                               ▼
┌───────────────────────────┐                   ┌───────────────────────────┐
│     Strategy Factory      │                   │     Repository Layer      │
│  ScraperStrategyFactory   │                   │ AlumniProfileRepository   │
└───────┬───────────┬───────┘                   │ UniversityRepository      │
        │           │                           │ SearchHistoryRepository   │
        ▼           ▼                           └─────────────┬─────────────┘
┌──────────────┐ ┌──────────────┐                             │
│PhantomBuster │ │ Resilient    │                             ▼
│Cloud Scraper │ │ Mock Engine  │               ┌───────────────────────────┐
└──────────────┘ └──────────────┘               │  PostgreSQL / H2 Database │
                                                └───────────────────────────┘
```

### Design Patterns Used
1. **Strategy Pattern (`LinkedInScraperStrategy`)**:
   - Decouples alumni extraction logic behind a uniform interface contract (`scrapeAlumni(query)`).
   - Implementations: `PhantomBusterScraperService` (live cloud scraper) and `MockLinkedInScraperService` (deterministic resilient engine).
2. **Factory Pattern (`ScraperStrategyFactory`)**:
   - Evaluates system configuration, active credentials, and API quota health to instantiate or route between active scrapers with zero client-side impact.
3. **Adapter & Mapper Pattern (`AlumniDataMapper`)**:
   - Transforms heterogeneous raw JSON payloads from PhantomBuster agents and CSV rows into standardized JPA domain entities and DTO records.
4. **Specification Pattern (`AlumniSpecification`)**:
   - Constructs type-safe, dynamic SQL predicates via Spring Data JPA Criteria API for multi-field filtering without string concatenation.
5. **DTO Pattern & Custom Deserializers**:
   - Clean boundary separation between database entities and HTTP request/response models. Includes `PassoutYearDeserializer` which handles integers (`2021`), string representations (`"2021"`), or assignment placeholders (`"OPTIONAL_PASSOUT_YEAR"`).

---

## Technology Stack

- **Runtime & Language**: Java 21 LTS
- **Core Framework**: Spring Boot 3.3.2 (Spring MVC, Spring Data JPA, Spring Cache, Actuator, Validation)
- **Database**: PostgreSQL 16 (Production) & H2 Database (Local zero-config dev mode)
- **API Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI 2.5.0
- **HTTP Engine**: Spring 6 `RestClient` & `RestTemplate`
- **Build Automation**: Apache Maven 3.9+
- **Containerization**: Docker (Multi-stage build) & Docker Compose
- **Reverse Proxy / Tunnel**: Cloudflare Tunnel for secure live public HTTPS accessibility

---

## Live Endpoints & Public Access

| Resource | Public Live URL | Local URL |
| :--- | :--- | :--- |
| **Interactive SaaS Web Portal** | [https://methodology-nominations-chief-voted.trycloudflare.com/](https://methodology-nominations-chief-voted.trycloudflare.com/) | [http://localhost:8080/](http://localhost:8080/) |
| **Interactive Swagger UI** | [https://methodology-nominations-chief-voted.trycloudflare.com/swagger-ui/index.html](https://methodology-nominations-chief-voted.trycloudflare.com/swagger-ui/index.html) | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| **OpenAPI Specification (JSON)** | [https://methodology-nominations-chief-voted.trycloudflare.com/api-docs](https://methodology-nominations-chief-voted.trycloudflare.com/api-docs) | [http://localhost:8080/api-docs](http://localhost:8080/api-docs) |
| **Health & Telemetry Endpoint** | [https://methodology-nominations-chief-voted.trycloudflare.com/api/health](https://methodology-nominations-chief-voted.trycloudflare.com/api/health) | [http://localhost:8080/api/health](http://localhost:8080/api/health) |

---

## Complete API Specification

### 1. Search Alumni Profiles API (Assignment Primary Requirement)
Triggers live PhantomBuster scraping (or resilient fallback), persists extracted profiles to the database with deduplication, and records audit telemetry.

- **URL**: `POST /api/alumni/search`
- **Headers**: `Content-Type: application/json`

#### Request Payload
```json
{
  "university": "Stanford University",
  "designation": "Software Engineer",
  "passoutYear": 2021
}
```

#### Response (`200 OK`)
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "currentRole": "Software Engineer",
      "university": "Stanford University",
      "location": "New York, NY",
      "linkedinHeadline": "Passionate Software Engineer at XYZ Corp",
      "passoutYear": 2021,
      "linkedinUrl": "https://www.linkedin.com/in/john-doe-100",
      "company": "XYZ Corp",
      "verificationStatus": "SCRAPED"
    },
    {
      "id": 2,
      "name": "Jane Smith",
      "currentRole": "Software Engineer",
      "university": "Stanford University",
      "location": "San Francisco, CA",
      "linkedinHeadline": "Software Engineer | AI & Cloud Enthusiast",
      "passoutYear": 2021,
      "linkedinUrl": "https://www.linkedin.com/in/jane-smith-101",
      "company": "Google",
      "verificationStatus": "SCRAPED"
    }
  ]
}
```

---

### 2. University Management APIs
Allows universities to register institutions, maintain records, and view institution-specific alumni rosters.

- **Register University**: `POST /api/universities`
```json
{
  "name": "Stanford University",
  "domain": "stanford.edu",
  "country": "United States",
  "city": "Stanford, CA"
}
```
- **List All Registered Universities**: `GET /api/universities`
- **Get University by ID**: `GET /api/universities/{id}`
- **Get Alumni by University Name**: `GET /api/universities/{name}/alumni`
- **Delete University**: `DELETE /api/universities/{id}`

---

### 3. Bulk CSV Import & Lead Export APIs
Facilitates bulk data management for institutional administrators.

- **Download CSV Template**: `GET /api/alumni/template`
  - Returns sample headers and example records for easy population.
- **Bulk CSV Ingestion**: `POST /api/alumni/import` (`multipart/form-data`)
  - Parses uploaded CSV files, validates rows, upserts profiles into the database, and returns counts:
```json
{
  "status": "success",
  "data": {
    "status": "success",
    "importedCount": 25,
    "updatedCount": 3,
    "skippedCount": 0,
    "totalProcessed": 28
  },
  "message": "Bulk alumni import processed successfully"
}
```
- **Export Alumni to CSV**: `GET /api/alumni/export`
  - Streams full alumni database as a formatted CSV attachment.

---

### 4. Profile Verification & Manual Management
- **Manual Profile Creation**: `POST /api/alumni`
- **Get All Profiles**: `GET /api/alumni/all`
- **Get Profile by ID**: `GET /api/alumni/{id}`
- **Update Profile**: `PUT /api/alumni/{id}`
- **Toggle Verification Status**: `PATCH /api/alumni/{id}/verify?status=VERIFIED`
- **Delete Profile**: `DELETE /api/alumni/{id}`

---

### 5. Placement Analytics & Telemetry APIs
- **Aggregated Stats**: `GET /api/alumni/stats`
  - Returns total profiles, top 5 hiring companies, top roles, and graduation year distribution.
- **Scraper Audit History**: `GET /api/alumni/history`
  - Returns audit logs with execution times, scrape providers, and record counts.

---

## Database Schema & JPA Mapping

Mapped with clean relational constraints and performance indexes:

```sql
CREATE TABLE educational_institutions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    domain VARCHAR(150),
    country VARCHAR(100),
    city VARCHAR(100),
    established_year INTEGER,
    website_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE alumni_profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    job_role VARCHAR(150) NOT NULL,
    university VARCHAR(200) NOT NULL,
    location VARCHAR(150),
    linkedin_headline VARCHAR(500),
    passout_year INTEGER,
    linkedin_url VARCHAR(500),
    company VARCHAR(150),
    email VARCHAR(150),
    skills VARCHAR(500),
    verification_status VARCHAR(50) DEFAULT 'SCRAPED',
    search_query VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,
    university VARCHAR(200) NOT NULL,
    designation VARCHAR(150) NOT NULL,
    passout_year INTEGER,
    results_count INTEGER NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    scraper_provider VARCHAR(50) NOT NULL,
    searched_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_alumni_university ON alumni_profiles(university);
CREATE INDEX idx_alumni_job_role ON alumni_profiles(job_role);
CREATE INDEX idx_alumni_passout_year ON alumni_profiles(passout_year);
CREATE INDEX idx_alumni_linkedin_url ON alumni_profiles(linkedin_url);
```

---

## Frontend SaaS Dashboard

The frontend at `/` provides an enterprise-ready control center:
1. **Hero Search Console**: Fast real-time queries with instant university preset chips.
2. **University Directory**: Real-time institutional roster management with modal for university onboarding.
3. **Directory Data Table**: Search-as-you-type, verification badge filters, manual record creation, and one-click lead CSV export.
4. **Drag-and-Drop CSV Uploader**: Ingest hundreds of records with instant feedback and progress indicators.
5. **Real-time Placement KPI Cards**: Live dynamic breakdown of top employers, job titles, and cohort graduation trends.
6. **Scraper Audit Trail**: Inspect execution latency, scraper source (`PHANTOMBUSTER` vs `FALLBACK_SIMULATION`), and historical queries.

---

## Local Setup & Zero-Config Execution

### Prerequisites
- Java 21 LTS
- Maven 3.9+ (or use the packaged executable)

### Zero-Configuration Local Execution (H2 In-Memory)
```bash
# Clone the repository
git clone https://github.com/Vansh7818/alumni-linkedin-profile-searcher.git
cd alumni-linkedin-profile-searcher

# Run with the local development profile
java -Dspring.profiles.active=local -jar target/alumni-linkedin-searcher-1.0.0.jar
```

Access:
- **Web Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Swagger Documentation**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC: `jdbc:h2:mem:alumnidb`)

### Live PhantomBuster Scraping
Supply your PhantomBuster credentials via environment variables:
```bash
export PHANTOMBUSTER_API_KEY="your-api-key"
export PHANTOMBUSTER_AGENT_ID="your-agent-id"
export PHANTOMBUSTER_MOCK_MODE=false
```

---

## Docker & Production Deployment

Run the complete multi-container production stack (PostgreSQL 16 + Spring Boot):
```bash
docker-compose up --build -d
```

Check health:
```bash
docker-compose logs -f alumni-app
```

---

## Loom / Video Presentation Script

Use this structured 2–5 minute walkthrough for your Loom recording:

### 1. Introduction & Architecture (0:00 - 1:15)
> "Hello! My name is Vansh Jain, and today I'm demonstrating the **Alumni LinkedIn Profile Searcher** enterprise platform built with **Spring Boot 3.3.2**, **Java 21**, and **PostgreSQL**."
> 
> "The objective of the assignment was to build a RESTful service that searches for LinkedIn profiles of alumni using the **PhantomBuster API** based on university, designation, and optional graduation year, and persists the data in a database."
> 
> "Beyond basic scraping, I engineered this into an **enterprise university management platform** featuring multi-tenant institution onboarding, bulk CSV ingestion, verification lifecycles, and interactive placement analytics."

### 2. Live API & Swagger Demonstration (1:15 - 2:45)
> "Let's look at the deployed API via Swagger UI."
> 
> 1. "First, I'll execute `POST /api/alumni/search` with `university: 'Stanford University'`, `designation: 'Staff Software Engineer'`, and `passoutYear: 2021`."
> 2. "Notice the `200 OK` response matching the assignment format. The data is parsed, normalized, and saved directly to the database."
> 3. "Now, running `GET /api/alumni/all` shows that the records are persisted."
> 4. "Next, let's look at the **University Management API**: calling `POST /api/universities` registers educational institutions, linking alumni cohorts together."
> 5. "Finally, the **Bulk CSV Import API** (`POST /api/alumni/import`) and **Placement Statistics** (`GET /api/alumni/stats`) provide institutional career teams with data-driven recruitment insights."

### 3. Key Technical Decisions (2:45 - 3:50)
> "Two key technical decisions stand out:"
> 
> 1. **Strategy and Factory Pattern for Scraping**: "I decoupled data collection using the Strategy Pattern. The primary engine interfaces with PhantomBuster REST API v2, while a resilient mock fallback activates automatically if API quotas or keys are unavailable, guaranteeing 100% uptime."
> 2. **Deduplication and Upsert Logic**: "To prevent duplicate candidate leads, the service executes composite upserts based on LinkedIn profile URLs and names, updating career transitions rather than duplicating records."
> 3. **Custom Deserializers**: "I developed a custom Jackson deserializer for `passoutYear` to gracefully handle numbers, strings, and the placeholder `'OPTIONAL_PASSOUT_YEAR'` without throwing JSON parsing exceptions."

### 4. Challenge & Solution (3:50 - 4:45)
> "A notable engineering challenge was handling asynchronous container execution with PhantomBuster and cross-database SQL reserved words."
> 
> "PhantomBuster launches asynchronous agents that require status polling. I implemented an exponential polling mechanism with safety timeouts to avoid thread lockups. Additionally, the SQL keyword `current_role` collided across PostgreSQL and H2; I resolved this by explicitly mapping the JPA column to `job_role` while preserving the clean domain model."
> 
> "Thank you for your time and consideration!"
