package se.ams.dcatprocessor.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

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
        String report = errorReporter.buildErrorReport(Map.of(), Map.of());
        assertTrue(report.isEmpty());
    }

    @Test
    void testThatBuildErrorReportContainsDocsUrlWhenErrorsExist() {
        String report = errorReporter.buildErrorReport(Map.of("api.yaml", "Invalid format"), Map.of());
        assertTrue(report.contains("https://docs.dataportal.se/dcat/sv/"));
    }

    @Test
    void testThatBuildErrorReportReportsExceptionsErrors() {
        String filename = "api.yaml";
        String msg = "Invalid format";

        String report = errorReporter.buildErrorReport(Map.of(filename, msg), Map.of());

        assertTrue(report.contains(msg));
        assertTrue(report.contains(filename));
    }

    @Test
    void testThatBuildErrorReportReportsValidationErrors() {
        String value = "About";
        String filename = "catalog.yaml";
        ValidationError error = new ValidationError(ErrorType.DUPLICATE_URI_BETWEEN_FILES, new String[]{filename}, value);
        Map<String, List<ValidationError>> validationErrors = Map.of(filename, List.of(error));

        String report = errorReporter.buildErrorReport(Map.of(), validationErrors);

        assertTrue(report.contains(value));
        assertTrue(report.contains(filename));
        assertTrue(report.contains(ErrorType.DUPLICATE_URI_BETWEEN_FILES.toString()));
    }

    @Test
    void testThatBuildErrorReportReportsBothErrorTypes() {
        String file1 = "catalog.yaml";
        String file2 = "api.yaml";
        String field = "About";
        String errorMsg = "Invalid format";

        ValidationError validationError = new ValidationError(ErrorType.DUPLICATE_URI_BETWEEN_FILES, new String[]{file1}, field);
        Map<String, List<ValidationError>> validationErrors = Map.of(file1, List.of(validationError));
        Map<String, String> exceptions = Map.of(file2, errorMsg);

        String report = errorReporter.buildErrorReport(exceptions, validationErrors);

        assertTrue(report.contains(file1));
        assertTrue(report.contains(field));
        assertTrue(report.contains(ErrorType.DUPLICATE_URI_BETWEEN_FILES.toString()));
        assertTrue(report.contains(file2));
        assertTrue(report.contains(errorMsg));
    }
}
