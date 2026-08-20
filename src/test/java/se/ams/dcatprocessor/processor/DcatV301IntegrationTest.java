// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.nio.file.Path;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import se.ams.dcatprocessor.converter.Converter;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;
import se.ams.dcatprocessor.testutil.TestHelper;

// Integrationtests for new properties and primary class when upgrading DCAT-AP-SE version to 3.0.1 (from 2.0)

@SpringBootTest 
public class DcatV301IntegrationTest {

    @Autowired
    private ObjectProvider<Manager> managerProvider;

    private Manager manager;
    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final String API_DEF_FILE = "src/test/resources/apidef/json_v3/json_oas_301.json";

    @BeforeEach
	void setup() throws Exception {
        TestHelper.resetSingeltons();
        Converter.errors.clear();
		manager = managerProvider.getObject();
	}

    @ParameterizedTest
    @CsvSource({
        "description,       http://purl.org/dc/terms/",
        "identifier,        http://purl.org/dc/terms/",
        "classification,    http://www.w3.org/ns/org#",
        "sameAs,            http://www.w3.org/2002/07/owl#",
    })
    void testThatNewAgentFieldsArePresentOnAllAgents (String fieldname, String namespace) throws Exception {   
        // 4 types of agents, identified with their about-URI
        IRI catalogPublisher = vf.createIRI("https://www.example.se/result.rdf#publisher");
        IRI datasetCreator = vf.createIRI("https://www.example.se/#creatorC");
        IRI attributionAgent = vf.createIRI("https://www.example.se/#other_agentC");
        IRI dataServicePublisher = vf.createIRI("https://www.example.se/#publisherC2");
        IRI field = vf.createIRI(namespace, fieldname);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result); 
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(catalogPublisher, field, null),"Catalog publisher missing: " + fieldname);
        assertTrue(model.contains(datasetCreator, field, null),"Dataset creator missing: " + fieldname);
        assertTrue(model.contains(attributionAgent, field, null),"Attribution-agent missing: " + fieldname);
        assertTrue(model.contains(dataServicePublisher, field, null),"DataService publisher missing: " + fieldname);
    }
    
    @Test
    void testThatCreatorAgentHasSameAs() throws Exception {
        IRI creator = vf.createIRI("https://www.example.se/#creatorC");
        IRI expectedSameAs = vf.createIRI("https://www.wikidata.org/wiki/Q123456");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(creator, OWL.SAMEAS, expectedSameAs), "Creator missing owl:sameAs");
    }

    @Test
    void testThatCreatorAgentHasClassification() throws Exception {
        IRI creator = vf.createIRI("https://www.example.se/#creatorC");
        IRI expectedClassification = vf.createIRI("http://purl.org/adms/publishertype/Company");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(creator, ORG.CLASSIFICATION, expectedClassification), "Creator missing org:classification, or wrong value");
    }

    @Test
    void testThatCreatorAgentHasDescription() throws Exception {
        IRI creator = vf.createIRI("https://www.example.se/#creatorC");
        Literal expectedDescription = vf.createLiteral("Beskrivning av skaparen (ny agent-egenskap i 3.0.1)", "sv");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(creator, DCTERMS.DESCRIPTION, expectedDescription), "Creator missing dcterms:description, or wrong value");
    }

    @Test
    void testThatCreatorAgentHasIdentifier() throws Exception {
        IRI creator = vf.createIRI("https://www.example.se/#creatorC");
        Literal expectedIdentifier = vf.createLiteral("556677-8899");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(creator, DCTERMS.IDENTIFIER, expectedIdentifier), "Creator missing dcterms:identifier, or wrong value");
    }

    @Test
    void testThatOrganizationsContainsHasURL() throws Exception {
        IRI datasetContact = vf.createIRI("https://www.example.se/#contactC");
        IRI dataServiceContact = vf.createIRI("https://www.example.se/#contactPointC2");
        IRI expectedDatasetContact = vf.createIRI("https://www.example.se/kontakt");
        IRI expectedDataServiceContact = vf.createIRI("https://www.organization2.se");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(datasetContact, VCARD4.HAS_URL, expectedDatasetContact),     "Dataset contactPoint: missing vcard:hasURL, or wrong value");
        assertTrue(model.contains(dataServiceContact, VCARD4.HAS_URL, expectedDataServiceContact), "DataService contactPoint: missing vcard:hasURL, or wrong value");
    }

    @Test
    void testThatDistributionContainsApplicableLegislation() throws Exception {
        IRI applicableLegislation = vf.createIRI("http://data.europa.eu/r5r#", "applicableLegislation");
        IRI distribution = vf.createIRI("https://www.example.se/#distributionC");
        IRI expectedApplicableLegislation = vf.createIRI("http://data.europa.eu/eli/reg_impl/2023/138/oj");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(distribution, applicableLegislation, expectedApplicableLegislation), "Distribution missing: dcatap:applicableLegislation, or wrong value");
    }

    @ParameterizedTest
    @CsvSource({
        "subject,                   http://purl.org/dc/terms/,      https://dataportal.se/concepts/grunddata/person",
        "subject,                   http://purl.org/dc/terms/,      https://dataportal.se/concepts/grunddata/foretag",
        "hvdCategory,               http://data.europa.eu/r5r#,     http://data.europa.eu/bna/c_a9135398",
        "hvdCategory,               http://data.europa.eu/r5r#,     http://data.europa.eu/bna/c_dd313021",
        "applicableLegislation,     http://data.europa.eu/r5r#,     http://data.europa.eu/eli/reg_impl/2023/138/oj",
        "inSeries,                  http://www.w3.org/ns/dcat#,     https://www.example.se/#datasetseriesC",
    })
    void testThatDatasetContainsNewUriFields(String fieldname, String namespace, String expectedValue) throws Exception {
        IRI field = vf.createIRI(namespace, fieldname);
        IRI dataset = vf.createIRI("https://www.example.se/#datasetC");
        IRI expected = vf.createIRI(expectedValue);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);    
        assertTrue(model.contains(dataset, field, expected),"Dataset missing: " + fieldname + ", or wrong value");
    }
    
    @Test
    void testThatDatasetContainsVersion() throws Exception {
        IRI dataset = vf.createIRI("https://www.example.se/#datasetC");
        Literal expectedVersion = vf.createLiteral("3.7.5");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(dataset, DCAT.VERSION, expectedVersion),"Dataset missing dcat:version, or wrong value");
    }

    @Test
    void testThatDataserviceContainsFormat() throws Exception { 
        IRI dataService = vf.createIRI("https://www.example.se/#dataserviceC");
        Literal expected = vf.createLiteral("application/json");

        String result = manager.createDcatFromFile(API_DEF_FILE);
        
        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(dataService, DCTERMS.FORMAT, expected),"Dataservice missing dcterms:format, or wrong value");
    }

    @ParameterizedTest
    @CsvSource({
        "subject,                   http://purl.org/dc/terms/,      https://dataportal.se/concepts/grunddata/person",
        "hvdCategory,               http://data.europa.eu/r5r#,     http://data.europa.eu/bna/c_a9135398",
        "applicableLegislation,     http://data.europa.eu/r5r#,     http://data.europa.eu/eli/reg_impl/2023/138/oj",
    })
    void testThatDataServiceContainsNewUriFields (String fieldname, String namespace, String expectedValue) throws Exception {
        IRI field = vf.createIRI(namespace, fieldname);
        IRI dataService = vf.createIRI("https://www.example.se/#dataserviceC");
        IRI expected = vf.createIRI(expectedValue);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(dataService, field, expected),"Dataservice missing: " + fieldname + ", or wrong value");
    }

    @Test
    void testThatDatasetSeriesIsTypedAsDatasetSeries() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        
        String result = manager.createDcatFromFile(API_DEF_FILE);
        
        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, RDF.TYPE, DCAT.DATASET_SERIES),"Model should contain dcat:DatasetSeries");
        assertFalse(model.contains(series, RDF.TYPE, DCAT.DATASET),"DatasetSeries should not be type dcat:Dataset");
    }

    @ParameterizedTest
    @CsvSource({
        "sv, Datamängdsserie C",
        "en, Dataset series C",
    })
    void testThatDatasetSeriesContainsLocalizedTitle(String languageKey, String expectedValue) throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        Literal expected = vf.createLiteral(expectedValue, languageKey);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCTERMS.TITLE, expected), "DatasetSeries missing title (" + languageKey + ")");
    }

    @ParameterizedTest
    @CsvSource({
        "sv, Exempel av Datasetseries (ny primärklass i 3.0.1)",
        "en, Example of Datasetseries (new primaryclass in 3.0.1)",
    })
    void testThatDatasetSeriesContainsLocalizedDescription(String languageKey, String expectedValue) throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        Literal expected = vf.createLiteral(expectedValue, languageKey);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCTERMS.DESCRIPTION, expected), "DatasetSeries missing description (" + languageKey + ")");
    }

    @ParameterizedTest
    @CsvSource({
        "sv, serie",
        "sv, exempel",
        "en, series",
        "en, example",
    })
    void testThatDatasetSeriesContainsLocalizedKeyword(String languageKey, String expectedValue) throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        Literal expected = vf.createLiteral(expectedValue, languageKey);

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCAT.KEYWORD, expected), "DatasetSeries missing keyword (" + languageKey + ")");
    }

    @ParameterizedTest
    @CsvSource({
        "theme,                 http://www.w3.org/ns/dcat#,     http://publications.europa.eu/resource/authority/data-theme/TRAN",
        "landingPage,           http://www.w3.org/ns/dcat#,     https://www.example.se/serie-landningssida",
        "hvdCategory,           http://data.europa.eu/r5r#,     http://data.europa.eu/bna/c_a9135398",
        "hvdCategory,           http://data.europa.eu/r5r#,     http://data.europa.eu/bna/c_dd313021",
        "applicableLegislation, http://data.europa.eu/r5r#,     http://data.europa.eu/eli/reg_impl/2023/138/oj",
        "spatial,               http://purl.org/dc/terms/,      http://sws.geonames.org/6695072",
        "subject,               http://purl.org/dc/terms/,      https://dataportal.se/concepts/grunddata/foretag",
        "relation,              http://purl.org/dc/terms/,      https://www.example.se/relaterad-resurs",
        "accrualPeriodicity,    http://purl.org/dc/terms/,      http://publications.europa.eu/resource/authority/frequency/ANNUAL",
    })
    void testThatDatasetSeriesContainsUriField(String fieldname, String namespace, String expectedValue) throws Exception {
        IRI field = vf.createIRI(namespace, fieldname);
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        IRI expected = vf.createIRI(expectedValue);

        String result = manager.createDcatFromFile(API_DEF_FILE);
        
        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, field, expected), "Datasetseries missing " + fieldname + ", or wrong value");
    }

    @ParameterizedTest
    @CsvSource({
        "issued,    2021-02-01",
        "modified,  2021-02-15",
    })
    void testThatDatasetSeriesContainsDateField(String fieldname, String expectedValue) throws Exception {
        IRI field = vf.createIRI(DCTERMS.NAMESPACE, fieldname);
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        Literal expected = vf.createLiteral(expectedValue, XSD.DATE);

        String result = manager.createDcatFromFile(API_DEF_FILE);
        
        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, field, expected), "Datasetseries missing " + fieldname + ", or wrong value");
    }

    @Test
    void testThatDatasetSeriesContainsContactPoint() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        IRI contact = vf.createIRI("https://www.example.se/#dsseriesContact");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCAT.CONTACT_POINT, contact), "missing dcat:contactPoint");
        assertTrue(model.contains(contact, VCARD4.FN, vf.createLiteral("Kontakt för serie")), "missing vcard:fn");
        assertTrue(model.contains(contact, VCARD4.HAS_EMAIL, vf.createIRI("mailTo:serie@exempel.se")), "missing vcard:hasEmail");
        assertTrue(model.contains(contact, VCARD4.HAS_URL, vf.createIRI("https://www.example.se/serie-kontakt")), "missing vcard:hasURL");
        // Telephone and address are blank nodes — verify presence of link
        assertTrue(model.contains(contact, VCARD4.HAS_TELEPHONE, null), "missing vcard:hasTelephone");
        assertTrue(model.contains(contact, VCARD4.HAS_ADDRESS, null), "missing vcard:hasAddress");
    }

    @Test
    void testThatDatasetSeriesContainsTemporal() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
    
        String result = manager.createDcatFromFile(API_DEF_FILE);
    
        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        Resource period = (Resource) model.filter(series, DCTERMS.TEMPORAL, null).objects().iterator().next();  // Hämta temporal-noden och verifiera start/end
        assertTrue(model.contains(period, DCAT.START_DATE, vf.createLiteral("2021-01-01", XSD.DATE)),"Temporal missing dcat:startDate, or wrong value");
        assertTrue(model.contains(period, DCAT.END_DATE, vf.createLiteral("2021-12-31", XSD.DATE)),"Temporal missing dcat:endDate, or wrong value");
    }

    @Test
    void testThatDatasetSeriesContainsConformsTo() throws Exception {
        IRI conformsTo = vf.createIRI("https://www.example.se/#datasetseriesC/conformsTo");
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCTERMS.CONFORMS_TO, conformsTo), "DatasetSeries missing dcterms:conformsTo");
        assertTrue(model.contains(conformsTo, DCTERMS.TITLE, vf.createLiteral("Standard för serie", "sv")), "Standard missing dcterms:title, or wrong value");
        assertTrue(model.contains(conformsTo, DCTERMS.DESCRIPTION, vf.createLiteral("Beskrivning av standard för serie", "sv")),"Standard missing dcterms:description, or wrong value");
    }

    @Test
    void testThatDatasetSeriesContainsQualifiedRelation() throws Exception {
        IRI expectedRole = vf.createIRI("http://inspire.ec.europa.eu/metadata-codelist/ResponsiblePartyRole/distributor");
        IRI expectedRelation = vf.createIRI("https://www.example.se/relaterad-resurs-i-relation");
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        Resource relationship = (Resource) model.filter(series, DCAT.QUALIFIED_RELATION, null).objects().iterator().next();
        assertTrue(model.contains(relationship, DCAT.HAD_ROLE, expectedRole),"Relationship missing dcat:hadRole, or wrong value");
        assertTrue(model.contains(relationship, DCTERMS.RELATION, expectedRelation),"Relationship missing dcterms:relation, or wrong value");
    }

    @Test
    void testThatDatasetSeriesContainsPage() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        IRI doc = vf.createIRI("https://www.example.se/#datasetseriesC/page");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, FOAF.PAGE, doc), "DatasetSeries missing foaf:page");
        assertTrue(model.contains(doc, DCTERMS.TITLE, vf.createLiteral("Dokument för serie", "sv")),"Document missing dcterms:title, or wrong value");
        assertTrue(model.contains(doc, DCTERMS.DESCRIPTION, vf.createLiteral("Beskrivning av dokument för serie", "sv")),"Document missing dcterms:description, or wrong value");
    }

    @Test
    void testThatDatasetSeriesContainsSpatial() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        IRI spatial = vf.createIRI("https://www.example.se/#datasetseriesC/spatial");
        Literal expectedBbox = vf.createLiteral("POLYGON ((12.42 56.3, 13.24 56.3, 13.24 56.5, 12.42 56.5, 12.42 56.3))");
        Literal expectedCentroid = vf.createLiteral("POINT (12.83 56.4)");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCTERMS.SPATIAL, spatial),"DatasetSeries missing nested dcterms:spatial (geographical area)");
        assertTrue(model.contains(spatial, DCAT.BBOX, expectedBbox),"Location resource missing dcat:bbox, or wrong value");
        assertTrue(model.contains(spatial, DCAT.CENTROID, expectedCentroid),"Location resource missing dcat:centroid, or wrong value");
    }

    // Publisher is set from the Catalog's publisher.
    // If a publisher is specified on the DatasetSeries it is ignored — same logic as DataSet
    // (see RDFWorker.createDatasetSeries(), which links the catalog agent, not the series' own).
    @Test
    void testThatDatasetSeriesHasCatalogPublisher() throws Exception {
        IRI series = vf.createIRI("https://www.example.se/#datasetseriesC");
        IRI expectedPublisher = vf.createIRI("https://www.example.se/result.rdf#publisher");

        String result = manager.createDcatFromFile(API_DEF_FILE);

        assertTrue(result.contains("RDF"), result);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);
        assertTrue(model.contains(series, DCTERMS.PUBLISHER, expectedPublisher),"DatasetSeries should have the catalog publisher");
    }

    // Mandatory language fields on the dataset series. Error is stored in ValidationErrorStorage
    @ParameterizedTest
    @ValueSource(strings = {"title", "description"})
    void testThatMissingMandatoryLanguageFieldGeneratesError(String field, @TempDir Path tempDir) throws Exception {
        Path modified = TestHelper.copyWith(Path.of(API_DEF_FILE), tempDir,
            json -> {
                JSONObject datasetseries = json.getJSONObject("info")
                    .getJSONObject("x-dcat")
                    .getJSONObject("dcat-datasetseries");

                datasetseries.remove(field + "-sv");
                datasetseries.remove(field + "-en");
            });

        manager.createDcatFromFile(modified.toString());
        ValidationError validationError = ValidationErrorStorage.getInstance().getValidationErrors().get(modified.toString()).getFirst();

        assertEquals("dcterms:" + field, validationError.getKey());
        assertEquals("The key dcterms:" + field + " occurs 0 times but the allowed range is 1..n", validationError.getDescription());
    }

    // Email is mandatory in DatasetSeries.Contactpoint. Error is stored in Converter.errors
    @Test
    void testThatMissingEmailInContactpointGeneratesError(@TempDir Path tempDir) throws Exception {
        Path modified = TestHelper.copyWith(Path.of(API_DEF_FILE), tempDir,
            json -> {
                json.getJSONObject("info")
                    .getJSONObject("x-dcat")
                    .getJSONObject("dcat-datasetseries")
                    .getJSONObject("contactPoint").remove("email");
            });

        manager.createDcatFromFile(modified.toString());

        String error = Converter.errors.get(0);
        assertEquals("Errormessage: email in contactPoint is Mandatory", error);
    }
}
