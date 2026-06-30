// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

// Builds a supporting RDF graph of controlled vocabulary type assertions
// used during SHACL validation of DCAT-AP-SE output.
//
// Vocabularies available as official RDF distributions are loaded from
// files in VOCABULARY_FILES. Vocabularies without a usable distribution
// are hardcoded in the add..() methods.
@Component
public class RDFVocabularyBuilder {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static final String BASE_PATH = "shapes/vocabularies/";
    
    // Official RDF distributions of EU Publications Office authority tables.
    // These files were committed as-is.
    // Refresh manually when EU publishes a new version.
    private static final List<String> VOCABULARY_FILES = List.of(
        "language.rdf",
        "file-type.rdf",
        "frequency.rdf",
        "data-theme-skos.rdf",
        "access-right-skos.rdf",
        "distribution-status-skos.rdf"
    );

    private static final Map<String, IRI> SCHEME_BRIDGE_TYPES = Map.ofEntries(
        Map.entry("http://publications.europa.eu/resource/authority/language",              DCTERMS.LINGUISTIC_SYSTEM),
        Map.entry("http://publications.europa.eu/resource/authority/access-right",          DCTERMS.RIGHTS_STATEMENT),
        Map.entry("http://publications.europa.eu/resource/authority/frequency",             DCTERMS.FREQUENCY),
        Map.entry("http://publications.europa.eu/resource/authority/licence",               DCTERMS.LICENSE_DOCUMENT),
        Map.entry("http://publications.europa.eu/resource/authority/file-type",             DCTERMS.MEDIA_TYPE_OR_EXTENT),
        Map.entry("http://publications.europa.eu/resource/authority/data-theme",            SKOS.CONCEPT),
        Map.entry("http://publications.europa.eu/resource/authority/distribution-status",   SKOS.CONCEPT)
    );

    public Model build() throws IOException {
        Model model = new LinkedHashModel();

        applyVocabulariesFromFiles(model);
        applySchemeBridges(model);
        addPlannedAvailability(model);
        addAdmsPublisherTypes(model);
        addInspireResponsiblePartyRoles(model);
        addSpdxChecksumAlgorithms(model);

        return model;
        
    }

    private void applyVocabulariesFromFiles(Model target) throws IOException {
        for (String filename : VOCABULARY_FILES) {
            ClassPathResource resource = new ClassPathResource(BASE_PATH + filename);
            if (!resource.exists()) {
                throw new IOException("Required vocabulary file not found on classpath: " + BASE_PATH + filename);
            }
            try (InputStream in = resource.getInputStream()) {
                Rio.parse(in, "", RDFFormat.RDFXML).forEach(target::add);
            } catch (RDFParseException e) {
                throw new IOException("Failed to parse vocabulary file '" + filename + "': " + e.getMessage(), e);
            }
        }
    }
    
    // Bridges differences between EU authority tables and DCAT-AP shape expectations:
    // 1. Adds the DCAT-AP-required class to each concept in a known scheme.
    //    (EU declares concepts as skos:Concept; shapes want e.g. dcterms:LinguisticSystem.)
    // 2. Mirrors skos:prefLabel to dcterms:title on the scheme itself.
    //    (EU uses skos:prefLabel; shapes require dcterms:title on ConceptSchemes.)
    private void applySchemeBridges(Model target) {
        SCHEME_BRIDGE_TYPES.forEach((schemeUri, requiredClass) -> {
            IRI scheme = VF.createIRI(schemeUri);

            // 1. Type each concept in the scheme.
            List<IRI> concepts = target.filter(null, SKOS.IN_SCHEME, scheme).stream()
                .map(stmt -> stmt.getSubject())
                .filter(IRI.class::isInstance)
                .map(IRI.class::cast)
                .toList();
            concepts.forEach(c -> target.add(c, RDF.TYPE, requiredClass));

            // 2. Copy each prefLabel to a matching title.
            List<Value> labels = target.filter(scheme, SKOS.PREF_LABEL, null).stream()
                .map(stmt -> stmt.getObject())
                .toList();
            labels.forEach(label -> target.add(scheme, DCTERMS.TITLE, label));
        });
    }

    // DCAT-AP defines its own tiny vocabulary directly in the specification,
    // with no separately published RDF distribution.
    private void addPlannedAvailability(Model target){
        IRI plannedAvailability = VF.createIRI("http://data.europa.eu/r5r/PlannedAvailability");

        Map<String, String> labels = Map.of(
            "stable",       "Stable",
            "experimental", "Experimental",
            "available",    "Available",
            "temporary",    "Temporary"
        );

        for (Map.Entry<String, String> e : labels.entrySet()) {
            IRI uri = VF.createIRI("http://data.europa.eu/r5r/availability/" + e.getKey());
            target.add(uri, RDF.TYPE, plannedAvailability);
            target.add(uri, RDF.TYPE, SKOS.CONCEPT);
            target.add(uri, SKOS.PREF_LABEL, VF.createLiteral(e.getValue(), "en"));
        }
    }

    // ADMS publisher types. The original purl.org URIs are no longer reliably
    // dereferenceable, but DCAT-AP-SE still specifies this vocabulary as the
    // range of dcterms:type on Agent.
    private void addAdmsPublisherTypes(Model target) {
        String base = "http://purl.org/adms/publishertype/";

        Map<String, String> types = Map.ofEntries(
            Map.entry("Academia-ScientificOrganisation", "Academia/Scientific organisation"),
            Map.entry("Company",                         "Company"),
            Map.entry("IndustryConsortium",              "Industry consortium"),
            Map.entry("LocalAuthority",                  "Local Authority"),
            Map.entry("NationalAuthority",               "National authority"),
            Map.entry("NonGovernmentalOrganisation",     "Non-Governmental Organisation"),
            Map.entry("NonProfitOrganisation",           "Non-Profit Organisation"),
            Map.entry("PrivateIndividual",               "Private Individual(s)"),
            Map.entry("RegionalAuthority",               "Regional authority"),
            Map.entry("StandardisationBody",             "Standardisation body"),
            Map.entry("SupraNationalAuthority",          "Supra-national authority")
        );
        types.forEach((key, label) -> {
            IRI uri = VF.createIRI(base + key);
            target.add(uri, RDF.TYPE, SKOS.CONCEPT);
            target.add(uri, SKOS.PREF_LABEL, VF.createLiteral(label, "en"));
        });
    }

    // INSPIRE responsible party roles. The official registry RDF includes
    // catalog metadata that triggers unwanted SHACL shapes if loaded as-is.
    private void addInspireResponsiblePartyRoles(Model target) {
        String base = "http://inspire.ec.europa.eu/metadata-codelist/ResponsiblePartyRole/";
        IRI dcatRole = VF.createIRI("http://www.w3.org/ns/dcat#Role");
        Map<String, String> roles = Map.ofEntries(
            Map.entry("resourceProvider",      "Resource provider"),
            Map.entry("custodian",             "Custodian"),
            Map.entry("owner",                 "Owner"),
            Map.entry("user",                  "User"),
            Map.entry("distributor",           "Distributor"),
            Map.entry("originator",            "Originator"),
            Map.entry("pointOfContact",        "Point of contact"),
            Map.entry("principalInvestigator", "Principal investigator"),
            Map.entry("processor",             "Processor"),
            Map.entry("publisher",             "Publisher"),
            Map.entry("author",                "Author")
        );
        roles.forEach((key, label) -> {
            IRI uri = VF.createIRI(base + key);
            target.add(uri, RDF.TYPE, dcatRole);
            target.add(uri, SKOS.PREF_LABEL, VF.createLiteral(label, "en"));
        });
    }

    // SPDX checksum algorithm identifiers used by spdx:algorithm on a
    // spdx:Checksum node. SPDX does not ship a small dedicated RDF
    // distribution of the algorithm individuals, so they are declared directly.
    private void addSpdxChecksumAlgorithms(Model target) {
        IRI checksumAlgorithm = VF.createIRI("http://spdx.org/rdf/terms#ChecksumAlgorithm");
        for (String alg : List.of("sha1", "sha256", "md5", "sha512", "sha384", "sha224")) {
            IRI uri = VF.createIRI("http://spdx.org/rdf/terms#checksumAlgorithm_" + alg);
            target.add(uri, RDF.TYPE, checksumAlgorithm);
        }
    }
}
