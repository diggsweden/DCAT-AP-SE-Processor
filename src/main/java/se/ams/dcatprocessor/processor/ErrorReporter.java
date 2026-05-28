// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.rdf.validate.ValidationError;

@Component
public class ErrorReporter {

    private static final String DOCS_URL = "https://docs.dataportal.se/dcat/sv/";

    public String buildErrorReport(Map<String, String> exceptions, Map<String, List<ValidationError>> validationErrors) {
        StringBuilder errors = new StringBuilder();
        StringBuilder report = new StringBuilder();

        // Errors from ApiDefinitionParser or Converters
        if (!exceptions.isEmpty()) {
            errors.append("\n");
            exceptions.forEach((key, value) -> errors.append(key).append(":\n").append(value).append("\n\n"));
        }

        // Errors from RDFWorker
        if (!validationErrors.isEmpty()) {
            errors.append("\n");
            validationErrors.forEach((key, value) -> {
                errors.append(key).append(":\n");
                for (ValidationError error : value) {
                    errors.append("Errortype: ").append(error.getErrorType())
                          .append(" Description: ").append(error.getDescription()).append("\n");
                }
                errors.append("\n");
            });
        }
        
        if(!errors.isEmpty()){
            report.append("Check DCAT-AP-SE specification for info. " + DOCS_URL + "\n---------------------------------\n");
            report.append(errors);
        }
        
        return report.toString();
    }
}
