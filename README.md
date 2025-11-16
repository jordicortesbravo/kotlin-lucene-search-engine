# 🔍 Kotlin + Spring Boot In-Memory Search Engine (Lucene-Based)

A high-performance, lightweight search microservice built with **Kotlin**, **Spring Boot**, and **Apache Lucene**, designed to demonstrate **clean architecture**, ultra-fast in-memory indexing, observability, and modern backend engineering practices. Ideal for read-heavy workloads requiring sub-millisecond query performance.

---

## 🚀 Key Features

- **High-Performance Search**  
  - Lucene index built at startup  
  - Full-text search with relevance scoring  
  - Optimized for in-memory operation (low latency)

- **Clean REST API**  
  - `GET /items/{id}` – Retrieve a resource  
  - `GET /search?q=` – Lucene-powered search endpoint  
  - JSON responses, idiomatic Kotlin design

- **Metrics & Observability**  
  - Micrometer integration  
  - P95/P99 search latency tracking  
  - Prometheus/Grafana ready

- **Benchmarking Included**  
  - Indexing time  
  - Average latency & percentiles  
  - Performance reproducibility

- **Containerized & Cloud-Ready**  
  - Lightweight Docker image  
  - Easy horizontal scaling

- **Unit Testing**  
  - JUnit 5  
  - API + Lucene index tests

---

## 🧠 Architecture & Design Philosophy

Modern search stacks (Elasticsearch/OpenSearch/NoSQL) are powerful but **complex and costly**:  
- Sharding, routing, node-to-shard ratios often misconfigured  
- Ongoing maintenance, scaling, monitoring required  
- SaaS options expensive; self-managed clusters operationally heavy  

**Alternative approach:** Lucene in-memory  
- Load entire searchable dataset into RAM  
- Build Lucene index at startup  
- Serve queries independently on each node  
- Achieve **sub-millisecond latency** for read-heavy workloads  
- **Trivial horizontal scaling**: add nodes → increase throughput  
- No master/slave, no replication, no coordination  

**Optimal Use Cases:**  
- Dataset fits in memory (tens of thousands to millions of items)  
- Read-heavy workloads  
- Ultra-fast search required  
- Low operational overhead and cost  

---

## 📈 What This Project Demonstrates

- High-performance, production-ready backend design  
- Clean, idiomatic Kotlin + Spring Boot architecture  
- In-memory search with Lucene and real benchmarking  
- Metrics-driven observability and testing  
- Scalable, minimal-complexity systems design  

A modern, minimalist alternative to distributed search stacks—fast, predictable, and operationally simple.

---

## 📂 Source Code

**[View the code here](repo-link)**

---

**Author:** Jordi Cortés Bravo — Senior Software Engineer & Technical Lead  
Spain (Remote)
