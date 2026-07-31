# Payment Engine

## Overview

Payment Engine is a backend service that processes account-to-account transfers while ensuring transaction consistency, idempotency, concurrency safety, and reliable synchronization with an external Core Banking System (CBS).

The application demonstrates payment processing patterns commonly used in financial systems, including:

* Atomic balance updates
* Database transaction management
* Idempotent request handling
* Concurrent transfer protection
* Transactional Outbox Pattern
* Asynchronous CBS synchronization
* Retry and failure handling

---

## Features

### Account Management

* Create and manage customer accounts
* Maintain account balances
* Track account status

### Transfer Processing

The transfer flow supports:

* Sender and receiver validation
* Balance verification
* Concurrent transfer protection
* Duplicate request prevention
* Transaction history creation
* Event generation for CBS synchronization

### CBS Synchronization

Transfers are asynchronously synchronized with the CBS using an outbox-driven background processor.

The system supports:

* External service failure handling
* Retry with exponential backoff
* Failed event tracking
* Recovery of interrupted processing

---

# Architecture Overview

The application follows a modular monolith architecture.

```
payment-engine

├── account
│   ├── controller
│   ├── service
│   ├── repository
│   └── entity
│
├── transaction
│   ├── controller
│   ├── service
│   ├── repository
│   └── entity
│
├── outbox
│   ├── worker
│   ├── service
│   ├── repository
│   └── entity
│
├── cbs
│   └── external integration layer
│
└── common
    ├── configuration
    ├── exception handling
    └── utilities
```

---

# Transaction Processing Flow

A transfer request follows this flow:

```
Client
  |
  v
Transfer API
  |
  v
Validate Request
  |
  v
Check Existing Transaction
  |
  v
Lock Sender and Receiver Accounts
  |
  v
Validate Balance
  |
  v
Update Account Balances
  |
  v
Create Transaction Record
  |
  v
Create Outbox Event
  |
  v
Return Response
  |
  v
Background CBS Synchronization
```

The transfer and outbox creation happen within the same database transaction, ensuring that a successful transfer always has a corresponding event waiting for CBS processing.

---

# Concurrency Handling

Financial transactions require protection against race conditions when multiple requests attempt to update the same account.

The application uses pessimistic database locking.

During transfer processing:

1. Sender and receiver accounts are locked.
2. Balances are validated while locks are held.
3. Account balances are updated.
4. Locks are released after transaction completion.

To prevent deadlocks, accounts are always locked in ascending account ID order.

Example:

```
Transfer A -> B

Lock account A
Lock account B


Transfer B -> A

Lock account A
Lock account B
```

Both transactions acquire locks in the same order.

---

# Idempotency

The transfer API supports idempotent requests.

Each request contains a unique request reference.

Before processing a transfer:

1. The system checks whether the request reference already exists.
2. If found, the existing transaction response is returned.
3. Duplicate processing is prevented.

This protects the system against:

* Client retries
* Network failures
* Duplicate payment submissions

---

# Transactional Outbox Pattern

The system uses the Transactional Outbox Pattern to reliably communicate with the CBS.

Instead of calling CBS during the database transaction:

```
Transfer Request
        |
        |
        v
Database Transaction

- Update Accounts
- Create Transaction
- Create Outbox Event

        |
        v

Background Worker
        |
        v
CBS Synchronization
```

This avoids keeping database transactions open while waiting for external services.

Benefits:

* Improved reliability
* No lost events
* External service failures do not rollback successful transfers
* Supports retries

---

# CBS Failure Handling

CBS communication is handled asynchronously.

Failure scenarios:

## Temporary Failure

Examples:

* CBS timeout
* CBS unavailable

Action:

* Increment retry count
* Schedule retry
* Apply retry backoff

## Permanent Failure

After maximum retry attempts:

* Event is marked as FAILED
* Transaction remains recorded
* Manual investigation can be performed

---

# Technology Stack

| Component         | Technology                  |
| ----------------- | --------------------------- |
| Language          | Java 21                     |
| Framework         | Spring Boot 4               |
| Database          | PostgreSQL                  |
| ORM               | Spring Data JPA / Hibernate |
| Migration         | Flyway                      |
| Testing           | JUnit 5, Testcontainers     |
| API Documentation | SpringDoc OpenAPI           |
| Build Tool        | Maven                       |

---

# API Documentation

Swagger UI is available at:

```
/swagger-ui.html
```

API documentation:

```
/api-docs
```

---

# Running Locally

## Requirements

* Java 21
* Maven 3.9+
* PostgreSQL

## Database Configuration

Configure database properties:

```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```

## Run Application

Using Maven:

```
./mvnw spring-boot:run
```

Windows:

```
mvnw.cmd spring-boot:run
```

---

# Testing

The project includes automated tests covering:

## Integration Tests

Validate:

* Successful transfers
* Database persistence
* Outbox event creation

## Concurrency Tests

Validate:

* Concurrent transfer handling
* Balance consistency
* Race condition prevention

## Outbox Tests

Validate:

* CBS synchronization
* Retry behaviour
* Event processing

Tests use Testcontainers with PostgreSQL to provide a production-like database environment.

---

# Future Improvements

Possible production enhancements:

* Kafka-based event publishing
* Distributed Job runner for outbox processing
* Metrics and observability dashboards
* Transaction reconciliation service
* Enhanced fraud detection rules
