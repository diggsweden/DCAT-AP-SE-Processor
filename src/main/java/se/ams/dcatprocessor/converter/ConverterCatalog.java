// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.apache.commons.collections4.MultiValuedMap;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONObject;
import se.ams.dcatprocessor.models.*;

import java.io.IOException;
import java.util.*;


public class ConverterCatalog extends Converter {

    /* Takes one specFile for a Catalog and creates a Catalog Object */
    @Override
    public DataClass catalogToDcat(JSONObject apiSpec) throws Exception {
        setConvertAndMandatoryFile(ConverterHelpClass.uriToDcatCatalog);
        processToDcat(orgConvert, apiSpec, Optional.empty(), Optional.empty(), Optional.empty());
        catalog.dcData.put("dcat:themeTaxonomy", "http://publications.europa.eu/resource/authority/data-theme");

        if (errors.size() > 0) {
            StringBuilder errorResult = new StringBuilder();
            for (String error : errors) {
                errorResult.append(error).append("\n");
            }
            throw new Exception(errorResult.toString());
        }
        return catalog;
    }

    /*
     * Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataClass dataClassLocal = new DataClass();
        
        for (String key : subConvert.keySet()) {

            if (key.equals(ConverterHelpClass.toDcatString) && subCat.isPresent()) {
                continue;
            }

            // Get tag-name for api-spec
            String annotationName = subConvert.getJSONObject(key).getString(ConverterHelpClass.toDcatString);

            // Check if tag is Mandatory
            boolean isMandatory = isKeyMandatory(key, subCat);

            // Do if key is CATALOG
            if (key.contains(DCAT.CATALOG.getLocalName())) {
                createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
            }
            // Do if key is LICENSE_DOCUMENT
            else if (key.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    }
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key)) {
                if (subCat.isPresent() && subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                    createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            /*
             * Checks for language strings to add them correctly */
            else {
                boolean hasLanguages;
                if (subCat.isPresent()) {
                    if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataClassLocal), key);
                    } else {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.ofNullable(catalog), key);
                    }
                } else {
                    hasLanguages = addLanguageValues(file, annotationName, Optional.empty(), Optional.ofNullable(catalog), key);
                }

                if (!hasLanguages) {
                    if (file.has(annotationName)) {
                        String value = String.valueOf(file.get(annotationName));
                        if (subCat.isPresent()) {
                            if (ConverterHelpClass.isNestedObjects(subCat.get())) {
                                addValues(dataClassLocal, value, key, subCat);
                            } else {
                                addValues(catalog, value, key, subCat);
                            }
                        }
                    } else if (isMandatory) {
                        addMandatoryError(annotationName, subCat);
                    }
                }
            }
        }
        if (subCat.isPresent()) {
            attachDataToParent(subCat, preData, dataClassLocal);
        }
    }

    // Attaches the locally built dataClassLocal to the correct field on the parent (either preData or catalog)
    private void attachDataToParent(Optional<String> subCat, Optional<DataClass> preData, DataClass dataClassLocal) {
        String subCatValue = subCat.get();

        if (subCatValue.contains(DCTERMS.PUBLISHER.getLocalName())) {
            if (preData.isPresent()) {
                preData.get().agents.add(dataClassLocal);
            } else {
                catalog.publisher = dataClassLocal;
            }
        } else if (subCatValue.equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
            catalog.rights = dataClassLocal;
        } else if (subCatValue.equals(DCTERMS.SPATIAL.getLocalName())) {
            catalog.spatial.add(dataClassLocal);
        } else if (subCatValue.contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
            preData.ifPresent(dataClass -> dataClass.licenseDocuments.add(dataClassLocal));
        }
    }

    /*
     * Sets a value to the correct Object */
    @Override
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

    @Override
    void addValues(DataClass dataObj, String value, String key, Optional<String> subCat) throws IOException {
        if (key.equals(ConverterHelpClass.toDcatAboutString)) {
            addAbout(dataObj, value);
        } else {
            addValue(dataObj.dcData, value, key, subCat);
        }
    }
}