## Local Development Setup

To run the application locally, follow these steps. You must use the `local` Spring profile.

### Requirements

- Docker & Docker Compose
- Any PostgreSQL service (Docker, local installation, or pgAdmin)

### 1. Start PostgreSQL (example via Docker)

```bash
docker run --name lumen-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

You can connect via pgAdmin or any preferred DB client.

### 2. Start LocalStack and Create S3 Buckets

```bash
./run-localstack.sh
```

This script will:

- Launch LocalStack via `docker-compose.yml`
- Automatically run `01-create-buckets.sh` to create required S3 buckets:
    - `test-movie-poster`
    - `test-movie-video`

S3 will be available at: `http://localhost:4566`

### 3. Run the Spring Boot App with the `local` profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
