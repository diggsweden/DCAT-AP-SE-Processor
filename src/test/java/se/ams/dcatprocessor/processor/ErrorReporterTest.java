// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        String report = errorReporter.buildErrorReport(Map.of(), new ArrayList<>());
        assertTrue(report.isEmpty());
    }

    @Test
    void testThatBuildErrorReportReportsExceptionsErrors() {
        String filename = "api.yaml";
        String msg = "Invalid format";

        String report = errorReporter.buildErrorReport(Map.of(filename, msg), new ArrayList<>());

        assertTrue(report.contains(msg));
        assertTrue(report.contains(filename));
    }

    @Test
    void testThatBuildErrorReportReportsValidationErrors() {
        String value = "About";
        String filename = "catalog.yaml";
        ValidationError error = new ValidationError(ErrorType.DUPLICATE_URI_BETWEEN_FILES, new String[]{filename}, value);

        String report = errorReporter.buildErrorReport(Map.of(), List.of(error));

        assertTrue(report.contains(value));
        assertTrue(report.contains(filename));
        assertTrue(report.contains("Duplicate uri between files"));
    }
}
