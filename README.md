# URL Shortener with Caching and Metrics

> The focus of this project is high-speed reads with low latency, data integrity, and local execution simplicity.

## 🎯 What to Build

An API that takes a long URL, generates a unique short identifier (e.g., `app.io/aB3x9`), redirects incoming requests via HTTP status code `302` or `301`, and logs access metrics.

## 💡 Real-World Problem It Solves

Shortening link length for easy sharing, masking tracking parameters, and measuring click engagement without degrading the response time of the primary redirect route.

## 🛣️ Core Endpoints

- [x] `POST /api/v1/urls` — Generates the short code with format validation and optional expiration.
- [x] `GET /{shortCode}` — Performs immediate redirection to the original URL.
- [x] `GET /api/v1/urls/{shortCode}/stats` — Returns total clicks, referrers, and peak traffic times.

## 🛠️ Key Concepts & Practices

- [x] **Encoding Algorithm:** Base62 conversion from numeric IDs (avoids MD5/SHA256 hash collisions without unsafe truncation).
- [x] **Caching Layer with Redis:** Key-value storage (`shortCode` &rarr; `originalUrl`) to serve redirects without querying the relational database on every request.
- [x] **Asynchronous Metrics Handling:** Increments click counters without blocking the redirect response (using an in-memory counter, Redis `INCR`, or internal events).
- [ ] **Containerization:** `docker-compose.yml` containing the application, relational database (PostgreSQL/MySQL), and Redis configured with health checks.

## 📚 Architectural Decisions

See [decisions/README.md](./decisions/README.md) for ADRs and technical specifications (v1 to v9).
