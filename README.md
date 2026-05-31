# Kafka Back Pressure Testing Scenarios

This document describes positive and negative test scenarios for validating a Spring Boot + Kafka + MySQL order processing system with back-pressure handling, retry mechanisms, idempotency, and fault tolerance.

---

# Architecture Under Test

```text
REST API
   │
   ▼
Kafka Producer
   │
   ▼
Kafka Topic
   │
   ▼
Kafka Consumer
   │
   ▼
MySQL Database
```

---

# Positive Test Scenarios

## Scenario 1: Normal Traffic

### Request

```json
{
  "eventId": "E1001",
  "orderId": "ORD1001",
  "product": "Laptop",
  "quantity": 1
}
```

### Flow

```text
REST
 ↓
Kafka
 ↓
Consumer
 ↓
DB
```

### Expected Result

Consumer Log:

```text
Processing Order ORD1001
Order Saved
```

Database:

```sql
select * from orders;
```

Result:

```text
ORD1001
Laptop
1
CREATED
```

Consumer Lag:

```text
0
```

---

## Scenario 2: Multiple Orders

### Send 10 Requests

```json
{
  "eventId": "E1002",
  "orderId": "ORD1002",
  "product": "Mobile",
  "quantity": 2
}
```

```json
{
  "eventId": "E1003",
  "orderId": "ORD1003",
  "product": "TV",
  "quantity": 1
}
```

### Expected Result

* 10 records inserted into MySQL
* Kafka Consumer Lag = 0
* No processing failures

---

## Scenario 3: Burst Traffic

### Send 1000 Requests Within Seconds

Example k6 Script:

```javascript
import http from 'k6/http';

export default function () {

  http.post(
      'http://localhost:8080/orders',
      JSON.stringify({
          eventId: crypto.randomUUID(),
          orderId: crypto.randomUUID(),
          product: 'Laptop',
          quantity: 1
      }),
      {
          headers: {
              'Content-Type': 'application/json'
          }
      }
  );
}
```

### Expected Result

* Kafka absorbs burst traffic
* Consumer processes messages gradually
* No application crash
* No message loss

Temporary lag is acceptable:

```text
Consumer Lag = 500
```

---

## Scenario 4: Consumer Slower Than Producer

### Add Artificial Delay

```java
Thread.sleep(5000);
```

Inside:

```java
service.process(event);
```

### Send

```text
500 Requests
```

### Expected Result

* Kafka Lag increases
* Application remains healthy
* MySQL remains available
* Back pressure works correctly

---

## Scenario 5: Duplicate Message Handling

### Send Message

```json
{
  "eventId": "E5001",
  "orderId": "ORD5001",
  "product": "Laptop",
  "quantity": 1
}
```

### Send Same Message Again

```json
{
  "eventId": "E5001",
  "orderId": "ORD5001",
  "product": "Laptop",
  "quantity": 1
}
```

### Expected Result

Only one record is inserted because:

```java
processedRepo.existsById(eventId)
```

returns:

```text
true
```

Database:

```text
1 record only
```

---

## Scenario 6: Kafka Restart

### During Message Production

```bash
docker restart kafka
```

### Expected Result

* Producer retries
* Consumer reconnects automatically
* Processing resumes
* No data loss

---

## Scenario 7: Application Restart

### Send

```text
1000 Orders
```

### Stop Spring Boot

```bash
Ctrl + C
```

### Start Application Again

### Expected Result

* Consumer resumes processing
* Remaining messages are consumed
* Offsets continue from last committed position

---

## Scenario 8: Temporary MySQL Slowdown

### Add Delay

```java
Thread.sleep(10000);
```

Before:

```java
repository.save(order);
```

### Expected Result

* Consumer throughput decreases
* Kafka Lag increases
* System remains stable

---

## Scenario 9: Semaphore Protection

### Configuration

```java
new Semaphore(100)
```

### Send

```text
10000 Requests
```

### Expected Result

* Maximum 100 concurrent DB operations
* Database protected from overload
* No DB storm

---

# Negative Test Scenarios

## Scenario 10: MySQL Container Restart

### During Processing

```bash
docker restart mysql
```

### Expected Result

Consumer Errors:

```text
Cannot get JDBC connection
```

Messages remain in Kafka.

After MySQL is available:

* Consumer retries
* Processing resumes

---

## Scenario 11: Invalid JSON

### Request

```json
{
  "eventId": "E1",
  "orderId":
}
```

### Expected Result

```http
400 Bad Request
```

* No Kafka message produced
* No DB insert

---

## Scenario 12: Missing Required Fields

### Request

```json
{
  "eventId": "E100"
}
```

### Validation Model

```java
public record OrderEvent(

        @NotBlank String eventId,

        @NotBlank String orderId,

        @NotBlank String product,

        @NotNull Integer quantity
) {}
```

### Expected Result

```http
400 Bad Request
```

---

## Scenario 13: Database Connection Exhaustion

### Hikari Configuration

```yaml
maximum-pool-size: 2
```

### Send

```text
5000 Requests
```

### Expected Result

* Hikari timeout exceptions
* Kafka lag increases
* Application survives
* No consumer crash

---

## Scenario 14: Database Constraint Violation

### Example

Duplicate Primary Key

### Expected Result

Exception:

```text
DataIntegrityViolationException
```

Message Handling:

* Retry Processing
* Or Move To DLT (Dead Letter Topic)

Based on application configuration.

---

## Scenario 15: Kafka Topic Missing

### Delete Topic

```bash
kafka-topics.sh --delete --topic orders-topic
```

### Send Request

### Expected Result

```text
Topic not found
```

Producer fails to publish.

---

## Scenario 16: Kafka Unavailable

### Stop Kafka

```bash
docker stop kafka
```

### Request

```http
POST /orders
```

### Expected Result

Producer timeout.

Recommended API Response:

```http
503 Service Unavailable
```

---

## Scenario 17: Out-of-Order Events

### Send First

```json
{
  "eventId": "E1",
  "orderId": "ORD100",
  "status": "DELIVERED"
}
```

### Then Send

```json
{
  "eventId": "E2",
  "orderId": "ORD100",
  "status": "CREATED"
}
```

### Expected Result

Business validation failure.

Example:

```text
Invalid Order State Transition
```

---

# Large Scale Stress Testing

## Scenario 18: Massive Load Test

### Send

```text
100000 Requests
```

### Monitor

* Kafka Lag
* CPU Usage
* Memory Usage
* JVM Heap
* Garbage Collection
* MySQL Connections
* Semaphore Usage
* Thread Count
* Consumer Throughput

### Expected Result

* No Out Of Memory Error
* No Application Crash
* No Message Loss
* Stable Throughput

---

# Enterprise Back Pressure Failure Matrix

| Scenario                          | Expected Behavior              |
| --------------------------------- | ------------------------------ |
| Producer 10x Faster Than Consumer | Lag increases, system survives |
| Kafka Restart                     | Auto reconnect                 |
| Consumer Restart                  | Resume from offset             |
| MySQL Restart                     | Retry processing               |
| Database Slowdown                 | Lag increases                  |
| Duplicate Messages                | Ignored via idempotency        |
| Invalid Payload                   | Rejected                       |
| Kafka Unavailable                 | Producer error                 |
| Topic Deleted                     | Producer error                 |
| Connection Pool Exhausted         | Lag increases, no crash        |
| 100k Request Burst                | Stable processing              |

---

# Success Criteria

The system is considered enterprise-ready when:

* No message loss occurs
* No Out Of Memory (OOM) occurs
* Consumer resumes after restart
* Producer retries successfully
* Kafka handles burst traffic
* Database failures are recoverable
* Duplicate messages are ignored
* Back pressure protects downstream systems
* Connection pool exhaustion does not crash services
* Large traffic spikes are absorbed safely

---

# Recommended Monitoring During Tests

## Kafka

* Consumer Lag
* Topic Throughput
* Producer Rate
* Consumer Rate

## JVM

* Heap Usage
* GC Activity
* Thread Count

## MySQL

* Active Connections
* Query Latency
* Slow Queries

## Application

* Request Rate
* Error Rate
* Response Time
* Retry Count
* DLT Count

These metrics provide complete visibility into system behavior under normal, failure, and extreme-load conditions.
