CREATE TABLE IF NOT EXISTS fhir_resources (
    resource_type VARCHAR(64) NOT NULL,
    resource_id   VARCHAR(64) NOT NULL,
    resource_json JSONB       NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (resource_type, resource_id)
);
