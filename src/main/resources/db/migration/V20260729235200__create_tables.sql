CREATE TABLE accounts
(
    id BIGSERIAL PRIMARY KEY,

    account_number VARCHAR(30) NOT NULL UNIQUE,

    balance NUMERIC(19,2) NOT NULL CONSTRAINT chk_account_balance_non_negative
        CHECK (balance >= 0),

    status VARCHAR(20) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);

CREATE TABLE transactions
(
    id BIGSERIAL PRIMARY KEY,

    request_reference VARCHAR(100) NOT NULL UNIQUE,

    transaction_reference VARCHAR(100) NOT NULL UNIQUE,

    sender_account_id BIGINT NOT NULL,

    receiver_account_id BIGINT NOT NULL,

    amount NUMERIC(19,2) NOT NULL CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0),

    status VARCHAR(30) NOT NULL,

    cbs_sync_status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT fk_sender
        FOREIGN KEY (sender_account_id)
            REFERENCES accounts(id),

    CONSTRAINT fk_receiver
        FOREIGN KEY (receiver_account_id)
            REFERENCES accounts(id)
);

CREATE TABLE outbox_events
(
    id BIGSERIAL PRIMARY KEY,

    transaction_id BIGINT NOT NULL,

    event_type VARCHAR(50) NOT NULL,

    payload TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    retry_count INT NOT NULL,

    next_retry_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    processed_at TIMESTAMP,

    locked_at TIMESTAMP,

    CONSTRAINT fk_transactions
        FOREIGN KEY (transaction_id)
            REFERENCES transactions(id)
);

CREATE INDEX idx_outbox_status_retry
    ON outbox_events(status, next_retry_at);