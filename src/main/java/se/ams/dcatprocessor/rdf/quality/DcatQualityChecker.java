// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.quality;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.rdf.VocabularyStringToIRI;
import se.ams.dcatprocessor.rdf.quality.QualityReport.MainClassQuality;
import se.ams.dcatprocessor.rdf.quality.QualityReport.QualityIssue;
import se.ams.dcatprocessor.specification.DcatProperty;
import se.ams.dcatprocessor.specification.DcatSpecification;

/**
 * Checks the quality of generated DCAT-AP-SE RDF.
 */
@Component
public class DcatQualityChecker {

    private final DcatSpecification specification;

    private record MainClass(IRI type, String templateId) {
    }

    private static final int MIN_DESCRIPTION_LENGTH = 40;
    private static final int MIN_KEYWORD_COUNT = 5;

    private static final String NO_LANGUAGE = "no language";
    
    // Prefixed property names shown in the report, the checks query the model with the IRIs
    private static final String DESCRIPTION_REPORT_NAME = "dcterms:description";
    private static final String KEYWORD_REPORT_NAME = "dcat:keyword";
    private static final String TITLE_REPORT_NAME = "dcterms:title";
    
    // Predefined issue messages
    private static final String ISSUE_DESCRIPTION_TOO_SHORT = "Description (%s) is shorter than the recommended %s characters";
    private static final String ISSUE_RECOMMENDED_PROPERTY_MISSING = "Recommended property %s is missing";
    private static final String ISSUE_TOO_FEW_KEYWORDS = "Only %s distinct keywords (%s), at least %s are recommended per language";
    private static final String ISSUE_TITLE_LANGUAGE_MISSING = "The description has a version in %s, but the title does not";
    private static final String ISSUE_DESCRIPTION_LANGUAGE_MISSING = "The title has a version in %s, but the description does not";
    private static final String ISSUE_RECOMMENDED_RESOURCE_MISSING = "Recommended %s is missing a described resource with its own details";
    
    // Exception message
    private static final String RECOMMENDED_PROPERTY_HAS_NO_IRI_MAPPING = "Configuration error: recommended property %s in the bundle has no IRI mapping in VocabularyStringToIRI";;

    private static final List<MainClass> MAIN_CLASSES = List.of(
            new MainClass(DCAT.CATALOG, "dcat:Catalog"),
            new MainClass(DCAT.DATASET, "dcat:Dataset"),
            new MainClass(DCAT.DATASET_SERIES, "dcat:DatasetSeries"),
            new MainClass(DCAT.DATA_SERVICE, "dcat:DataService"),
            new MainClass(DCAT.DISTRIBUTION, "dcat:Distribution"));

    public DcatQualityChecker(DcatSpecification specification) {
        this.specification = specification;
    }

    public QualityReport check(String rdf) {
        Model model = parse(rdf);

        List<MainClassQuality> mainClasses = checkMainClasses(model);
        return new QualityReport(true, mainClasses);
    }

    /**
     * Returns one entry per main class in the RDF, including main classes without issues.
     * Main classes that are absent from the RDF get no entry.
     */
    private List<MainClassQuality> checkMainClasses(Model model) {
        List<MainClassQuality> mainClasses = new ArrayList<>();
        for (MainClass mainClass : MAIN_CLASSES) {

            // Pick out every subject in the RDF of this main class, for example each Dataset
            Set<Resource> subjectsOfMainClass = model.filter(null, RDF.TYPE, mainClass.type()).subjects();
            for (Resource subject : subjectsOfMainClass) {
                MainClassQuality mainClassQuality = checkMainClass(model, mainClass, subject);
                mainClasses.add(mainClassQuality);
            }
        }
        return mainClasses;
    }

    /**
     * Runs all quality checks on one main class in the RDF.
     */
    private MainClassQuality checkMainClass(Model model, MainClass mainClass, Resource subject) {
        List<QualityIssue> issues = new ArrayList<>();
        
        issues.addAll(checkDescriptionLength(model, subject));
        issues.addAll(checkTitleAndDescriptionLanguages(model, subject));
        issues.addAll(checkRecommendedProperties(model, mainClass, subject));
        issues.addAll(checkKeywordCount(model, subject));

        return new MainClassQuality(mainClass.type().getLocalName(), subject.stringValue(), issues);
    }

    /**
     * Checks that each language version of the description has at least {@value #MIN_DESCRIPTION_LENGTH} characters.
     * A missing description gets no issue here, validation or the recommended properties check reports it.
     */
    private List<QualityIssue> checkDescriptionLength(Model model, Resource subject) {
        List<QualityIssue> issues = new ArrayList<>();
        for (Value value : model.filter(subject, DCTERMS.DESCRIPTION, null).objects()) {
            if (value instanceof Literal literal) {
                String description = literal.getLabel().trim();
                if (description.length() < MIN_DESCRIPTION_LENGTH) {
                    String language = literal.getLanguage().orElse(NO_LANGUAGE);
                    issues.add(new QualityIssue(DESCRIPTION_REPORT_NAME,
                            ISSUE_DESCRIPTION_TOO_SHORT.formatted(language, MIN_DESCRIPTION_LENGTH)));
                }
            }
        }
        return issues;
    }

    /**
     * Checks that the title and the description are given in the same languages.
     * A missing title or description gets no issue here, validation or the recommended properties check reports it.
     */
    private List<QualityIssue> checkTitleAndDescriptionLanguages(Model model, Resource subject) {
        List<QualityIssue> issues = new ArrayList<>();
        Set<String> titleLanguages = languagesOf(model, subject, DCTERMS.TITLE);
        Set<String> descriptionLanguages = languagesOf(model, subject, DCTERMS.DESCRIPTION);

        if (titleLanguages.isEmpty() || descriptionLanguages.isEmpty()) {
            return issues;
        }

        for (String language : titleLanguages) {
            if (!descriptionLanguages.contains(language)) {
                issues.add(new QualityIssue(DESCRIPTION_REPORT_NAME, ISSUE_DESCRIPTION_LANGUAGE_MISSING.formatted(languageName(language))));
            }
        }
        for (String language : descriptionLanguages) {
            if (!titleLanguages.contains(language)) {
                issues.add(new QualityIssue(TITLE_REPORT_NAME, ISSUE_TITLE_LANGUAGE_MISSING.formatted(languageName(language))));
            }
        }
        return issues;
    }

    private Set<String> languagesOf(Model model, Resource subject, IRI property) {
        Set<String> languages = new LinkedHashSet<>();
        for (Value value : model.filter(subject, property, null).objects()) {
            if (value instanceof Literal literal) {
                languages.add(literal.getLanguage().orElse(NO_LANGUAGE));
            }
        }
        return languages;
    }

    /**
     * Checks that the properties the bundle recommends for the main class are present.
     * A group counts as present if any of its properties is, and an object node needs a described object.
     */
    private List<QualityIssue> checkRecommendedProperties(Model model, MainClass mainClass, Resource subject) {
        List<QualityIssue> issues = new ArrayList<>();

        for (DcatProperty node : specification.recommendedFor(mainClass.templateId())) {
            List<String> properties = specification.propertiesOf(node);

            if (!properties.isEmpty()) {
                String property = properties.get(0);
                List<IRI> iris = toIris(properties);

                // An object node has items of its own, for example Location as a geographic area for dcterms:spatial
                boolean isObjectNode = node.getProperty() != null && !node.getItems().isEmpty();

                if (isObjectNode) {
                    boolean hasObject = hasDescribedObject(model, subject, iris);
                    if(!hasObject){
                        issues.add(new QualityIssue(property, ISSUE_RECOMMENDED_RESOURCE_MISSING.formatted(property)));
                    }
                } else {
                    boolean hasProperty = hasAnyProperty(model, subject, iris);
                    if(!hasProperty){
                        issues.add(new QualityIssue(property, ISSUE_RECOMMENDED_PROPERTY_MISSING.formatted(property)));
                    }
                }
            }
        }
        return issues;
    }

    /**
     * Checks that each language has at least {@value #MIN_KEYWORD_COUNT} distinct keywords.
     * A subject without keywords gets no issue here, {@link #checkRecommendedProperties} reports it.
     */
    private List<QualityIssue> checkKeywordCount(Model model, Resource subject) {
        Map<String, Set<String>> keywordsByLanguage = new LinkedHashMap<>();
        for (Value value : model.filter(subject, DCAT.KEYWORD, null).objects()) {
            if (value instanceof Literal literal) {
                String language = literal.getLanguage().orElse(NO_LANGUAGE);
                String keyword = literal.getLabel().trim().toLowerCase(Locale.ROOT);

                Set<String> keywords = keywordsByLanguage.get(language);
                if (keywords == null) {
                    keywords = new HashSet<>();
                    keywordsByLanguage.put(language, keywords);
                }
                keywords.add(keyword);
            }
        }

        List<QualityIssue> issues = new ArrayList<>();
        for (String language : keywordsByLanguage.keySet()) {
            int keywordCount = keywordsByLanguage.get(language).size();
            if (keywordCount < MIN_KEYWORD_COUNT) {
                issues.add(new QualityIssue(KEYWORD_REPORT_NAME,
                        ISSUE_TOO_FEW_KEYWORDS.formatted(keywordCount, language, MIN_KEYWORD_COUNT)));
            }
        }
        return issues;
    }

    private Model parse(String rdf) {
        try (StringReader reader = new StringReader(rdf)) {
            return Rio.parse(reader, "", RDFFormat.RDFXML);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read generated RDF", e);
        }
    }

    private List<IRI> toIris(List<String> prefixedNames) {
        List<IRI> iris = new ArrayList<>();
        for (String prefixedName : prefixedNames) {
            IRI iri = VocabularyStringToIRI.getIRI(prefixedName);
            if (iri == null) {
                throw new IllegalStateException(RECOMMENDED_PROPERTY_HAS_NO_IRI_MAPPING.formatted(prefixedName));
            }
            iris.add(iri);
        }
        return iris;
    }

    // For example "English (en)", so the report shows both the language and its tag
    private String languageName(String language) {
        if (NO_LANGUAGE.equals(language)) {
            return language;
        }
        String name = Locale.forLanguageTag(language).getDisplayLanguage(Locale.ENGLISH);
        return name + " (" + language + ")";
    }

    private boolean hasAnyProperty(Model model, Resource subject, List<IRI> properties) {
        for (IRI property : properties) {
            if (model.contains(subject, property, null)) {
                return true;
            }
        }
        return false;
    }

    
    // True if one of the properties points to a resource described in the RDF, for example a Location with coordinates.
    private boolean hasDescribedObject(Model model, Resource subject, List<IRI> properties) {
        for (IRI property : properties) {
            for (Value object : model.filter(subject, property, null).objects()) {
                if (object instanceof Resource resource && model.contains(resource, null, null)) {
                    return true;
                }
            }
        }
        return false;
    }
}
