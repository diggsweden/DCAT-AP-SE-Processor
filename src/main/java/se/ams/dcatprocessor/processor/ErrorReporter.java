// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.rdf.validate.ValidationError;

@Component
public class ErrorReporter {

    private static final String DOCS_URL = "https://docs.dataportal.se/dcat/sv/";

    /** Key used in the exceptions map for errors that don't originate from a specific input file. */
    public static final String GENERIC_ERROR_KEY = "generic_error";

    public String buildErrorReport(Map<String, String> exceptions, List<ValidationError> validationErrors) {
        StringBuilder report = new StringBuilder();

        appendSystemErrors(report, exceptions);
        appendValidationErrors(report, validationErrors);

        return report.toString();
    }

    /** Errors from ApiDefinitionParser or unexpected exceptions */
    private void appendSystemErrors(StringBuilder report, Map<String, String> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }

        report.append("ERROR - Could not generate DCAT").append("\n")
            .append("-".repeat(52)).append("\n");

        exceptions.forEach((fileName, message) -> {
            report.append("\n");
            appendFileName(report, fileName);
            report.append(message).append("\n");
        });
        report.append("\n");
    }

    private void appendValidationErrors(StringBuilder report, List<ValidationError> validationErrors) {
        if (validationErrors.isEmpty()) {
            return;
        }

        report.append("ERROR - Validation of API specification failed").append("\n")
                .append("Check the DCAT-AP-SE specification for info\n")
                .append(DOCS_URL).append("\n")
                .append("-".repeat(70)).append("\n");

        Map<String, List<ValidationError>> validationErrorsPerFile = groupByFileName(validationErrors);

        for (Map.Entry<String, List<ValidationError>> entry : validationErrorsPerFile.entrySet()) {
            report.append("\n");
            appendFileName(report, entry.getKey());

            for (ValidationError error : entry.getValue()) {
                String section = error.getSection();
                String label = (section != null && !section.isEmpty()) ? section : toLabel(error.getErrorType());
                appendErrorLine(report, label, error.getKey(), error.getDescription());
            }
        }
        report.append("\n");
    }

    private void appendErrorLine(StringBuilder report, String label, String path, String description) {
        if (label != null && !label.isEmpty()) {
            report.append(label).append(": ");
        }
        if (path != null && !path.isEmpty()) {

            // Don´t write the section name twice
            // Example: Dataset: Dataset.dcterms:title -> Dataset: dcterms:title
            if (label != null && path.startsWith(label + ".")) {
                path = path.substring(label.length() + 1);
            }
            report.append(path).append(" — ");
        }
        report.append(description).append("\n");
    }

    /** Filename is omitted when input did not come from a file */
    private void appendFileName(StringBuilder report, String fileName) {
        if (!fileName.equals(GENERIC_ERROR_KEY)) {
            report.append(fileName).append(":\n");
        }
    }

    /** ILLEGAL_FORMAT -> "Illegal format" */
    private String toLabel(ValidationError.ErrorType errorType) {
        String words = errorType.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    private Map<String, List<ValidationError>> groupByFileName(List<ValidationError> validationErrors) {
        Map<String, List<ValidationError>> errorsByFile = new LinkedHashMap<>();
        for (ValidationError error : validationErrors) {
            List<ValidationError> errorsForFile = errorsByFile.get(error.getFileName());
            if (errorsForFile == null) {
                errorsForFile = new ArrayList<>();
                errorsByFile.put(error.getFileName(), errorsForFile);
            }
            errorsForFile.add(error);
        }
        return errorsByFile;
    }
}