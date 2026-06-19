package com.alekseidudchenko.fhirmiipipeline.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FhirProfileValidatorTest {

    private static FhirProfileValidator validator;
    private static FhirContext ctx;

    @BeforeAll
    static void setUp() {
        ctx = FhirContext.forR4();
        validator = new FhirProfileValidator(ctx);
    }

    @Test
    void validMiiPatient_passesValidation() {
        Patient patient = buildValidMiiPatient();

        ValidationResult result = validator.validate(patient);

        assertTrue(result.isSuccessful(),
                "Valid MII Patient should pass validation, but got: "
                        + ctx.newJsonParser().setPrettyPrint(true)
                        .encodeResourceToString(result.toOperationOutcome()));
    }

    @Test
    void patientWithoutIdentifier_failsValidation() {
        Patient patient = new Patient();
        patient.getMeta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
        patient.addName().setFamily("Fehler").addGiven("Max");
        patient.setGender(Enumerations.AdministrativeGender.MALE);
        patient.setBirthDateElement(new DateType("1990-01-15"));

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isSuccessful(), "Patient without identifier should fail validation");
        OperationOutcome outcome = (OperationOutcome) result.toOperationOutcome();
        boolean hasIdentifierError = outcome.getIssue().stream()
                .anyMatch(issue -> issue.getSeverity() == OperationOutcome.IssueSeverity.ERROR
                        && issue.getDiagnostics() != null
                        && issue.getDiagnostics().toLowerCase().contains("identifier"));
        assertTrue(hasIdentifierError, "Should report missing identifier error");
    }

    @Test
    void patientWithoutName_failsValidation() {
        Patient patient = new Patient();
        patient.getMeta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
        addGkvIdentifier(patient);
        patient.setGender(Enumerations.AdministrativeGender.MALE);
        patient.setBirthDateElement(new DateType("1990-01-15"));

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isSuccessful(), "Patient without name should fail validation");
    }

    @Test
    void patientWithoutGender_failsValidation() {
        Patient patient = new Patient();
        patient.getMeta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
        addGkvIdentifier(patient);
        patient.addName().setFamily("Mustermann").addGiven("Erika");
        patient.setBirthDateElement(new DateType("1964-08-12"));

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isSuccessful(), "Patient without gender should fail validation");
    }

    @Test
    void patientWithoutBirthDate_failsValidation() {
        Patient patient = new Patient();
        patient.getMeta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
        addGkvIdentifier(patient);
        patient.addName().setFamily("Mustermann").addGiven("Erika");
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);

        ValidationResult result = validator.validate(patient);

        assertFalse(result.isSuccessful(), "Patient without birthDate should fail validation");
    }

    @Test
    void patientWithoutProfile_passesBaseValidation() {
        Patient patient = new Patient();
        patient.addName().setFamily("Simple").addGiven("Test");

        ValidationResult result = validator.validate(patient);

        assertTrue(result.isSuccessful(),
                "Patient without MII profile should pass base FHIR validation");
    }

    private Patient buildValidMiiPatient() {
        Patient patient = new Patient();
        patient.getMeta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient");
        addGkvIdentifier(patient);
        patient.addName().setFamily("Mustermann").addGiven("Erika");
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);
        patient.setBirthDateElement(new DateType("1964-08-12"));
        return patient;
    }

    private void addGkvIdentifier(Patient patient) {
        Identifier gkvId = patient.addIdentifier();
        gkvId.getType().addCoding()
                .setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis")
                .setCode("GKV");
        gkvId.setSystem("http://fhir.de/sid/gkv/kvid-10");
        gkvId.setValue("A123456789");
        gkvId.getAssigner().getIdentifier()
                .setSystem("http://fhir.de/sid/arge-ik/iknr")
                .setValue("109500969");
    }
}
