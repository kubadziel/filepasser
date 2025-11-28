# FilePasser

**FilePasser** is a learning-oriented project designed to explore **microservice communication**, and **cloud integration** using modern technologies.

The goal is to build an extendable ecosystem where different services can exchange, transform, and route financial ISO 20022 XML messages between clients, applications, and banks — with a focus on **performance, reliability, scalability, and secure access (Single Sign-On)**.

---

## Project Goals

- Learn how to design and build a **microservice-based messaging ecosystem**
- Understand **message routing**, **file storage**, and **asynchronous communication**
- Explore **cloud-native development** using **Azure**, **Docker**, and **RabbitMQ**
- Practice **Java Spring Boot** backend development and **React TypeScript** frontend integration
- Develop hands-on experience with **cloud storage**, **databases**, and **DevOps tools**
- Implement and understand **Single Sign-On (SSO)** authentication for user login and identity management

---

## Current MVP: Router Application

The first component in the FilePasser ecosystem is the **Router**, responsible for:

- Receiving financial messages (ISO 20022 XML) from clients
- Validating and hashing incoming files
- Uploading original XML files to **MinIO** (S3-compatible object storage)
- Storing message metadata in **PostgreSQL**
- Returning message status to clients

This serves as the foundation for adding further messages processing and routing logic in future phases.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3 |
| **Storage** | PostgreSQL 16 |
| **Object Storage** | MinIO (S3 API compatible) |
| **Containerization** | Docker & Docker Compose |
| **Cloud Platform** | Azure |
| **Frontend (planned)** | React + TypeScript |
| **Message Format** | ISO 20022 XML |

---

## Local Development Setup

### Prerequisites
- [Docker Desktop]
- [Java 21]
- [Maven 3.9+]
- Git

### Start infrastructure and app
Before running the app, create a `.env` file in your project root.  
This file is **required** by both `docker-compose.yml` and `application.yml`.

Then in the root directory (where `docker-compose.yml` is located) run:

```bash
make up
```

## Testing

### Backend
- `RUN_TESTS=1 make shared-events-build`, `RUN_TESTS=1 make router-build`, `RUN_TESTS=1 make uploader-build` execute each module’s unit/integration suites.
- `mvn -pl system-tests test` boots Postgres, Kafka, and MinIO via Testcontainers and validates the uploader → router → MinIO flow with real services.
- `make e2e-full` ensures Docker is running, starts the Vite dev server, runs the Playwright happy-path test against the live stack, and tears everything down if it started it.

### Frontend
```bash
cd filepasser-frontend
npm install
npx playwright install --with-deps   # first run only
npm run dev                          # serves http://localhost:5173
PLAYWRIGHT_BASE_URL=http://localhost:5173 npm run test:e2e
```
- Set `E2E_REAL_BACKEND=1` (and optionally `UPLOAD_ENDPOINT=...`) to run the happy-path Playwright spec against the live backend (requires the stack from `make up`).
- Use `npm run test:e2e:ui` for Playwright’s interactive runner.
