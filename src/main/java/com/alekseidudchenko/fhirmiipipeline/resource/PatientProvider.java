package com.alekseidudchenko.fhirmiipipeline.resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.ValidationResult;
import com.alekseidudchenko.fhirmiipipeline.persistence.FhirResourceRepository;
import com.alekseidudchenko.fhirmiipipeline.validation.FhirProfileValidator;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PatientProvider implements IResourceProvider {

    private static final String MII_PATIENT_PROFILE =
            "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient";

    private final FhirContext fhirContext;
    private final FhirResourceRepository repository;
    private final FhirProfileValidator profileValidator;

    public PatientProvider(FhirContext fhirContext, FhirResourceRepository repository,
                           FhirProfileValidator profileValidator) {
        this.fhirContext = fhirContext;
        this.repository = repository;
        this.profileValidator = profileValidator;
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam Patient patient) {
        if (!patient.getMeta().hasProfile(MII_PATIENT_PROFILE)) {
            patient.getMeta().addProfile(MII_PATIENT_PROFILE);
        }

        ValidationResult validationResult = profileValidator.validate(patient);
        if (!validationResult.isSuccessful()) {
            OperationOutcome outcome = (OperationOutcome) validationResult.toOperationOutcome();
            throw new UnprocessableEntityException(fhirContext, outcome);
        }

        String id = UUID.randomUUID().toString();
        patient.setId(id);

        String json = fhirContext.newJsonParser().encodeResourceToString(patient);
        repository.save("Patient", id, json);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", id));
        outcome.setCreated(true);
        outcome.setResource(patient);
        return outcome;
    }

    @Read
    public Patient read(@IdParam IdType id) {
        String json = repository.findById("Patient", id.getIdPart())
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return fhirContext.newJsonParser().parseResource(Patient.class, json);
    }
}
