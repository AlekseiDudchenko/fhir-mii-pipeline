package com.alekseidudchenko.fhirmiipipeline.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.ServletException;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirServletConfig {

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServletRegistration(FhirContext fhirContext) {
        RestfulServer server = new RestfulServer(fhirContext) {
            @Override
            protected void initialize() throws ServletException {
            }
        };

        ServletRegistrationBean<RestfulServer> registration = new ServletRegistrationBean<>(server, "/fhir/*");
        registration.setName("fhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
