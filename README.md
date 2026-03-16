# Sporty Betting Settlement Trigger Service

A Spring Boot application that simulates sports betting event outcome handling and bet settlement via **Kafka** and **RocketMQ** (mocked).

## Architecture

```
POST /api/event-outcomes
        │
        ▼
EventOutcomeProducer ──► Kafka topic: event-outcomes
                                  │
                                  ▼
                         EventOutcomeConsumer
                                  │
                    Query H2 DB for bets by eventId
                                  │
                                  ▼
                     BetSettlementProducer (MOCK)
                         Logs to: bet-settlements
```

> **Note on RocketMQ:** Per the assignment requirements ("If the RocketMQ setup is too complex, use mocks for the RocketMQ producer. Just log the payload."), `BetSettlementProducer` is implemented as a mock that logs the full JSON payload.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.3.2 |
| Language  | Java 17 |
| Messaging | Apache Kafka (spring-kafka) |
| RocketMQ  | Mocked (logs payload) |
| Database  | H2 in-memory |
| ORM       | Spring Data JPA / Hibernate |

## Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `./mvnw` wrapper)
- Docker & Docker Compose

## Running the Application

### Step 1 — Start Kafka

```bash
docker-compose up -d
```

This starts a single-node Kafka broker in KRaft mode (no Zookeeper) on `localhost:9092`.

Wait ~10 seconds for Kafka to be ready:

```bash
docker-compose ps        # should show "healthy"
```

### Step 2 — Start the Spring Boot Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package -DskipTests
java -jar target/sporty-betting-1.0.0.jar
```

The application starts on **http://localhost:8080**.

On startup it automatically:
- Creates the Kafka topics (`event-outcomes`, `bet-settlements`)
- Seeds the H2 database with 5 sample bets

## API Endpoints

### Postman collection 
``` sporty-betting.postman_collection.json ```

### Publish a Sports Event Outcome

```
POST /api/event-outcomes
Content-Type: application/json
```

**Request body:**
```json
{
  "eventId": "event-100",
  "eventName": "Champions League Final",
  "eventWinnerId": "team-A"
}
```

**Response (202 Accepted):**
```json
{
  "status": "accepted",
  "message": "Event outcome published to Kafka",
  "eventId": "event-100"
}
```

**What happens:**
1. The outcome is published to the Kafka `event-outcomes` topic
2. The Kafka consumer picks it up and queries the database for bets matching `eventId`
3. For each matching bet, a `BetSettlement` is sent to the (mocked) RocketMQ `bet-settlements` topic — the payload is logged

---

### View All Bets

```
GET /api/bets
```

### View Bets by Event

```
GET /api/bets/event/{eventId}
```

### Create a Bet

```
POST /api/bets
Content-Type: application/json
```

```json
{
  "betId": "bet-custom",
  "userId": "user-99",
  "eventId": "event-100",
  "eventMarketId": "market-3",
  "eventWinnerId": "team-A",
  "betAmount": 200.00
}
```

### H2 Console (in-browser SQL)

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:bettingdb
Username: sa
Password: (empty)
```

## Pre-seeded Bets

| betId | userId | eventId   | eventMarketId | eventWinnerId | betAmount |
|-------|--------|-----------|---------------|---------------|-----------|
| bet-1 | user-1 | event-100 | market-1      | team-A        | 50.00     |
| bet-2 | user-2 | event-100 | market-1      | team-B        | 25.00     |
| bet-3 | user-3 | event-100 | market-2      | team-A        | 100.00    |
| bet-4 | user-4 | event-200 | market-1      | team-C        | 75.00     |
| bet-5 | user-5 | event-200 | market-1      | team-D        | 30.00     |

## Example End-to-End Test

**Scenario:** Event `event-100` concludes with winner `team-A`.

```bash
curl -X POST http://localhost:8080/api/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{"eventId":"event-100","eventName":"Champions League Final","eventWinnerId":"team-A"}'
```

**Expected logs:**
```
INFO  EventOutcomeProducer    : Publishing event outcome to Kafka topic 'event-outcomes': ...
INFO  EventOutcomeProducer    : Event outcome published successfully for eventId=event-100, offset=0
INFO  EventOutcomeConsumer    : Received event outcome from Kafka: EventOutcome(eventId=event-100, ...)
INFO  EventOutcomeConsumer    : Found 3 bet(s) to settle for eventId=event-100
INFO  BetSettlementProducer   : [RocketMQ MOCK] Sending to topic 'bet-settlements': {"betId":"bet-1","userId":"user-1","eventId":"event-100","eventMarketId":"market-1","eventWinnerId":"team-A","betAmount":50.00,"won":true,"settledAt":"..."}
INFO  BetSettlementProducer   : [RocketMQ MOCK] Sending to topic 'bet-settlements': {"betId":"bet-2","userId":"user-2","eventId":"event-100","eventMarketId":"market-1","eventWinnerId":"team-A","betAmount":25.00,"won":false,"settledAt":"..."}
INFO  BetSettlementProducer   : [RocketMQ MOCK] Sending to topic 'bet-settlements': {"betId":"bet-3","userId":"user-3","eventId":"event-100","eventMarketId":"market-2","eventWinnerId":"team-A","betAmount":100.00,"won":true,"settledAt":"..."}
INFO  EventOutcomeConsumer    : Settlement processing complete for eventId=event-100
```

- `bet-1` → **won** (placed on team-A ✓)
- `bet-2` → **lost** (placed on team-B ✗)
- `bet-3` → **won** (placed on team-A ✓)

## Running Tests

```bash
./mvnw test
```

Tests use an embedded Kafka broker — no Docker required.

## Stopping

```bash
# Stop the Spring Boot app: Ctrl+C
# Stop Kafka:
docker-compose down
```
