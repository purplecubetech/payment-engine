# Payment Engine Architecture

## 1. Overview

The Payment Engine is designed as a reliable transaction processing system that prioritizes correctness, consistency, and resilience.

The main architectural goals are:

* Prevent incorrect balance updates
* Handle concurrent transaction requests safely
* Prevent duplicate transaction processing
* Ensure reliable communication with external banking systems
* Isolate database transactions from external service latency

---

# 2. High-Level Architecture

The application follows a modular monolith approach.

Although deployed as a single application, the codebase is separated into independent business modules.

```text
Payment Engine

        +----------------+
        |   REST API     |
        +----------------+
                |
                v
        +----------------+
        | Transfer       |
        | Service        |
        +----------------+
                |
        +-------+-------+
        |               |
        v               v

 Account Module     Transaction Module

                |
                v

        +----------------+
        | Outbox Module  |
        +----------------+
                |
                v

        +----------------+
        | CBS Integration|
        +----------------+
```

---

# 3. Transfer Processing Design

A transfer is processed inside a single database transaction.

The transaction performs:

1. Validate request
2. Check duplicate request
3. Lock affected accounts
4. Validate sender balance
5. Update balances
6. Create transaction record
7. Create outbox event

The database transaction guarantees:

* Account balances cannot partially update
* Transactions cannot exist without corresponding outbox events

---

# 4. Concurrency Control

## Problem

Multiple requests may attempt to update the same account simultaneously.

Example:

Account balance:

```
1000
```

Two concurrent requests:

```
Transfer 700
Transfer 500
```

Without locking, both requests may read:

```
Balance = 1000
```

causing an incorrect final balance.

---

## Solution

The application uses pessimistic database locking.

Accounts are locked before balance validation:

```text
Request A

Lock Sender
Lock Receiver

Update Balance


Request B

Wait until locks are released
```

The database guarantees that only one transaction can modify the locked account rows at a time.

---

## Deadlock Prevention

Transfers can happen in both directions:

```
Account A -> Account B

Account B -> Account A
```

To avoid deadlocks, accounts are always locked using ascending account IDs.

Example:

```text
Transfer 10 -> 20

Lock 10
Lock 20


Transfer 20 -> 10

Lock 10
Lock 20
```

Both transactions acquire locks in the same order.

---

# 5. Idempotency Design

Payment APIs must handle duplicate requests safely.

Common causes:

* Client retries
* Network timeout
* Mobile application retry logic
* Payment gateway retries

The system uses a unique request reference.

Flow:

```text
Incoming Request

       |
       v

Check request_reference

       |
       +------ Exists
       |
       v

Return existing transaction


       |
       +------ Not Exists
       |
       v

Process transfer
```

A database unique constraint provides an additional safety layer.

---

# 6. Transactional Outbox Pattern

## Problem

Calling CBS directly inside the transaction creates problems.

Example:

```text
BEGIN TRANSACTION

Debit Account

Call CBS
(wait 5 seconds)

CBS fails

ROLLBACK
```

This causes:

* Long database locks
* Poor throughput
* Dependency on external availability

---

## Solution

The system stores an outbox event in the same database transaction.

Example:

```text
BEGIN TRANSACTION

Debit Account

Create Transaction

Create Outbox Event

COMMIT
```

After committing, a background worker processes the event.

---

# 7. Outbox Processing Flow

```text
OUTBOX EVENT

PENDING
   |
   |
   v

PROCESSING

   |
   |
   v

CBS CALL

   |
   +------------+
   |            |
 SUCCESS      FAILURE
   |            |
   v            v

COMPLETED    RETRY
              |
              |
              v

          FAILED
          (after max retries)
```

---

# 8. External Service Handling

The CBS integration is intentionally outside the database transaction.

Reason:

External services are unpredictable.

They may experience:

* Network delays
* Timeouts
* Downtime
* Slow responses

Keeping database transactions open while waiting for CBS would:

* Hold database connections
* Reduce throughput
* Increase lock contention

Instead:

1. Database work completes quickly
2. CBS synchronization happens asynchronously
3. Failures are retried

---

# 9. Retry Strategy

Failed CBS requests use retry with increasing delays.

Example:

```text
Retry 1
5 seconds

Retry 2
15 seconds

Retry 3
30 seconds

Retry 4+
120 seconds
```

A small random jitter is added to avoid multiple workers retrying simultaneously.

---

# 10. Testing Strategy

The system uses automated tests with PostgreSQL Testcontainers.

## Integration Tests

Verify:

* Transfer success
* Database persistence
* Outbox creation

## Concurrency Tests

Verify:

* Multiple simultaneous transfers
* Correct final balances
* No lost updates

## Outbox Tests

Verify:

* Event processing
* CBS synchronization
* Failure handling

---

# 11. Production Considerations

Possible production extensions:

## Distributed Outbox Workers

Multiple application instances can process events using:

* Row locking
* Event claiming
* Distributed coordination

## Observability

Add:

* Metrics
* Distributed tracing
* Structured logging

---

# Conclusion

The architecture focuses on correctness first by combining database transactions, locking strategies, idempotency, and asynchronous processing patterns.

The design allows the system to remain reliable even when external dependencies such as CBS become unavailable.
