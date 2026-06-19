package com.alekseidudchenko.fhirmiipipeline.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FhirResourceRepository {

    private final JdbcTemplate jdbcTemplate;

    public FhirResourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String resourceType, String id, String json) {
        jdbcTemplate.update(
                "DELETE FROM fhir_resources WHERE resource_type = ? AND resource_id = ?",
                resourceType, id);
        jdbcTemplate.update(
                "INSERT INTO fhir_resources (resource_type, resource_id, resource_json) VALUES (?, ?, ?)",
                resourceType, id, json);
    }

    public Optional<String> findById(String resourceType, String id) {
        var results = jdbcTemplate.queryForList(
                "SELECT resource_json FROM fhir_resources WHERE resource_type = ? AND resource_id = ?",
                String.class, resourceType, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
