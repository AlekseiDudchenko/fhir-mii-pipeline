package com.alekseidudchenko.fhirmiipipeline.resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.alekseidudchenko.fhirmiipipeline.persistence.FhirResourceRepository;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ObservationProvider implements IResourceProvider {

    private final FhirContext fhirContext;
    private final FhirResourceRepository repository;

    public ObservationProvider(FhirContext fhirContext, FhirResourceRepository repository) {
        this.fhirContext = fhirContext;
        this.repository = repository;
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

    @Create
    public MethodOutcome create(@ResourceParam Observation observation) {
        String id = UUID.randomUUID().toString();
        observation.setId(id);

        String json = fhirContext.newJsonParser().encodeResourceToString(observation);
        repository.save("Observation", id, json);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Observation", id));
        outcome.setCreated(true);
        outcome.setResource(observation);
        return outcome;
    }

    @Read
    public Observation read(@IdParam IdType id) {
        String json = repository.findById("Observation", id.getIdPart())
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return fhirContext.newJsonParser().parseResource(Observation.class, json);
    }
}
