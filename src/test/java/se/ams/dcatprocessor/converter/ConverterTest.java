package se.ams.dcatprocessor.converter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import se.ams.dcatprocessor.models.DataClass;
import se.ams.dcatprocessor.models.DataSet;

public class ConverterTest {

    private Converter converter;

    @BeforeEach
    void setup(){
        converter = new Converter();
        Converter.deleteErrors();
    }

    @Test
    void testThatAddLanguageValuesReturnsFalseWhenNoMatch() throws IOException {
        boolean result = converter.addLanguageValues(getJSONObjectForTest(), "about", Optional.empty(), Optional.of(new DataClass()), "about");
        assertFalse(result);
    }

    @Test
    void testThatAddLanguageValuesSetsLanguageWithoutSubset() throws IOException{
        String annotationName = "title";
        String key = "dcterms:title";
        Optional<String> subCat = Optional.empty();
        Optional<DataClass> dataClass = Optional.empty();

        boolean result = converter.addLanguageValues(getJSONObjectForTest(), annotationName, subCat, dataClass, key);

        assertTrue(result);
        assertTrue(converter.catalog.dcData.get(key + "-sv").contains("Svensk-titel"));
        assertTrue(converter.catalog.dcData.get(key + "-en").contains("English-title"));
    }

   	@ParameterizedTest
	@MethodSource("getDcatLocalNames")
    void testThatAddLanguageValuesSetsLanguageWithSubset(String subCat) throws IOException{
        String annotationName = "title";
        String key = "dcterms:title";
        DataClass dataClass = new DataClass();

        boolean result = converter.addLanguageValues(getJSONObjectForTest(), annotationName, Optional.of(subCat), Optional.of(dataClass), key);

        assertTrue(result);
        assertTrue(dataClass.dcData.get(key).contains("sv¤Svensk-titel"));
        assertTrue(dataClass.dcData.get(key).contains("en¤English-title"));
    }

    @Test
    void testThatAddLanguageValuesSetsLanguageIfProvenace() throws IOException{
        String key = DCTERMS.PROVENANCE.getLocalName();
        String annotationName = "provenance";
        Optional<String> subCat = Optional.of(DCAT.DATASET.getLocalName());
        DataSet dataSet = new DataSet();

        boolean result = converter.addLanguageValues(getJSONObjectForTest(), annotationName, subCat, Optional.of(dataSet), key);

        assertTrue(result);
        assertTrue(dataSet.provenances.stream()
            .anyMatch(p -> p.dcData.get("dcterms:description").contains("sv¤Källa SCB")));
        assertTrue(dataSet.provenances.stream()
            .anyMatch(p -> p.dcData.get("dcterms:description").contains("en¤Source SCB")));
    }

    @Test
    void testThatAddMandatoryErrorAddsMessageWithSubCat() {
        converter.addMandatoryError("title", Optional.of("dataset"));
        assertTrue(Converter.errors.contains("Errormessage: title in dataset is Mandatory"));
    }

    @Test
    void testThatAddMandatoryErrorAddsMessageWithoutSubCat() {
        converter.addMandatoryError("title", Optional.empty());
        assertTrue(Converter.errors.contains("Errormessage: title is Mandatory"));
    }

    @Test
    void testThatIsKeyMandatoryReturnsTrueWhenKeyExists() {
        converter.jsonObjectMandatoryDcat = new JSONObject("{\"title\": true}");
        assertTrue(converter.isKeyMandatory("title", Optional.empty()));
    }

    @Test
    void testThatIsKeyMandatoryReturnsTrueWhenCompositeKeyExists() {
        converter.jsonObjectMandatoryDcat = new JSONObject("{\"dataset-title\": true}");
        assertTrue(converter.isKeyMandatory("title", Optional.of("dataset")));
    }

    @Test
    void testThatIsKeyMandatoryReturnsFalseWhenKeyMissing() {
        converter.jsonObjectMandatoryDcat = new JSONObject("{}");
        assertFalse(converter.isKeyMandatory("title", Optional.empty()));
    }

    private JSONObject getJSONObjectForTest(){
        return new JSONObject("""
        {
            "title-sv": "Svensk-titel",
            "title-en": "English-title",
            "provenance-sv": "Källa SCB",
            "provenance-en": "Source SCB"
        }
        """);
    }

    private static String[] getDcatLocalNames(){
        return new String[]{
            DCAT.DATASET.getLocalName(),
            DCAT.DISTRIBUTION.getLocalName(), 
            DCAT.CATALOG.getLocalName(),
        };
    }
}
