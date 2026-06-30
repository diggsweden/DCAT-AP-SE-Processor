// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.validate.RDFValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;

public class ErrorReporterTest {

    private ErrorReporter errorReporter;
    
    @BeforeEach
    void setUp() {
        errorReporter = new ErrorReporter();
    }

    @Test
    void testThatBuildErrorReportWithNoErrorsReturnsEmpty() {
        String report = errorReporter.buildErrorReport(Map.of(), Map.of(), List.of());
        assertTrue(report.isEmpty());
    }

    @Test
    void testThatBuildErrorReportReportsExceptionsErrors() {
        String filename = "api.yaml";
        String msg = "Invalid format";

        String report = errorReporter.buildErrorReport(Map.of(filename, msg), Map.of(), List.of());

        assertTrue(report.contains(msg));
        assertTrue(report.contains(filename));
    }

    @Test
    void testThatBuildErrorReportReportsValidationErrors() {
        String value = "About";
        String filename = "catalog.yaml";
        ValidationError error = new ValidationError(ErrorType.DUPLICATE_URI_BETWEEN_FILES, new String[]{filename}, value);
        Map<String, List<ValidationError>> validationErrors = Map.of(filename, List.of(error));

        String report = errorReporter.buildErrorReport(Map.of(), validationErrors, List.of());

        assertTrue(report.contains(value));
        assertTrue(report.contains(filename));
        assertTrue(report.contains(ErrorType.DUPLICATE_URI_BETWEEN_FILES.toString()));
    }

    @Test
    void testThatBuildErrorReportReportsRDFValidationError() {
        String msg = "DCAT-AP-SE requires a publisher (dcterms:publisher) on every Dataset.";
        RDFValidationError error = new RDFValidationError();
        error.message = msg;

        String report = errorReporter.buildErrorReport(Map.of(), Map.of(), List.of(error));

        assertTrue(report.contains(msg));
    }
}
