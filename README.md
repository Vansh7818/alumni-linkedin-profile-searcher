# Alumni LinkedIn Profile Searcher

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java Version](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://adoptium.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203-yellow.svg)](http://localhost:8080/swagger-ui/index.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A production-grade, enterprise Spring Boot RESTful microservice designed to discover, extract, and persist LinkedIn profiles of university alumni using the **PhantomBuster API** and **PostgreSQL**.

Built with modern software engineering practices, Clean Architecture, Design Patterns, and fault-tolerant fallbacks.

---

## Table of Contents
1. [Objective & Key Features](#objective--key-features)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Technology Stack](#technology-stack)
4. [API Specification](#api-specification)
5. [Database Schema & Integration](#database-schema--integration)
6. [Local Setup & Running](#local-setup--running)
7. [Docker & Containerization](#docker--containerization)
8. [Public Deployment Guide](#public-deployment-guide)
9. [Loom / Video Presentation Script](#loom--video-presentation-script)

---

## Objective & Key Features

The application allows users to search for alumni LinkedIn profiles by:
- **University Name** (Mandatory)
- **Current Designation / Role** (Mandatory)
- **Pass-out Year** (Optional)

### Enterprise Features
- **PhantomBuster Integration**: Interacts with the PhantomBuster REST API v2 to trigger LinkedIn search Phantoms, poll execution containers, and parse profile data.
- **Resilient Fallback Mode**: If PhantomBuster credentials are not provided or the API is rate-limited, the system engages a realistic mock scraper strategy, allowing continuous testing and evaluation without cost.
- **Data Persistence & Deduplication**: Upserts extracted alumni into PostgreSQL. Duplicate profiles (matched by LinkedIn URL or composite keys) update with recent data rather than producing duplicates.
- **Dynamic Multi-Criteria Search & Filtering**: Leverages JPA Criteria Specifications to query stored alumni across any permutation of name, role, university, graduation year, or location.
- **Interactive Swagger / OpenAPI 3 UI**: Full interactive API documentation available out of the box at `/swagger-ui/index.html`.
- **Comprehensive Error Handling**: Centralized `@RestControllerAdvice` returning structured validation and service error responses.

---

## Architecture & Design Patterns

The microservice follows **Layered Clean Architecture**:

```
Client (Front-end / Postman / Swagger)
                │
                ▼
      [Controller Layer]           (AlumniSearchController, HealthController)
                │
                ▼
       [Service Layer]             (AlumniService, AlumniServiceImpl)
        │             │
        ▼             ▼
[Strategy Factory]  [Repository Layer]  (AlumniProfileRepository, Specifications)
   │         │               │
   ▼         ▼               ▼
[Phantom]  [Mock]     [PostgreSQL / H2 Database]
```

### Design Patterns Used
1. **Strategy Pattern (`LinkedInScraperStrategy`)**:
   - Defines a common contract for scraping alumni data.
   - Implementations: `PhantomBusterScraperService` (live cloud scraping) and `MockLinkedInScraperService` (high-fidelity simulation).
2. **Factory Pattern (`ScraperStrategyFactory`)**:
   - Dynamically selects the active scraper strategy according to environment configuration and handles graceful fallback during service degradation.
3. **Adapter Pattern (`AlumniDataMapper`)**:
   - Normalizes heterogeneous JSON and CSV fields extracted from PhantomBuster containers into standardized domain entities and DTOs.
4. **Specification Pattern (`AlumniSpecification`)**:
   - Encapsulates reusable, type-safe query predicates using the JPA Criteria API for dynamic filtering.
5. **Data Transfer Object (DTO) Pattern & Custom Deserializers**:
   - Decouples internal database entities from external API contracts. Includes a custom `PassoutYearDeserializer` that tolerates numbers (`2020`), strings (`"2020"`), and placeholders (`"OPTIONAL_PASSOUT_YEAR"`).

---

## Technology Stack

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.3.2 (Spring MVC, Spring Data JPA, Spring Validation, Actuator)
- **Database**: PostgreSQL 16 (Production) & H2 In-Memory (Local Development & Testing)
- **HTTP Client**: Spring 6 `RestClient` & `RestTemplate`
- **Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI
- **Build Tool**: Apache Maven
- **Containerization**: Docker (multi-stage build) & Docker Compose

---

## API Specification

### 1. Search Alumni Profiles API
Initiates alumni discovery via PhantomBuster API, saves records into the database, and returns the retrieved profiles.

- **Method**: `POST`
- **Endpoint**: `/api/alumni/search`
- **Headers**: `Content-Type: application/json`

#### Request Body
```json
{
  "university": "University of XYZ",
  "designation": "Software Engineer",
  "passoutYear": 2020
}
```

#### Response (`200 OK`)
```json
{
  "status": "success",
  "data": [
    {
      "name": "John Doe",
      "currentRole": "Software Engineer",
      "university": "University of XYZ",
      "location": "New York, NY",
      "linkedinHeadline": "Passionate Software Engineer at XYZ Corp",
      "passoutYear": 2020
    },
    {
      "name": "Jane Smith",
      "currentRole": "Data Scientist",
      "university": "University of XYZ",
      "location": "San Francisco, CA",
      "linkedinHeadline": "Data Scientist | AI Enthusiast",
      "passoutYear": 2019
    }
  ]
}
```

---

### 2. Get All Saved Alumni Profiles API
Retrieves all alumni profiles stored in the database with support for pagination and sorting.

- **Method**: `GET`
- **Endpoint**: `/api/alumni/all`
- **Query Parameters**:
  - `page` (optional, default: `0`): Page index
  - `size` (optional, default: `20`): Page size
  - `sortBy` (optional, default: `createdAt`): Property to sort by
  - `direction` (optional, default: `DESC`): Sort direction (`ASC` or `DESC`)

#### Response (`200 OK`)
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "currentRole": "Software Engineer",
      "university": "University of XYZ",
      "location": "New York, NY",
      "linkedinHeadline": "Passionate Software Engineer at XYZ Corp",
      "passoutYear": 2020,
      "linkedinUrl": "https://www.linkedin.com/in/john-doe-100",
      "company": "XYZ Corp"
    }
  ]
}
```

---

### 3. Filter Saved Alumni Profiles API
- **Method**: `GET`
- **Endpoint**: `/api/alumni/filter`
- **Query Parameters**:
  - `university`: University name substring
  - `designation`: Current job title
  - `passoutYear`: Graduation year
  - `location`: City or region
  - `name`: Candidate name

---

### 4. Alumni Statistics API
- **Method**: `GET`
- **Endpoint**: `/api/alumni/stats`

#### Response (`200 OK`)
```json
{
  "status": "success",
  "data": {
    "totalProfiles": 18,
    "topUniversities": {
      "University of XYZ": 10,
      "Stanford University": 8
    },
    "topRoles": {
      "Software Engineer": 12,
      "Data Scientist": 6
    },
    "graduationYearDistribution": {
      "2020": 8,
      "2019": 5
    }
  }
}
```

---

### 5. Health & Diagnostic Check
- **Method**: `GET`
- **Endpoint**: `/api/health`

---

## Database Schema & Integration

PostgreSQL table definition (`alumni_profiles`):

```sql
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
    search_query VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_alumni_university ON alumni_profiles(university);
CREATE INDEX idx_alumni_current_role ON alumni_profiles(job_role);
CREATE INDEX idx_alumni_passout_year ON alumni_profiles(passout_year);
CREATE INDEX idx_alumni_linkedin_url ON alumni_profiles(linkedin_url);
```

---

## Local Setup & Running

### Prerequisites
- Java 21 or higher
- Maven 3.9+ (or use the built-in Maven)

### Running with In-Memory Database (Zero-Configuration)
```bash
# Clone the repository
git clone <YOUR_GITHUB_REPO_URL>
cd <REPO_DIR>

# Run with local profile (uses in-memory database with automatic schema setup)
java -Dspring.profiles.active=local -jar target/alumni-linkedin-searcher-1.0.0.jar
```

Access:
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:alumnidb`)

### Running with Real PhantomBuster API Credentials
Set environment variables:
```bash
export PHANTOMBUSTER_API_KEY="your-api-key"
export PHANTOMBUSTER_AGENT_ID="your-agent-id"
export PHANTOMBUSTER_MOCK_MODE=false
```

---

## Docker & Containerization

To run the complete production stack (Spring Boot + PostgreSQL 16):

```bash
docker-compose up --build -d
```

View application logs:
```bash
docker-compose logs -f alumni-app
```

---

## Public Deployment Guide

You can deploy this service free of charge to **Render** or **Railway** in under 5 minutes:

### Deploying to Render
1. Push this repository to your personal GitHub account (do not include "freightfox" in the repository name).
2. Go to [Render.com](https://render.com) and create a free **PostgreSQL Database**.
3. Create a **New Web Service** connected to your GitHub repository:
   - **Environment**: Docker (Render auto-detects `Dockerfile`)
   - **Environment Variables**:
     - `SPRING_PROFILES_ACTIVE`: `prod`
     - `SPRING_DATASOURCE_URL`: `<Internal_Database_URL_from_Render>`
     - `SPRING_DATASOURCE_USERNAME`: `<User>`
     - `SPRING_DATASOURCE_PASSWORD`: `<Password>`
     - `PORT`: `8080`
4. Click **Deploy**. Your live API URL will be:
   - Live URL: `https://<your-service>.onrender.com`
   - Live Swagger UI: `https://<your-service>.onrender.com/swagger-ui/index.html`

---

## Loom / Video Presentation Script

Use this structured 2–5 minute script for your Loom screen recording:

1. **Introduction & Approach (0:00 - 1:00)**:
   - "Hello! Today I'm presenting the **Alumni LinkedIn Profile Searcher** microservice built with Spring Boot 3.3.2 and Java 21."
   - "The objective is to search for LinkedIn profiles of alumni from a specific university, current designation, and optional graduation year using the PhantomBuster API, and persist the records into a database."
   - "I structured the project using Clean Layered Architecture separating Controller, Service, Strategy, Mapper, and Repository layers."

2. **API Demo via Swagger UI (1:00 - 2:30)**:
   - Open Swagger UI at `/swagger-ui/index.html`.
   - Execute `POST /api/alumni/search` with `university: "University of XYZ"`, `designation: "Software Engineer"`, `passoutYear: 2020`.
   - Show the `200 OK` response matching the required schema.
   - Execute `GET /api/alumni/all` to demonstrate that the scraped alumni records are saved in the database.
   - Show `GET /api/alumni/stats` showcasing analytics across stored records.

3. **Key Technical Decisions (2:30 - 3:45)**:
   - "First, I implemented the **Strategy Pattern** for the scraping layer. The primary strategy integrates with PhantomBuster REST API v2, while a fallback mock strategy ensures zero disruption if API keys or rate limits occur."
   - "Second, I implemented **deduplication and upsert semantics**. When new alumni profiles are searched, existing records are updated rather than duplicated."
   - "Third, I wrote a custom Jackson deserializer for `passoutYear` to gracefully handle both numbers (`2020`) and text placeholders like `'OPTIONAL_PASSOUT_YEAR'` without throwing JSON parsing exceptions."

4. **Challenges & Solution (3:45 - 4:45)**:
   - "One challenge faced during development was asynchronous agent execution with PhantomBuster and SQL reserved keywords like `CURRENT_ROLE` across relational databases. I resolved the database compatibility by mapping the column to `job_role` while preserving the domain property, and handled scraper container polling with configurable timeouts and resilient fallbacks."
   - "Thank you!"
