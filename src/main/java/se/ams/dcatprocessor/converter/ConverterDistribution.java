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

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.json.JSONObject;
import se.ams.dcatprocessor.models.*;
import se.ams.dcatprocessor.rdf.namespace.SPDX;

import java.util.Optional;


public class ConverterDistribution extends Converter {

    /* Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataSet dataSet = new DataSet();
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

            // Do if key is DISTRIBUTION
            if (key.equals(DCAT.DISTRIBUTION.getLocalName())) {
                createSubset(file, key, annotationName, Optional.of(dataSet), Optional.of(distribution), isMandatory);
            }
            // Do if key is LICENSE_DOCUMENT
            else if (key.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    } else {
                        createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.of(distribution), isMandatory);
                    }
                } else {
                    createSubset(file, key, annotationName, preData, Optional.of(distribution), isMandatory);
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key)) {
                if (subCat.isEmpty()) {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                } else if (subCat.get().equals(DCAT.DISTRIBUTION.getLocalName())) {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.of(distribution), isMandatory);
                } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                    createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            
            // Checks for language strings to add them correctly
            else {
                boolean hasLanguages;
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DISTRIBUTION.getLocalName())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(distribution), key);
                    } else if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataClassLocal), key);
                    } else {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.ofNullable(catalog), key);
                    }
                } else {
                    hasLanguages = addLanguageValues(file, annotationName, Optional.empty(), Optional.ofNullable(catalog), key);
                }
                if (!hasLanguages) {
                    if (key.equals(VCARD4.ADDRESS.getLocalName())) {
                        handleAddress(file, key, annotationName, organizationLocal, subCat);
                    } else if (file.has(annotationName)) {
                        String value = String.valueOf(file.get(annotationName));
                        if (subCat.isPresent()) {
                            if (subCat.get().contains(DCAT.DATASET.getLocalName())) {
                                addValues(dataSet, value, key, subCat);
                            } else if (subCat.get().contains(DCAT.DISTRIBUTION.getLocalName())) {
                                addValues(distribution, value, key, subCat);
                            } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
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
            attachDataToParent(subCat, preData, preDist, distribution, organizationLocal, dataClassLocal);
        }
    }

    
    // Attaches the locally built objects (dataClassLocal, distribution, organizationLocal) 
    // to the correct field on the parent context (preData or preDist)
    private void attachDataToParent(Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist, Distribution distribution, 
        Organization organizationLocal, DataClass dataClassLocal) {
        
        String subCatValue = subCat.get();
        DataClass parentData = preData.orElse(null);
        DataClass parentDist = preDist.orElse(null);

        if (subCatValue.contains(DCTERMS.PUBLISHER.getLocalName()) && parentData != null) {
            parentData.agents.add(dataClassLocal);
        } else if (subCatValue.equals(SPDX.CHECKSUM.getLocalName()) && parentDist != null) {
            dataClassLocal.dcData.put("spdx:algorithm", "http://spdx.org/rdf/terms#checksumAlgorithm_sha1");
            ((Distribution) parentDist).checksum = dataClassLocal;
        } else if (subCatValue.equals(DCTERMS.RIGHTS_STATEMENT.getLocalName()) && parentDist != null) {
            ((Distribution) parentDist).rights = dataClassLocal;
        } else if (subCatValue.equals(DCTERMS.STANDARD.getLocalName()) && parentDist != null) {
            parentDist.dcData.put("dcterms:conformsTo", dataClassLocal.about);
        } else if (subCatValue.equals(DCTERMS.SPATIAL.getLocalName()) && parentData != null) {
            ((DataSet) parentData).spatial.add(dataClassLocal);
        } else if (subCatValue.contains(DCAT.DISTRIBUTION.getLocalName()) && parentData != null) {
            ((DataSet) parentData).dcat_distribution.add(distribution);
        } else if (subCatValue.contains(DCAT.CONTACT_POINT.getLocalName()) && parentData != null) {
            ((DataService) parentData).organizations.add(organizationLocal);
        } else if (subCatValue.contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
            if (parentDist != null) {
                parentDist.licenseDocuments.add(dataClassLocal);
            } else if(parentData != null){
                parentData.licenseDocuments.add(dataClassLocal);
            }          
        } else if (subCatValue.contains(FOAF.DOCUMENT.getLocalName())) {
            if (parentDist != null) {
                parentDist.documents.add(dataClassLocal);
            } else if(parentData != null){
                parentData.documents.add(dataClassLocal);
            }
        }
    }
}
