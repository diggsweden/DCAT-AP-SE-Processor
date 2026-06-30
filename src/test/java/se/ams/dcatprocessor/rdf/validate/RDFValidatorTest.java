// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.testutil.TestRDFBuilder;

public class RDFValidatorTest {

    private static RDFValidator rdfValidator;

    @BeforeAll
    static void setUpAll() throws IOException {
        rdfValidator = new RDFValidator(new RDFVocabularyBuilder());
    }

    @Test
    void testThatValidRdfReturnsNoErrors() throws Exception{
        String rdf = TestRDFBuilder.minimalValidRdf().toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(result.size() == 0, "Unexpected error while validating valid RDF");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testThatMissingRdfThrowsDcatException(String rdf) {
        DcatException ex = assertThrows(DcatException.class, () -> rdfValidator.validate(rdf));
        assertTrue(ex.getMessage().contains("RDFValidator: RDF input is empty or missing"));
    }

    @Test
    void testThatInvalidRdfThrowsDcatException() {
        String invalidRdf = "<rdf:RDF></rdf:RDF>";
        DcatException ex = assertThrows(DcatException.class, () -> rdfValidator.validate(invalidRdf));
        assertTrue(ex.getMessage().contains("RDFValidator: RDF input is Invalid"));
    }

    @Test
    void testThatMissingLicenseReturnsValidationError() throws Exception{
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .remove(Values.iri("https://example.org/catalog/1"), DCTERMS.LICENSE)
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(result.size() == 1);
        assertTrue(result.getFirst().message.contains("DCAT-AP-SE requires a licence (dcterms:license) on every Catalogue"));
    }

    @Test
    void testThatMissingPublisherReturnsValidationError() throws Exception{
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .remove(Values.iri("https://example.org/dataset/1"), DCTERMS.PUBLISHER)
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(result.size() == 1);
        assertTrue(result.getFirst().message.contains("DCAT-AP-SE requires a publisher (dcterms:publisher) on every Dataset"));
    }

    @Test
    void testThatMissingTypeOnAgentReturnsValidationError() throws Exception{
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .remove(Values.iri("https://example.org/agent/1"), DCTERMS.TYPE)
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(result.size() == 1);
        assertTrue(result.getFirst().message.contains("DCAT-AP-SE requires a type (dcterms:type) on every Agent"));
    }

    @Test
    void testThatDatasetWithInvalidPublisherReturnsValidationError() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .remove(Values.iri("https://example.org/dataset/1"), DCTERMS.PUBLISHER)
            .modify(b -> b
                .subject("https://example.org/dataset/1")
                    .add(DCTERMS.PUBLISHER, Values.iri("https://example.org/nonexistent-agent")))
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(!result.isEmpty());
        RDFValidationError error = result.getFirst();
        assertTrue(error.constraintType.contains("ClassConstraintComponent"));
        assertTrue(error.path.contains("publisher"));
    }

    @Test
    void testThatAddingSecondDatasetIsValid() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .modify(b -> b
                .subject("https://example.org/catalog/1")
                    .add(DCAT.HAS_DATASET, Values.iri("https://example.org/dataset/2"))
                .subject("https://example.org/dataset/2")
                    .add(RDF.TYPE, DCAT.DATASET)
                    .add(DCTERMS.TITLE, Values.literal("Andra datasetet", "sv"))
                    .add(DCTERMS.DESCRIPTION, Values.literal("Beskrivning", "sv"))
                    .add(DCTERMS.PUBLISHER, Values.iri("https://example.org/agent/1")))
            .toRdfString();
            
        List<RDFValidationError> result = rdfValidator.validate(rdf);
            
        assertTrue(result.isEmpty(), "Expected no errors, got: " + result);
    }

    @Test
    void testThatDistributionWithoutAccessUrlReturnsValidationError() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .modify(b -> b
                .subject("https://example.org/dataset/1")
                    .add(DCAT.HAS_DISTRIBUTION, Values.iri("https://example.org/distribution/1"))
                .subject("https://example.org/distribution/1")
                    .add(RDF.TYPE, DCAT.DISTRIBUTION)
                    .add(DCTERMS.LICENSE, Values.iri("http://creativecommons.org/publicdomain/zero/1.0/")))
                    // no accessURL
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(!result.isEmpty());
        RDFValidationError error = result.getFirst();
        assertTrue(error.message.contains("Mandatory value missing"));
        assertTrue(error.constraintType.contains("MinCountConstraintComponent"));
        assertTrue(error.path.contains("accessURL"));
    }

    @Test
    void testThatMultipleViolationsAreReported() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .remove(Values.iri("https://example.org/catalog/1"), DCTERMS.LICENSE)   //mandatory
            .remove(Values.iri("https://example.org/dataset/1"), DCTERMS.PUBLISHER) //mandatory
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertEquals(2, result.size(), "Expected 2 violations, got: " + result.size());

        assertTrue(result.stream()
            .anyMatch(e -> e.message != null && e.message.contains("licence (dcterms:license)")));

        assertTrue(result.stream()
            .anyMatch(e -> e.message != null && e.message.contains("publisher (dcterms:publisher)")));
    }

    @Test
    void testThatNonLiteralIssuedGivesNodeKindViolation() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .modify(b -> b
                .subject("https://example.org/dataset/1")
                    .add(DCTERMS.ISSUED, Values.iri("https://example.org/foo")))
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(!result.isEmpty());
        RDFValidationError error = result.getFirst();
        assertTrue(error.constraintType.contains("NodeKindConstraintComponent"));
    }

    @Test
    void testThatMultipleLicensesGivesMaxCountViolation() throws Exception {
        String rdf = TestRDFBuilder
            .minimalValidRdf()
            .modify(b -> b
                .subject("https://example.org/catalog/1")
                    // add second license
                    .add(DCTERMS.LICENSE, Values.iri("http://creativecommons.org/publicdomain/zero/2.0/")))
            .toRdfString();

        List<RDFValidationError> result = rdfValidator.validate(rdf);

        assertTrue(!result.isEmpty());
        RDFValidationError error = result.getFirst();
        assertTrue(error.constraintType.contains("MaxCountConstraintComponent"));
    }
}
