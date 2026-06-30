// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.JSONObject;

import se.ams.dcatprocessor.models.*;

import java.util.Optional;

public class ConverterDatasetSeries extends Converter {

    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DatasetSeries datasetSeries = new DatasetSeries();
        Organization organizationLocal = new Organization();
        DataClass dataClassLocal = new DataClass();

        for (String key : subConvert.keySet()) {

            if (key.equals(ConverterHelpClass.toDcatString)) {
                continue;
            }

            // Get tag-name for api-spec
            String annotationName = subConvert.getJSONObject(key).getString(ConverterHelpClass.toDcatString);
            boolean isMandatory = isKeyMandatory(key, subCat);

            // Do if key is A Nested Object (publisher, contactPoint, conformsTo, temporal, spatial, qualifiedRelation, page)
            if (ConverterHelpClass.isNestedObjects(key)) {
                createSubset(file, key, annotationName, Optional.of(datasetSeries), Optional.empty(), isMandatory);
            }

            else {              
                // Checks for language strings to add them correctly
                boolean hasLanguages = false;
                if (subCat.get().equals(DCAT.DATASET_SERIES.getLocalName())) {
                    hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(datasetSeries), key);
                } else if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                    hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataClassLocal), key);
                }

                if(hasLanguages) {
                    continue;
                }

                if (key.equals(VCARD4.ADDRESS.getLocalName())) {
                    handleAddress(file, key, annotationName, organizationLocal, subCat);
                    continue;
                } 
                
                if (file.has(annotationName)) {
                    String value = String.valueOf(file.get(annotationName));

                    // Target depends on which class the current subCat represents
                    if (subCat.get().contains(DCAT.DATASET_SERIES.getLocalName())) {
                        addValues(datasetSeries, value, key, subCat);
                    } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                        addValues(organizationLocal, value, key, subCat);
                    } else if (ConverterHelpClass.isNestedObjects(subCat.get())) {
                        addValues(dataClassLocal, value, key, subCat);
                    }
                    
                } else if (isMandatory) {
                    addMandatoryError(annotationName, subCat);
                }
            }
        }

        attachDataToParent(subCat, preData, datasetSeries, organizationLocal, dataClassLocal);     
    }

    // Attaches the locally built objects to the correct field on the parent
    // (the series itself, or fileHandler for the outer block).
    private void attachDataToParent(Optional<String> subCat, Optional<DataClass> preData, DatasetSeries datasetSeries,
            Organization organizationLocal, DataClass dataClassLocal) {

        String subCatValue = subCat.get();

        if (subCatValue.contains(DCAT.DATASET_SERIES.getLocalName())) {
            fileHandler.dcat_datasetSeries.add(datasetSeries);
            return;
        }

        DataClass parentData = preData.get();

        if (subCatValue.contains(DCTERMS.PUBLISHER.getLocalName())) {
            parentData.agent = dataClassLocal;
        } else if (subCatValue.equals(DCTERMS.TEMPORAL.getLocalName())) {
            ((DatasetSeries) parentData).temporals.add(dataClassLocal);
        } else if (subCatValue.equals(DCTERMS.STANDARD.getLocalName())) {
            ((DatasetSeries) parentData).conformsTo.add(dataClassLocal);
        } else if (subCatValue.equals(DCTERMS.SPATIAL.getLocalName())) {
            ((DatasetSeries) parentData).spatial.add(dataClassLocal);
        } else if (subCatValue.contains(DCAT.CONTACT_POINT.getLocalName())) {
            ((DatasetSeries) parentData).organizations.add(organizationLocal);
        } else if (subCatValue.contains(FOAF.DOCUMENT.getLocalName())) {
            parentData.documents.add(dataClassLocal);
        } else if (subCatValue.contains(DCAT.QUALIFIED_RELATION.getLocalName())) {
            ((DatasetSeries) parentData).qualifiedRelations.add(dataClassLocal);
        }
    }
}
