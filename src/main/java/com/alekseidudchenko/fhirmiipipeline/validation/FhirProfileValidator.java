package com.alekseidudchenko.fhirmiipipeline.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class FhirProfileValidator {

    private final FhirValidator validator;

    public FhirProfileValidator(FhirContext fhirContext) {
        NpmPackageValidationSupport npmSupport = new NpmPackageValidationSupport(fhirContext);
        try {
            npmSupport.loadPackageFromClasspath("classpath:packages/de.medizininformatikinitiative.kerndatensatz.person-2025.0.0.tgz");
            npmSupport.loadPackageFromClasspath("classpath:packages/de.basisprofil.r4-1.5.0.tgz");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load FHIR profile packages", e);
        }

        ValidationSupportChain supportChain = new ValidationSupportChain(
                npmSupport,
                new DefaultProfileValidationSupport(fhirContext),
                new CommonCodeSystemsTerminologyService(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext)
        );

        CachingValidationSupport cachingSupport = new CachingValidationSupport(supportChain);

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(cachingSupport);
        instanceValidator.setNoTerminologyChecks(true);

        this.validator = fhirContext.newValidator();
        this.validator.registerValidatorModule(instanceValidator);
    }

    public ValidationResult validate(Resource resource) {
        return validator.validateWithResult(resource);
    }
}
