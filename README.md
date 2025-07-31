# Be the Master!

[![Version](https://img.shields.io/badge/version-0.1.0-blue.svg)](https://semver.org)  
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)  
[![Docker Compose](https://img.shields.io/badge/Docker--Compose-3-blue)](https://docs.docker.com/compose/)  
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-lightblue)](https://github.com/pgvector/pgvector)  
[![Redis](https://img.shields.io/badge/Redis-7.0.10-red)](https://redis.io/)

---

**Be the Master!** is a RAG (Retrieval-Augmented Generation) application for managing and semantically querying a collection of text chunks representing the *lore* or *rules* of a game.  
This repository provides the full containerized setup using Docker Compose.

---

## Architecture Overview

This system is composed of the following services:

- **API Service** ([be-the-master-api](https://github.com/AGRFesta/be-the-master-api))  
  Exposes endpoints for inserting and semantically querying game-related content.  
  *Docker image must be built locally from the repository.*

- **PostgreSQL + pgvector** (`btm-db`)  
  Vector database storing chunk embeddings.  
  *Image available on Docker Hub: `pgvector/pgvector:pg16`*

- **Redis** (`btm-cache`)  
  Used for caching to improve performance.  
  *Image available on Docker Hub: `redis:7.0.10-alpine`*

- **Embedding Service** ([e5-embeddings-service](https://github.com/AGRFesta/e5-embedding-service))  
  Generates multilingual embeddings using the `e5-large-multilingual` model.  
  *Docker image must be built locally from the repository.*

---

## Tech Stack

- **Kotlin 1.9.24**, **Spring Boot 3.5.3**
- **PostgreSQL** with **pgvector**
- **Redis 7.0.10 Alpine**
- **e5-large-multilingual** (via local embedding container)
- **Docker & Docker Compose v3**

---

## Getting Started

### 1. Requirements

- Docker and Docker Compose installed
- A `.env` file containing:

```env
POSTGRES_USER=btm
POSTGRES_PASSWORD=your_password

REDIS_PASSWORD=your_redis_password

PROVIDERS_OPENAI_APIKEY=your_openai_key

BTM_DB_CONTAINER=btm-db
BTM_CACHE_CONTAINER=btm-cache
BTM_API_CONTAINER=btm-api
E5_SERVICE_CONTAINER=e5-embeddings-service

BTM_DB_HOST_PORT=5432
BTM_CACHE_HOST_PORT=6379
BTM_API_HOST_PORT=8080
E5_SERVICE_HOST_PORT=8000
