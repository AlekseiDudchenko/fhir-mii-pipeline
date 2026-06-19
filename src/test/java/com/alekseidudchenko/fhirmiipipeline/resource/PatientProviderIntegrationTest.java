package com.alekseidudchenko.fhirmiipipeline.resource;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatientProviderIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FhirContext fhirContext;

    @Test
    void createValidPatient_returns201() {
        String body = """
                {
                  "resourceType": "Patient",
                  "meta": {
                    "profile": [
                      "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
                    ]
                  },
                  "identifier": [{
                    "type": { "coding": [{ "system": "http://fhir.de/CodeSystem/identifier-type-de-basis", "code": "GKV" }] },
                    "system": "http://fhir.de/sid/gkv/kvid-10",
                    "value": "A123456789",
                    "assigner": { "identifier": { "system": "http://fhir.de/sid/arge-ik/iknr", "value": "109500969" } }
                  }],
                  "name": [{ "family": "Mustermann", "given": ["Erika"] }],
                  "gender": "female",
                  "birthDate": "1964-08-12"
                }
                """;

        ResponseEntity<String> response = postPatient(body);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createInvalidPatient_returns422WithOperationOutcome() {
        String body = """
                {
                  "resourceType": "Patient",
                  "meta": {
                    "profile": [
                      "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
                    ]
                  },
                  "name": [{ "family": "Fehler", "given": ["Max"] }],
                  "gender": "male",
                  "birthDate": "1990-01-15"
                }
                """;

        ResponseEntity<String> response = postPatient(body);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        OperationOutcome outcome = fhirContext.newJsonParser()
                .parseResource(OperationOutcome.class, response.getBody());
        boolean hasIdentifierError = outcome.getIssue().stream()
                .anyMatch(i -> i.getSeverity() == OperationOutcome.IssueSeverity.ERROR
                        && i.getDiagnostics() != null
                        && i.getDiagnostics().toLowerCase().contains("identifier"));
        assertTrue(hasIdentifierError, "Should report missing identifier validation error");
    }

    @Test
    void createPatientWithoutProfile_stillValidatesAgainstMiiProfile() {
        String body = """
                {
                  "resourceType": "Patient",
                  "name": [{ "family": "NoProfile", "given": ["Test"] }],
                  "gender": "male",
                  "birthDate": "2000-01-01"
                }
                """;

        ResponseEntity<String> response = postPatient(body);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode(),
                "Patient without meta.profile should still be validated against MII profile");

        OperationOutcome outcome = fhirContext.newJsonParser()
                .parseResource(OperationOutcome.class, response.getBody());
        boolean hasIdentifierError = outcome.getIssue().stream()
                .anyMatch(i -> i.getSeverity() == OperationOutcome.IssueSeverity.ERROR
                        && i.getDiagnostics() != null
                        && i.getDiagnostics().toLowerCase().contains("identifier"));
        assertTrue(hasIdentifierError, "Should report missing identifier even without explicit profile");
    }

    private ResponseEntity<String> postPatient(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/fhir+json"));
        headers.set(HttpHeaders.ACCEPT, "application/fhir+json");
        return restTemplate.exchange(
                "http://localhost:" + port + "/fhir/Patient",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }
}
