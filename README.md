# 🔍 Kotlin + Spring Boot In-Memory Search Engine (Lucene-Based)

A high-performance, lightweight search microservice built with **Kotlin**, **Spring Boot**, and **Apache Lucene**, designed to demonstrate **clean architecture**, **sub-millisecond in-memory search**, and **production-grade backend engineering practices**.

This project showcases how a modern search service can be implemented **without** the complexity and operational cost of distributed systems like Elasticsearch or OpenSearch.

---

## 🚀 Key Features

- **Ultra-Fast In-Memory Search**
    - Lucene index built entirely at startup
    - Sub-millisecond query latency
    - Ideal for read-heavy workloads

- **Clean REST API**
    - `GET /properties/{id}` – Retrieve a single resource
    - `GET /properties/search?q=` – Lucene-powered search endpoint
    - Idiomatic Kotlin responses with minimal overhead

- **In-Memory Object Store (O(1) Lookup)**
    - Full `Property` objects kept in RAM
    - Lucene index stores only *searchable* fields
    - Search flow:
        1. Lucene returns only document IDs
        2. IDs mapped to full objects via O(1) hashmap lookup
    - Same architectural pattern used by **Elasticsearch**, **Algolia**, and **Solr** for large-scale search systems

- **Metrics & Observability**
    - Micrometer integration
    - P95 / P99 latency
    - Prometheus/Grafana ready

- **Benchmarking Included**
    - Indexing performance
    - Query percentiles
    - Reproducible performance setup

- **Containerized & Cloud-Ready**
    - Lightweight Docker packaging
    - Stateless → horizontal scaling is trivial

- **Unit Testing**
    - JUnit 5
    - API + Lucene index tests

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

- High-performance backend engineering
- Clean and idiomatic Kotlin + Spring Boot design
- Lucene-powered in-memory search
- Metrics-driven observability
- Minimal-complexity, high-throughput architecture
- A production-inspired pattern without distributed overhead

---

## 📂 Source Code

**Repository:** `https://github.com/your/repository`  
*(Replace with actual link)*

---

**Author:** Jordi Cortés Bravo — Senior Software Engineer & Technical Lead  
Spain (Remote)

