// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclValidator;
import org.eclipse.rdf4j.sail.shacl.ShaclValidator.ValidatorWithShapes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.rdf.DcatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RDFValidator {

    private record Override(String path, String constraintType) {}
    
    private final List<Override> overrides;
    private final ValidatorWithShapes shaclValidator;
    private final Model vocabulary;

    private static final String OVERRIDES_FILE = "shapes/dcat-ap-se-overrides.json";
    private static final List<String> SHAPE_FILES = List.of(
        "shapes/dcat-ap-SHACL.ttl",
        "shapes/dcat-ap-se-additions.ttl"
    );

    // Maps predicate → rdf:type to assert on its IRI value (e.g. anything used as dcterms:license is typed as dcterms:LicenseDocument).
    private static final Map<IRI, IRI> PREDICATE_TARGET_TYPES = Map.of(
        DCTERMS.CONFORMS_TO, DCTERMS.STANDARD,
        DCTERMS.SPATIAL,     DCTERMS.LOCATION,
        DCTERMS.LICENSE,     DCTERMS.LICENSE_DOCUMENT
    );

    public RDFValidator(RDFVocabularyBuilder rdfVocabularyBuilder) throws IOException{
        this.vocabulary = rdfVocabularyBuilder.build();
        this.shaclValidator = setupShaclValidator();
        this.overrides = loadOverrides();
    }

    // Tags runtime-generated IRIs with their expected rdf:type so SHACL validation passes.
    private void enrichDataGraph(RepositoryConnection conn) {
        PREDICATE_TARGET_TYPES.forEach((predicate, type) -> {
            List<IRI> toType = new ArrayList<>();
            try (var stmts = conn.getStatements(null, predicate, null, false)) {
                stmts.forEach(s -> {
                    if (s.getObject() instanceof IRI iri) toType.add(iri);
                });
            }
            toType.forEach(iri -> conn.add(iri, RDF.TYPE, type));
        });
    }

    public List<RDFValidationError> validate(String rdf) throws Exception {
        if (rdf == null || rdf.isBlank()) {
            throw new DcatException("RDFValidator: RDF input is empty or missing");
        }

        List<RDFValidationError> result = new ArrayList<>();

        // Build a data sail: generated RDF + supporting vocabulary types
        SailRepository dataRepo = new SailRepository(new MemoryStore());
        dataRepo.init();

        try (SailRepositoryConnection conn = dataRepo.getConnection()) {
            conn.add(new StringReader(rdf), "", RDFFormat.RDFXML);
            conn.add(vocabulary);
            enrichDataGraph(conn);

            var report = shaclValidator.validate(dataRepo.getSail());
            Model reportModel = report.asModel();

            //Check for violations in RDFValidation
            for (var violation : reportModel.filter(null, RDF.TYPE, SHACL.VALIDATION_RESULT).subjects()) {
                RDFValidationError error = new RDFValidationError(violation, reportModel);

                if(!isOverridden(error)){
                    result.add(error);
                }
            }

            return result;

        } catch (RDFParseException e) {
            throw new DcatException("RDFValidator: RDF input is Invalid");
        } catch (IOException e) {
            throw new DcatException("RDFValidator: Failed to load shapes or vocabulary");       
        } finally {
            dataRepo.shutDown();
        }
    }

    // Builds the SHACL validator from the configured shape files.
    private ValidatorWithShapes setupShaclValidator() throws IOException {    
        String shapes = loadFilesAsString(SHAPE_FILES);
        ValidatorWithShapes validator = ShaclValidator.builder()
            .withShapes(shapes, "", RDFFormat.TURTLE)
            .build();

        return validator;         
    }

    private String loadFilesAsString(List<String> paths) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            try (InputStream in = new ClassPathResource(path).getInputStream()) {
                sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Reads the SE override rules from JSON.
     * Each pair represents a SHACL violation that should be filtered out
     * of the report because the Swedish profile diverges from the EU profile
     */
    private List<Override> loadOverrides() throws IOException {
        String json;
        try (InputStream in = new ClassPathResource(OVERRIDES_FILE).getInputStream()) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<Override> out = new ArrayList<>();
        JSONArray rules = new JSONObject(json).getJSONArray("overrides");
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String path = rule.getString("path");
            JSONArray constraints = rule.getJSONArray("ignoreConstraints");
            for (int j = 0; j < constraints.length(); j++) {
                out.add(new Override(path, constraints.getString(j)));
            }
        }
        return out;
    }
 
    // Ignore overridden for violations, validation is based on DCAT-AP shapes(.ttl file). SE version differs but has no availbale shapes
    private boolean isOverridden(RDFValidationError error) {
        return overrides.stream().anyMatch(o ->
                o.path().equals(error.path) && o.constraintType().equals(error.constraintType));
    }
}
