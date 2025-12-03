# 🔍 Kotlin + Spring Boot In-Memory Search Engine (Lucene-Based)

A high-performance, lightweight search microservice built with **Kotlin**, **Spring Boot**, and **Apache Lucene**, designed to demonstrate **clean architecture**, **sub-millisecond in-memory search**, and **production-grade backend engineering practices**.

This project showcases how a modern search service can be implemented **without** the complexity and operational cost of distributed systems like Elasticsearch or OpenSearch.

---

## 🚀 Key Features

- **Ultra-Fast In-Memory Search**
    - Lucene index built entirely at startup
    - Sub-millisecond query latency
    - Ideal for read-heavy workloads

- **Advanced Search API**
    - `GET /api/properties/{id}` – Retrieve a single resource
    - `POST /api/properties/search` – Advanced Lucene-powered search with filters
    - **Faceted Search:** Multi-dimensional aggregations (city, type, amenities, price ranges)
    - **Geo-location Search:** Radius-based queries with latitude/longitude
    - **Multi-criteria Filtering:** Price, rating, amenities, property type, guests, bedrooms
    - Idiomatic Kotlin responses with minimal overhead

- **In-Memory Object Store (O(1) Lookup)**
    - Full `Property` objects kept in RAM
    - Lucene index stores only *searchable* fields
    - Search flow:
        1. Lucene returns only document IDs
        2. IDs mapped to full objects via O(1) hashmap lookup
    - Same architectural pattern used by **Elasticsearch**, **Algolia**, and **Solr** for large-scale search systems

- **Professional Logging & Performance Analysis**
    - Structured logging with timing information
    - JMH benchmarking suite for performance testing
    - Comprehensive test coverage with metrics

- **Performance Optimization**
    - Sub-millisecond in-memory Lucene queries
    - FastTaxonomyFacetCounts for efficient aggregations
    - Optimized indexing with configurable buffer sizes

- **Scalable & Production-Ready Architecture**
    - Stateless design → horizontal scaling ready
    - In-memory architecture → predictable performance
    - Spring Boot → containerization and cloud deployment ready

- **Comprehensive Test Suite**
    - **Unit Tests:** Service layer with Mockito mocking
    - **Integration Tests:** Full HTTP API testing with TestRestTemplate
    - **Index Tests:** Lucene search functionality (17 test scenarios)
    - **Facets Tests:** Multi-dimensional aggregation validation
    - **JMH Benchmarks:** Performance testing with realistic datasets
    - JUnit 5 + Kotlin test framework

---

## 🧠 Architecture & Design Philosophy

Modern search stacks like Elasticsearch/OpenSearch are powerful but **complex and costly**:

- Sharding, replication, routing, and node sizing are often misconfigured
- Operational maintenance is non-trivial
- SaaS versions are expensive; self-hosted clusters require expertise
- Latency increases with network hops and cluster coordination

### ✔ Alternative: In-Memory Lucene

- Load entire searchable dataset into RAM
- Build Lucene index at startup
- Sub-millisecond local queries
- Zero network coordination
- Stateless by design → scale horizontally as needed
- In many real-world use cases, this model is faster, simpler, and far cheaper

---

## 🗃️ Hybrid Architecture: Indexed Fields + In-Memory Object Store

This project uses the **same pattern as industrial search engines**:

### 🔹 Lucene Index
Stores *only* fields needed for search (tokens, text, numeric ranges).  
Example: name, city, country, amenities, price, rating…

### 🔹 In-Memory Object Store
Stores the **full `Property` object** in a `ConcurrentHashMap<String, Property>`.

### 🔹 Search Flow
1. Lucene returns document IDs based on scoring
2. Full objects are restored via O(1) lookup
3. Response assembled with complete domain objects

### ✔ Benefits
- Smaller index → faster queries
- Zero deserialization cost
- No large blobs in Lucene
- Perfect for read-heavy systems
- Deterministic performance
- Stateless scaling out-of-the-box

---

## 📈 What This Project Demonstrates

- **Enterprise-Grade Search Engine:** Faceted search, geo-queries, multi-criteria filtering
- **High-Performance Backend Engineering:** Sub-millisecond Lucene queries with FastTaxonomyFacetCounts
- **Clean Architecture:** Kotlin + Spring Boot with proper separation of concerns
- **Production Testing:** 35+ tests covering unit, integration, facets, and performance
- **Configurable Data Loading:** Support for file:// and generate:// data sources
- **Professional Logging & Monitoring:** Structured logging with performance metrics
- **Scalable Design:** Stateless architecture ready for horizontal scaling

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (tested with Java 21/25)
- **Maven 3.6+**

### Run the Application
```bash
# Clone and run (works out of the box!)
git clone https://github.com/jordicortesbravo/kotlin-lucene-search-engine.git
cd kotlin-lucene-search-engine

# Start the server
mvn spring-boot:run

# Or compile and run
mvn clean compile
mvn exec:java -Dexec.mainClass="com.jcortes.Application"
```

### Test the API
```bash
# Get property by ID
curl http://localhost:8080/api/properties/1

# Search with filters
curl -X POST http://localhost:8080/api/properties/search \
  -H "Content-Type: application/json" \
  -d '{
    "cities": ["Madrid"],
    "minPrice": 100,
    "maxPrice": 200,
    "facets": ["city", "type", "priceRange"]
  }'
```

### Run Tests & Benchmarks
```bash
# Run all tests (35+ test scenarios)
mvn test

# Run specific test suites
mvn test -Dtest=PropertyIndexTest          # Lucene search tests
mvn test -Dtest=PropertyControllerIntegrationTest  # API tests
mvn test -Dtest=PropertySearchBenchmarkTest        # Performance tests
```

**✨ The application loads 10 sample properties at startup and is immediately ready for search queries!**

---

## 📂 Source Code

**Repository:** [https://github.com/jordicortesbravo/kotlin-lucene-search-engine](https://github.com/jordicortesbravo/kotlin-lucene-search-engine)

---

**Author:** Jordi Cortés Bravo — Senior Software Engineer & Technical Lead  
Spain (Remote)

