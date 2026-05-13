/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.converter;

import org.apache.commons.collections4.MultiValuedMap;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONObject;
import se.ams.dcatprocessor.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Converter {
    Catalog catalog = new Catalog();
    FileStorage fileHandler = new FileStorage();
    JSONObject jsonObjectMandatoryDcat;
    JSONObject orgConvert;
    public static List<String> errors = new ArrayList<>();

    public static void deleteErrors() {
        errors.clear();
    }

    /*
     * Takes one specFile for a Catalog and creates a Catalog Object */
    public DataClass catalogToDcat(JSONObject apiSpec) throws Exception {
        return catalog;
    }

    /*
     * Takes one specFile for a Api and creates an Object with ApiSpec tags, DataSet, DataService, Distribution etc. */
    public DataClass fileToDcat(JSONObject apiSpec) throws Exception {
        return fileHandler;
    }

    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
    }

    /*
     * Finds and sets correct converterfile and mandatoryfile depending on if it is catalog or other file */
    void setConvertAndMandatoryFile(String uriToDirectory) throws IOException {
        String convertFile;
        convertFile = getFileString(uriToDirectory + ConverterHelpClass.convertFileName);
        orgConvert = new JSONObject(convertFile);

        String mandatoryFile = getFileString(uriToDirectory + ConverterHelpClass.mandatoryFileName);
        jsonObjectMandatoryDcat = new JSONObject(mandatoryFile);
    }

    String getFileString(String fileName) throws IOException {
        InputStream is2 = getClass().getClassLoader().getResourceAsStream(fileName);
        return new String(is2 != null ? is2.readAllBytes() : new byte[0], StandardCharsets.UTF_8);
    }

    /*
     * Finds and sets correct supportiveFile to create correct values for some tags */
    JSONObject getSupportiveFile(String key, String subCat) throws IOException {
        JSONObject jsonSupportiveDcat = null;

        if (ConverterHelpClass.supportiveFile.containsKey(key)) {
            String fileName = ConverterHelpClass.uriToDcatSupportive + ConverterHelpClass.supportiveFile.get(key);
            String supportiveFile;
            supportiveFile = getFileString(fileName);
            jsonSupportiveDcat = new JSONObject(supportiveFile);

        } else if (ConverterHelpClass.supportiveFile.containsKey(subCat + "-" + key)) {
            String fileName = ConverterHelpClass.uriToDcatSupportive + ConverterHelpClass.supportiveFile.get(subCat + "-" + key);
            String supportiveFile = getFileString(fileName);
            jsonSupportiveDcat = new JSONObject(supportiveFile);
        }
        return jsonSupportiveDcat;
    }

    /*
     * Adds a correct about string to the Object */
    void addAbout(DataClass dataObj, String value) {
        if (!(value.contains("http"))) {
            value = "http://" + value;
        }
        dataObj.about = value;
    }

    boolean loopLanguage(JSONObject file, String AnnotationName, Optional<String> subCat, Optional<DataClass> dataClass, String key) throws IOException {
        boolean hasLanguage = false;

        String fileName = ConverterHelpClass.uriToLanguageDcat;
        String languageFile;
        languageFile = getFileString(fileName);
        JSONObject jsonObjectLanguageDcat = new JSONObject(languageFile);

        Object[] languageKeys = jsonObjectLanguageDcat.keySet().toArray();
        Iterator<?> keysInLanguageFile = Arrays.stream(languageKeys).iterator();

        while (keysInLanguageFile.hasNext()) {
            Object keyTest = keysInLanguageFile.next();

            String keyInLanguage = jsonObjectLanguageDcat.getJSONObject((String) keyTest).getString(ConverterHelpClass.toDcatString);
            String urlLanguage = jsonObjectLanguageDcat.getJSONObject((String) keyTest).getString("url");

            if (file.has(AnnotationName + "-" + keyInLanguage)) {
                String value = (String) file.get(AnnotationName + "-" + keyInLanguage);
                String[] splitValue = value.split(";");
                for (String s : splitValue) {
                    if (subCat.isPresent()) {
                        if (key.equals(DCTERMS.PROVENANCE.getLocalName())) {
                            DataClass provenance = new DataClass();
                            provenance.dcData.put("dcterms:description", keyInLanguage + "¤" + s);
                            if (dataClass.isPresent()) ((DataSet)dataClass.get()).provenances.add(provenance);
                        }
                        else {
                            dataClass.ifPresent(aClass -> aClass.dcData.put(key, keyInLanguage + "¤" + s));
                        }
                        if (subCat.get().equals(DCAT.DATASET.getLocalName()) || subCat.get().equals(DCAT.DISTRIBUTION.getLocalName()) || subCat.get().equals(DCAT.CATALOG.getLocalName())) {
                            if (dataClass.isPresent()) {
                                if (!((dataClass.get().dcData.get(ConverterHelpClass.languages)).contains(urlLanguage))) {
                                    dataClass.get().dcData.put(ConverterHelpClass.languages, urlLanguage);
                                }
                            }
                        }
                    } else {
                        catalog.dcData.put(key + "-" + keyInLanguage, value);
                    }
                }
                hasLanguage = true;
            }
        }
        return hasLanguage;
    }

    /*
     * Sets a value to the correct Object */
    void addValue(MultiValuedMap<String, String> valueMap, String value, String key, Optional<String> subCat) throws IOException {
        JSONObject jsonSupportiveDcat = null;
        if (subCat.isPresent())
            jsonSupportiveDcat = getSupportiveFile(key, subCat.get());

        String[] splitValue = value.split(";");

        for (String s : splitValue) {
            String mapValue = s;
            if (jsonSupportiveDcat != null) {
                mapValue = mapValue.trim();
                if (jsonSupportiveDcat.has(mapValue)) {
                    mapValue = (String) ((JSONObject) (jsonSupportiveDcat.get(mapValue))).get("url");
                } else if (!mapValue.contains("http://") && !key.contains("format")) {
                    errors.add("Errormessage: " + key + " has a not supported value (" + mapValue + "). Check list for " + key + " to see the correct values that can be used.");
                }
            }
            if (ConverterHelpClass.tagWithUri.contains(key)) {
                mapValue = mapValue.trim();
                if (!(mapValue.contains("http"))) {
                    mapValue = "http://" + mapValue;
                }
            }
            if (key.contains("format")) {
                key = "dcterms:format";
            }
            if (key.equals("license")) {
                key = "dcterms:" + key;
            }
            if (ConverterHelpClass.tagWithUriMail.contains(key)) {
                mapValue = mapValue.trim();
                mapValue = "mailTo:" + mapValue;
            }
            valueMap.put(key, mapValue);
        }
    }

    /*
     * Create an Address Object from a list and saves it to the Organization Object */
    void addAddress(DataClass dataObj, String value) {
        String[] splitAddress = value.split(";");
        DataClass valueMapAddress = new DataClass();
        if (splitAddress.length == 4) {
            valueMapAddress.dcData.put(ConverterHelpClass.addressObject.get("StreetAddress"), splitAddress[0]);
            valueMapAddress.dcData.put(ConverterHelpClass.addressObject.get("PostalCode"), splitAddress[1]);
            valueMapAddress.dcData.put(ConverterHelpClass.addressObject.get("Locality"), splitAddress[2]);
            valueMapAddress.dcData.put(ConverterHelpClass.addressObject.get("Country"), splitAddress[3]);
            ((Organization) dataObj).adress.add(valueMapAddress);
        } else if (splitAddress.length < 4) {
            errors.add("Errormessage: " + " Address in Contact point has too few values, should contain street-address, postal-code, locality and country-name");
        } else {
            errors.add("Errormessage: " + " Address in Contact point has too many values, should contain street-address, postal-code, locality and country-name");
        }
    }

    /*
     * Adds a phonenumber with the correct value to the Organization Object */
    void addPhone(DataClass dataObj, String value) {
        String[] splitValue = value.split(";");
        for (String s : splitValue) {
            DataClass valueMapPhone = new DataClass();
            s = "tel:" + s;
            valueMapPhone.dcData.put("vcard:hasValue", s);
            ((Organization) dataObj).phone.add(valueMapPhone);
        }
    }

    void addValues(DataClass dataObj, String value, String key, Optional<String> subCat) throws IOException {
        if (key.equals(ConverterHelpClass.toDcatAboutString)) {
            addAbout(dataObj, value);
        } else if (key.equals("vcard:hasTelephone")) {
            addPhone(dataObj, value);
        } else if (key.equals("Address")) {
            addAddress(dataObj, value);
        } else {
            addValue(dataObj.dcData, value, key, subCat);
        }
    }

    void loopData(JSONObject file, String key, String AnnotationName, String newKey, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
    }

    void loopObject(JSONObject file, String key, String annotationName, DataClass address, Optional<String> subCat) throws IOException {
        Object[] fileKeys = file.keySet().toArray();
        Iterator<?> keysInRamlFile = Arrays.stream(fileKeys).iterator();

        while (keysInRamlFile.hasNext()) {
            Object keyInRaml = keysInRamlFile.next();
            if ((annotationName != null) && ((keyInRaml.toString()).contains(annotationName))) {

                String value = file.get(keyInRaml.toString()).toString();
                addValues(address, value, key, subCat);
            }
        }
    }

    void createSubset(JSONObject file, String key, String annotationName, Optional<DataClass> preData, Optional<DataClass> preDist, boolean isMandatory) throws Exception {
        Object[] fileKeys = file.keySet().toArray();
        Iterator<?> keysInRamlFile = Arrays.stream(fileKeys).iterator();
        boolean exists = false;

        while (keysInRamlFile.hasNext()) {
            Object keyInRaml = keysInRamlFile.next();
            if ((annotationName != null)) {
                if (((keyInRaml.toString()).contains(annotationName)) && !((keyInRaml.toString()).equals("temporalResolution"))) {
                    if (((JSONObject) orgConvert.get(key)).keySet().size() > 1) {
                        exists = true;
                        loopData(file, key, keyInRaml.toString(), key, preData, preDist);
                    }
                }
            }
        }

        if (!exists && isMandatory) {
            errors.add("Errormessage: " + annotationName + " is Mandatory");
        }
    }
}