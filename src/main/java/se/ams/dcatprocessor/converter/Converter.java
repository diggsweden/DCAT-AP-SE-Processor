// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.apache.commons.collections4.MultiValuedMap;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONObject;
import se.ams.dcatprocessor.models.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Converter {
    Catalog catalog = new Catalog();
    FileStorage fileHandler = new FileStorage();
    JSONObject jsonObjectMandatoryDcat;
    JSONObject jsonLanguageFile;
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
        String convertFile = getFileString(uriToDirectory + ConverterHelpClass.convertFileName);
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
        String resolvedKey = null;
        String compositeKey = subCat + "-" + key;

        if (ConverterHelpClass.supportiveFile.containsKey(key)) {
            resolvedKey = key;
        } else if (ConverterHelpClass.supportiveFile.containsKey(compositeKey)) {
            resolvedKey = compositeKey;
        } else {
            return null;
        }

        String fileName = ConverterHelpClass.uriToDcatSupportive + ConverterHelpClass.supportiveFile.get(resolvedKey);
        return new JSONObject(getFileString(fileName));
    }

    /*
     * Adds a correct about string to the Object */
    void addAbout(DataClass dataObj, String value) {
        if (!(value.contains("http"))) {
            value = "http://" + value;
        }
        dataObj.about = value;
    }

    JSONObject getOrLoadLanguageFile() throws IOException {
        if (jsonLanguageFile == null) {
            jsonLanguageFile = new JSONObject(getFileString(ConverterHelpClass.uriToLanguageDcat));
        }
        return jsonLanguageFile;
    }

    boolean addLanguageValues(JSONObject file, String annotationName, Optional<String> subCat, Optional<DataClass> dataClass, String key) throws IOException {
        boolean hasLanguage = false;
        JSONObject languageFile = getOrLoadLanguageFile();

        for(String langKey : languageFile.keySet()){
            String keyInLanguage = languageFile.getJSONObject(langKey).getString(ConverterHelpClass.toDcatString);
            String fieldName = annotationName + "-" + keyInLanguage;
            
            if (!file.has(fieldName)) continue;
            
            String value = (String) file.get(fieldName);
            String urlLanguage = languageFile.getJSONObject(langKey).getString("url");

            for (String s : value.split(";")) {
                applyLanguageValue(subCat, key, keyInLanguage, dataClass, s.trim(), urlLanguage);
            }
            hasLanguage = true;        
        }
        return hasLanguage;
    }

    private void applyLanguageValue(Optional<String> subCat, String key, String keyInLanguage, Optional<DataClass> optionalDataClass, String value, String urlLanguage){  
        // Without subCat, write directly to catalog with language suffix
        if (subCat.isEmpty()) {
            catalog.dcData.put(key + "-" + keyInLanguage, value);
            return;
        }
        
        if(subCat.isPresent()){

            if (optionalDataClass.isEmpty()) return;

            DataClass dataClass = optionalDataClass.get();
            String combinedValue = keyInLanguage + "¤" + value;

            // Provenance is a special case that requires its own DataClass entry
            if (key.equals(DCTERMS.PROVENANCE.getLocalName())) {
                DataClass provenance = new DataClass();
                provenance.dcData.put("dcterms:description", combinedValue);

                if (dataClass instanceof DataSet dataSet) {
                    dataSet.provenances.add(provenance);
                }
            }
            // Add value to dataClass
            else {
                dataClass.dcData.put(key, combinedValue);
            }
            String subCatValue = subCat.get();
            if (subCatValue.equals(DCAT.DATASET.getLocalName()) || subCatValue.equals(DCAT.DISTRIBUTION.getLocalName()) || subCatValue.equals(DCAT.CATALOG.getLocalName())) {

                if (!((dataClass.dcData.get(ConverterHelpClass.languages)).contains(urlLanguage))) {
                    dataClass.dcData.put(ConverterHelpClass.languages, urlLanguage);
                }
            }
        }
        else{
            catalog.dcData.put(key + "-" + keyInLanguage, value);
        }
    }

    /*
     * Sets a value to the correct Object */
    void addValue(MultiValuedMap<String, String> valueMap, String value, String key, Optional<String> subCat) throws IOException {
        JSONObject jsonSupportiveDcat = null;
        if (subCat.isPresent())
            jsonSupportiveDcat = getSupportiveFile(key, subCat.get());

        String[] splitValue = value.split(";");
        
        for (String s : splitValue) {
            String mapValue = s.trim();
            if (jsonSupportiveDcat != null) {
                if (jsonSupportiveDcat.has(mapValue)) {
                    mapValue = (String) ((JSONObject) (jsonSupportiveDcat.get(mapValue))).get("url");
                } else if (!mapValue.contains("http://") && !key.contains("format")) {
                    errors.add("Errormessage: " + key + " has a not supported value (" + mapValue + "). Check list for " + key + " to see the correct values that can be used.");
                }
            }
            if (ConverterHelpClass.tagWithUri.contains(key)) {
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

    void convertNestedObject(JSONObject file, String key, String annotationName, String newKey, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        JSONObject subToConvert = (JSONObject) orgConvert.get(key);
        subToConvert.remove(ConverterHelpClass.toDcatMandatoryString);
        
        if (subToConvert.keySet().size() > 0) {
            JSONObject subJsonFile = ((JSONObject) file.get(annotationName));
            processToDcat(subToConvert, subJsonFile, Optional.ofNullable(newKey), preData, preDist);
        }
    }

    void addFieldsContainingName(JSONObject file, String key, String annotationName, DataClass address, Optional<String> subCat) throws IOException {
        if (annotationName == null) return;

        for(String keyInRaml : file.keySet()){
            if(keyInRaml.contains(annotationName)){
                String value = file.get(keyInRaml).toString();
                addValues(address, value, key, subCat);
            }
        }
    }

    void createSubset(JSONObject file, String key, String annotationName, Optional<DataClass> preData, Optional<DataClass> preDist, boolean isMandatory) throws Exception { 
        if (annotationName == null) return;
        
        boolean exists = false;
        JSONObject mappingBlock = (JSONObject) orgConvert.get(key);

        for (Object keyInRaml : file.keySet()) {      
            String ramlKey = keyInRaml.toString();

            if (ramlKey.contains(annotationName) && !substringCollision(annotationName, ramlKey)) {

                // Check if block has nested fields to convert.
                if (mappingBlock.keySet().size() > 1) {
                    exists = true;
                    convertNestedObject(file, key, ramlKey, key, preData, preDist);
                }
            }
        }

        if (!exists && isMandatory) {
            errors.add("Errormessage: " + annotationName + " is Mandatory");
        }
    }

    protected boolean substringCollision(String annotationName, String ramlKey){
        // prevent temporalResolution (field) from matching as a temporal (class)
        if(ramlKey.equals("temporalResolution")){
            return true;
        }
        // prevent dcat-datasetseries from matching as a dcat-dataset
        if(annotationName.equals("dcat-dataset") && ramlKey.equals("dcat-datasetseries")){
            return true;
        }
        return false;
    } 

    protected boolean isKeyMandatory(Object key, Optional<String> subCat) {
        String mandatoryKey;
        if (subCat.isPresent()) {
            mandatoryKey = subCat.get() + "-" + key;
        } else {
            mandatoryKey = key.toString();
        }
        return jsonObjectMandatoryDcat.has(mandatoryKey);
    }

    protected void addMandatoryError(String annotationName, Optional<String> subCat) {
        if (subCat.isPresent()) {
            errors.add("Errormessage: " + annotationName + " in " + subCat.get() + " is Mandatory");
        } else {
            errors.add("Errormessage: " + annotationName + " is Mandatory");
        }
    }

    protected void handleAddress(JSONObject file, String key, String annotationName, Organization organizationLocal, Optional<String> subCat) throws Exception {
        if (file.has(annotationName)) {
            addValues(organizationLocal, String.valueOf(file.get(annotationName)), key, subCat);
        } else {
            addFieldsContainingName(file, key, annotationName, organizationLocal, subCat);
        }
    }
}