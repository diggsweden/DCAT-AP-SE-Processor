// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.json.JSONObject;
import se.ams.dcatprocessor.models.*;

import java.util.*;

public class ConverterFiles extends Converter {

    /* Takes one specFile for a Api and creates an Object with ApiSpec tags, DataSet, DataService, Distribution etc. */
    @Override
    public DataClass fileToDcat(JSONObject apiSpec) throws Exception {
        setConvertAndMandatoryFile(ConverterHelpClass.uriToDcat);
        processToDcat(orgConvert, apiSpec, Optional.empty(), Optional.empty(), Optional.empty());

        if (errors.size() > 0) {
            StringBuilder errorResult = new StringBuilder();
            for (String error : errors) {
                errorResult.append(error).append("\n");
            }
            throw new Exception(errorResult.toString());
        }
        return fileHandler;
    }

    /* Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataSet dataSet = new DataSet();
        DataService dataService = new DataService();
        Distribution distribution = new Distribution();
        Organization organizationLocal = new Organization();
        DataClass dataClassLocal = new DataClass();

        for (String key : subConvert.keySet()) {

            if (key.equals(ConverterHelpClass.toDcatString) && subCat.isPresent()) {
                continue;
            }

            // Get tag-name for api-spec
            String annotationName = subConvert.getJSONObject(key).getString(ConverterHelpClass.toDcatString);

            // Check if tag is Mandatory
            boolean isMandatory = isKeyMandatory(key, subCat);
            
            // Do if key is DATASETSERIES
            if (key.equals(DCAT.DATASET_SERIES.getLocalName())) { 
                ConverterDatasetSeries converterDatasetSeries = new ConverterDatasetSeries();
                converterDatasetSeries.orgConvert = orgConvert;
                converterDatasetSeries.fileHandler = fileHandler;
                converterDatasetSeries.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                converterDatasetSeries.createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
            }
            // Do if key is DATASET
            else if (key.contains(DCAT.DATASET.getLocalName())) {
                ConverterDataSet convertDataSet = new ConverterDataSet();
                convertDataSet.orgConvert = orgConvert;
                convertDataSet.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataSet.fileHandler = fileHandler;
                convertDataSet.createSubset(file, key, annotationName, Optional.of(dataSet), Optional.empty(), isMandatory);
            }
            // Do if key is LICENSE_DOCUMENT
            else if (key.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
                    } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    } else {
                        createSubset(file, key, annotationName, Optional.empty(), Optional.of(distribution), isMandatory);
                    }
                } else {
                    createSubset(file, key, annotationName, preData, Optional.of(distribution), isMandatory);
                }
            }
            // Do if key is DATASERVICE
            else if (key.contains(DCAT.DATA_SERVICE.getLocalName())) {
                ConverterDataService convertDataService = new ConverterDataService();
                convertDataService.orgConvert = orgConvert;
                convertDataService.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataService.fileHandler = fileHandler;
                convertDataService.createSubset(file, key, annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key)) {
                if (subCat.isEmpty()) {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                    createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            
            // Checks for language strings to add them correctly
            else {
                boolean hasLanguages = false;
                if (subCat.isPresent()) {
                    if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataClassLocal), key);
                    }
                }
                if (!hasLanguages) {
                    if (key.equals(VCARD4.ADDRESS.getLocalName())) {
                        handleAddress(file, key, annotationName, organizationLocal, subCat);
                    } else if (file.has(annotationName)) {
                        String value = String.valueOf(file.get(annotationName));
                        if (subCat.isPresent()) {
                            if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                                addValues(organizationLocal, value, key, subCat);
                            } else if (ConverterHelpClass.isNestedObjects(subCat.get())) {
                                addValues(dataClassLocal, value, key, subCat);
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

    
    // Attaches the locally built dataClassLocal to the correct field on preData
    private void attachDataToParent(Optional<String> subCat, Optional<DataClass> preData, DataClass dataClassLocal) {
        if (preData.isEmpty()) return;
        
        String subCatValue = subCat.get();
        DataClass parent = preData.get();
        
        if (subCatValue.contains(DCTERMS.PUBLISHER.getLocalName())) {
            parent.agent = dataClassLocal;
        } else if (subCatValue.contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
            parent.licenseDocuments.add(dataClassLocal);
        } else if (subCatValue.contains(FOAF.DOCUMENT.getLocalName())) {
            parent.documents.add(dataClassLocal);
        }
    }
}