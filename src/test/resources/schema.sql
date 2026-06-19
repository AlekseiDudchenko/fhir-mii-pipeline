CREATE TABLE IF NOT EXISTS fhir_resources (
    resource_type VARCHAR(64) NOT NULL,
    resource_id   VARCHAR(64) NOT NULL,
    resource_json CLOB        NOT NULL,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resource_type, resource_id)
);
