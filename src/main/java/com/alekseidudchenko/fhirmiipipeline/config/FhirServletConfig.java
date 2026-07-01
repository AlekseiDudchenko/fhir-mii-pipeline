package com.alekseidudchenko.fhirmiipipeline.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import com.alekseidudchenko.fhirmiipipeline.resource.ObservationProvider;
import com.alekseidudchenko.fhirmiipipeline.resource.PatientProvider;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class FhirServletConfig {

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServletRegistration(
            FhirContext fhirContext,
            PatientProvider patientProvider,
            ObservationProvider observationProvider,
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {

        RestfulServer server = new RestfulServer(fhirContext) {
            @Override
            protected void initialize() throws ServletException {
                registerProvider(patientProvider);
                registerProvider(observationProvider);
                registerInterceptor(new CorsInterceptor(buildCorsConfig(allowedOrigins)));
            }
        };

        ServletRegistrationBean<RestfulServer> registration = new ServletRegistrationBean<>(server, "/fhir/*");
        registration.setName("fhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    private static CorsConfiguration buildCorsConfig(String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept", "Origin", "X-Requested-With", "x-fhir-starter"));
        config.setExposedHeaders(List.of("Location", "Content-Location", "ETag", "Last-Modified"));
        return config;
    }
}
