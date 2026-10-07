// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.quality;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.VocabularyStringToIRI;
import se.ams.dcatprocessor.rdf.quality.QualityReport.MainClassQuality;
import se.ams.dcatprocessor.rdf.quality.QualityReport.QualityIssue;
import se.ams.dcatprocessor.specification.DcatSpecification;
import se.ams.dcatprocessor.specification.SpecificationLoader;
import se.ams.dcatprocessor.testutil.TestHelper;

public class DcatQualityCheckerTest {

    private static DcatQualityChecker checker;

    private static final String COMPLETE_RDF_FILE = "rdf/oas_301.rdf";

    @BeforeAll
    static void setup() throws IOException {
        SpecificationLoader specificationLoader = new SpecificationLoader(
                TestHelper.bundlePathFromApplicationProperties());
        checker = new DcatQualityChecker(new DcatSpecification(specificationLoader));
    }

    @Test
    void testThatCompleteRdfGivesNoIssues() throws IOException {
        String rdf = readResource(COMPLETE_RDF_FILE);
    
        QualityReport report = checker.check(rdf);
    
        for (MainClassQuality mainClass : report.mainClasses()) {
            assertTrue(mainClass.issues().isEmpty(), mainClass.uri() + " has issues: " + mainClass.issues());
        }
    }

    @Test
    void testThatEveryMainClassInstanceIsInReport() throws IOException {
        String rdf = readResource(COMPLETE_RDF_FILE);

        QualityReport report = checker.check(rdf);

        assertEquals(5, report.mainClasses().size());
    }

    @Test
    void testThatMalformedRdfThrows() {
        assertThrows(RDFParseException.class, () -> checker.check("this is not rdf"));
    }

    @Test
    void testThatShortDatasetDescriptionGivesIssue() throws IOException {
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        model.remove(dataset, DCTERMS.DESCRIPTION, null);
        model.add(dataset, DCTERMS.DESCRIPTION, Values.literal("Kort beskrivning", "sv"));
        model.add(dataset, DCTERMS.DESCRIPTION, Values.literal("A sufficiently long English description of dataset C", "en"));

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCTERMS.DESCRIPTION);

        assertTrue(hasIssue);
    }

    @Test
    void testThatLongDatasetSeriesDescriptionGivesNoIssue() throws IOException {
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        IRI datasetSeries = Values.iri("https://www.example.se/#datasetseriesC");

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, datasetSeries.stringValue(), DCTERMS.DESCRIPTION);

        assertFalse(hasIssue);
    }

    @Test
    void testThatMissingRecommendedContactPointGivesIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(dataset, DCAT.CONTACT_POINT, null);

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCAT.CONTACT_POINT);

        assertTrue(hasIssue);
    }

    @Test
    void testThatMissingRecommendedThemeGivesIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(dataset, DCAT.THEME, null);

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCAT.THEME);

        assertTrue(hasIssue);
    }

    @Test
    void testThatMissingRecommendedLicenseOnDistributionGivesIssue() throws IOException {
        IRI distribution = Values.iri("https://www.example.se/#distributionC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(distribution, DCTERMS.LICENSE, null);

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, distribution.stringValue(), DCTERMS.LICENSE);

        assertTrue(hasIssue);
    }

    @Test
    void testThatPresentLicenseOnDistributionGivesNoIssue() throws IOException {
        IRI distribution = Values.iri("https://www.example.se/#distributionC");
        IRI license = Values.iri("http://creativecommons.org/licenses/by/4.0/");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.add(distribution, DCTERMS.LICENSE, license);

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, distribution.stringValue(), DCTERMS.LICENSE);

        assertFalse(hasIssue);
    }

    @Test
    void testThatTooFewKeywordsPerLanguageHasIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(dataset, DCAT.KEYWORD, null);

        // only 2 keywords/language is not considered good practice, keywords with
        // different language tags should not be checked as one.
        model.add(dataset, DCAT.KEYWORD, Values.literal("transport", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("trafik", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("transport", "en"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("traffic", "en"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("verkehr", "de"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("transport", "de"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("liikenne", "fi"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("kuljetus", "fi"));
        
        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCAT.KEYWORD);

        assertTrue(hasIssue);
    }

    @Test
    void testThatMinimumNumberOfKeywordsGivesNoIssues() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(dataset, DCAT.KEYWORD, null);
        model.add(dataset, DCAT.KEYWORD, Values.literal("transport", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("trafik", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("väg", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("järnväg", "sv"));
        model.add(dataset, DCAT.KEYWORD, Values.literal("kollektivtrafik", "sv"));

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCAT.KEYWORD);

        assertFalse(hasIssue);
    }

    @Test
    void testThatTitleLanguageMissingInDescriptionGivesIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.add(dataset, DCTERMS.TITLE, Values.literal("Dataset C", "en"));

        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCTERMS.DESCRIPTION);

        assertTrue(hasIssue);
    }

    @Test
    void testThatDescriptionLanguageMissingInTitleGivesIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.add(dataset, DCTERMS.DESCRIPTION, Values.literal("A sufficiently long description of dataset C", "en"));
    
        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCTERMS.TITLE);
    
        assertTrue(hasIssue);
    }

    @Test
    void testThatNamedPlaceWithoutGeographicAreaGivesIssue() throws IOException {
        IRI dataset = Values.iri("https://www.example.se/#datasetC");
        IRI namedPlace = Values.iri("http://sws.geonames.org/6695072");
        Model model = toModel(readResource(COMPLETE_RDF_FILE));
        model.remove(dataset, DCTERMS.SPATIAL, null);
        model.add(dataset, DCTERMS.SPATIAL, namedPlace);
    
        QualityReport report = checker.check(toRdfXml(model));
        boolean hasIssue = hasIssue(report, dataset.stringValue(), DCTERMS.SPATIAL);
    
        assertTrue(hasIssue);
    }

    private static boolean hasIssue(QualityReport report, String uri, IRI property) {
        for (MainClassQuality mainClass : report.mainClasses()) {
            if (mainClass.uri().equals(uri)) {
                for (QualityIssue issue : mainClass.issues()) {
                    if (property.equals(VocabularyStringToIRI.getIRI(issue.property()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Model toModel(String rdf) throws IOException {
        try (StringReader reader = new StringReader(rdf)) {
            return Rio.parse(reader, "", RDFFormat.RDFXML);
        }
    }

    private static String toRdfXml(Model model) {
        StringWriter writer = new StringWriter();
        Rio.write(model, writer, RDFFormat.RDFXML);
        return writer.toString();
    }

    private static String readResource(String path) throws IOException {
        try (InputStream inputStream = DcatQualityCheckerTest.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Test resource not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
