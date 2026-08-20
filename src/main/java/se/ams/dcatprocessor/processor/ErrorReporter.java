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
        StringBuilder report = new StringBuilder();

        // Errors from ApiDefinitionParser, Converters or general errors
        if (!exceptions.isEmpty()) {
            report.append("ERROR - Failed to process API specification\n");
            report.append("-------------------------------------------\n");
            report.append("\n");

            exceptions.forEach((key, value) -> {
                report.append(key).append(":\n").append(value).append("\n\n");  
            });
        }

        // Errors from RDFWorker
        else if (!validationErrors.isEmpty()) {
            report.append("There are Errors in the following files:\n");
            report.append("Check DCAT-AP-SE specification for info\n");
            report.append(DOCS_URL + "\n");
            report.append("---------------------------------------\n");

            report.append("\n");
            validationErrors.forEach((key, value) -> {
                report.append(key).append(":\n");
                for (ValidationError error : value) {
                    report.append("Errortype: ").append(error.getErrorType());
                    report.append(" Description: ").append(error.getDescription() + "\n");
                }
                report.append("\n");
            });
        }
   
        return report.toString();
    }
}
