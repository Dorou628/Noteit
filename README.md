# Noteit

Noteit is a Spring Boot MVP backend for a Xiaohongshu-style note and article community. The current goal is to provide a stable, testable API surface for frontend/backend integration while keeping room for later production architecture changes such as JWT auth, feed fanout, Redis, Kafka, Canal, and AI summary providers.

## Current Scope

The MVP currently supports:

- Plain-password login for development and integration testing
- Header-based mock authentication with `X-User-Id`
- Article publishing, editing, detail, feed, and author article lists
- Article images and cover metadata
- Upload task creation and upload confirmation for direct OSS-style uploads
- Like/unlike and favorite/unfavorite
- Liked article and favorited article lists
- Follow/unfollow, following lists, and follower lists
- User profile query and profile editing
- Basic demo seed data for local integration testing

The API contract is maintained in [docs/API-MVP.md](docs/API-MVP.md).

## Tech Stack

- Java 17
- Spring Boot 4.0.5
- Spring Web, Validation, JDBC, Actuator
- MyBatis 4.0.0
- Flyway
- MySQL for development
- H2 for automated tests
- Maven Wrapper

## Project Layout

```text
src/main/java/com/example/noteit
  article/       Article publishing, feed, detail, media
  auth/          MVP login and auth user lookup
  common/        Shared response, auth context, IDs, errors, events
  file/          Upload task and presigned upload abstractions
  interaction/   Likes, favorites, interaction snapshots
  relation/      Follow relations, following/follower lists
  summary/       AI summary extension boundary
  user/          User profile query and editing

src/main/resources
  db/migration/  Flyway migrations and seed data
  mapper/        MyBatis XML mappers

docs/
  API-MVP.md
  DELIVERY-PLAN-MVP.md
  DEV-SEED-DATA.md
  DB-SCHEMA-MVP.md
  TECH-ARCHITECTURE-MVP.md
```

## Prerequisites

- JDK 17
- MySQL 8.x for the `dev` profile
- PowerShell, Git, and a reachable Git remote

The test profile uses H2 and does not require a local MySQL instance.

## Configuration

The default profile is `dev,local`. Development database settings can be provided with environment variables:

| Variable | Default |
| --- | --- |
| `NOTEIT_DB_HOST` | `localhost` |
| `NOTEIT_DB_PORT` | `3306` |
| `NOTEIT_DB_NAME` | `noteit` |
| `NOTEIT_DB_USERNAME` | `root` |
| `NOTEIT_DB_PASSWORD` | `root` |
| `NOTEIT_OSS_ENDPOINT` | `https://oss-cn-hangzhou.aliyuncs.com` |
| `NOTEIT_OSS_BUCKET` | `noteit-dev` |
| `NOTEIT_OSS_ACCESS_KEY_ID` | empty |
| `NOTEIT_OSS_ACCESS_KEY_SECRET` | empty |
| `NOTEIT_OSS_PUBLIC_BASE_URL` | `https://noteit-dev.oss-cn-hangzhou.aliyuncs.com` |
| `NOTEIT_AI_ENABLED` | `false` |

Create the local database before starting the app:

```sql
CREATE DATABASE noteit CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Flyway will create and seed the schema on startup.

## Run Locally

```powershell
.\mvnw.cmd spring-boot:run
```

The service starts with base path:

```text
http://localhost:8080/api/v1
```

Health checks are exposed through Actuator:

```text
GET http://localhost:8080/actuator/health
```

## Run Tests

```powershell
.\mvnw.cmd test
```

Tests run against an in-memory H2 database using the `test` profile.

## MVP Authentication

The MVP uses a simple login endpoint and header-based mock auth for later business requests.

Login example:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "style",
  "password": "123456"
}
```

After login, the frontend should keep the returned user information and send:

```http
X-User-Id: 101
X-User-Nickname: Noteit_穿搭研究所
```

JWT is intentionally reserved for a later phase.

## Seed Accounts

The development seed data includes these users:

| User | Username | Password | User ID |
| --- | --- | --- | --- |
| Noteit_穿搭研究所 | `style` | `123456` | `101` |
| Noteit_城市漫游 | `city` | `123456` | `102` |
| Noteit_效率手帐 | `notes` | `123456` | `103` |

Seed articles use IDs `1001` through `1006`. See [docs/DEV-SEED-DATA.md](docs/DEV-SEED-DATA.md) for integration examples.

## Key Endpoints

All business endpoints use `/api/v1`.

| Feature | Endpoint |
| --- | --- |
| Login | `POST /auth/login` |
| Feed | `GET /articles?pageNo=1&pageSize=10` |
| Article detail | `GET /articles/{articleId}` |
| Create article | `POST /articles` |
| Update article | `PUT /articles/{articleId}` |
| Like/unlike | `PUT /articles/{articleId}/like`, `DELETE /articles/{articleId}/like` |
| Favorite/unfavorite | `PUT /articles/{articleId}/favorite`, `DELETE /articles/{articleId}/favorite` |
| User profile | `GET /users/{userId}` |
| Edit my profile | `PUT /users/me/profile` |
| User articles | `GET /users/{userId}/articles` |
| My liked articles | `GET /users/me/liked-articles` |
| My favorited articles | `GET /users/me/favorited-articles` |
| Follow/unfollow | `PUT /users/{userId}/follow`, `DELETE /users/{userId}/follow` |
| Following list | `GET /users/{userId}/following` |
| Follower list | `GET /users/{userId}/followers` |
| Upload task | `POST /upload-tasks`, `POST /upload-tasks/{uploadTaskId}/complete` |

## API Response Shape

Successful responses use:

```json
{
  "code": "0",
  "message": "success",
  "requestId": "req-xxx",
  "data": {}
}
```

Failed responses use:

```json
{
  "code": "INVALID_PARAMETER",
  "message": "Request parameter is invalid",
  "requestId": "req-xxx"
}
```

`data` is omitted on failures because null fields are not serialized.

## ID Strategy

The MVP currently uses a simple local incremental ID generator to make manual testing and assertions easier. A Snowflake ID generator is already reserved in code for later switching.

Demo seed IDs are intentionally short:

- Users: `101-103`
- Articles: `1001-1006`
- Article media: `2001+`
- Likes: `3001+`
- Favorites: `4001+`
- Follows: `5001+`

## Documentation

- [MVP API Contract](docs/API-MVP.md)
- [Delivery Plan](docs/DELIVERY-PLAN-MVP.md)
- [Development Seed Data](docs/DEV-SEED-DATA.md)
- [Database Schema](docs/DB-SCHEMA-MVP.md)
- [Technical Architecture](docs/TECH-ARCHITECTURE-MVP.md)
