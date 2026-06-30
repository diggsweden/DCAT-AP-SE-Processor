// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.DataClass;
import se.ams.dcatprocessor.models.DataService;
import se.ams.dcatprocessor.models.Organization;

import java.util.Optional;

@Component
@Scope("prototype")
public class ConverterDataService extends Converter {

    /*
     * Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataService dataService = new DataService();
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

            // Do if key is LICENSE_DOCUMENT
            if (key.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
                    } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key, annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    }
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key)) {
                if (subCat.isEmpty()) {
                    createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                } else if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                    createSubset(file, key, annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
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
                    if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataService), key);
                    } else if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = addLanguageValues(file, annotationName, subCat, Optional.of(dataClassLocal), key);
                    }
                }

                if (!hasLanguages) {
                    if (key.equals(VCARD4.ADDRESS.getLocalName())) {
                        handleAddress(file, key, annotationName, organizationLocal, subCat);
                    } else if (file.has(annotationName)) {
                        String value = String.valueOf(file.get(annotationName));
                        if (subCat.isPresent()) {
                            if (subCat.get().contains(DCAT.DATA_SERVICE.getLocalName())) {
                                addValues(dataService, value, key, subCat);
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
            attachDataToParent(subCat, preData, dataService, organizationLocal, dataClassLocal);
        }
    }


    // Attaches the locally built objects (dataService, dataClassLocal, organizationLocal) 
    // to the correct field on the parent (preData or fileHandler)
    private void attachDataToParent(Optional<String> subCat, Optional<DataClass> preData, DataService dataService, Organization organizationLocal, DataClass dataClassLocal) {
        String subCatValue = subCat.get();

        if (subCatValue.contains(DCAT.DATA_SERVICE.getLocalName())) {
            fileHandler.dataService.add(dataService);
            return;
        }

        if (preData.isEmpty()) return;
        DataClass parent = preData.get();
        
        if (subCatValue.contains(DCTERMS.PUBLISHER.getLocalName())) {     
            if (parent.getClass().equals(DataService.class)) {
                ((DataService) parent).agents.add(dataClassLocal);
            } else {
                parent.agent = dataClassLocal;
            }
            
        } else if (subCatValue.equals(DCTERMS.STANDARD.getLocalName())) {       
            parent.dcData.put("dcterms:conformsTo", dataClassLocal.about );       
        } else if (subCatValue.contains(DCAT.CONTACT_POINT.getLocalName())) {
            ((DataService) parent).organizations.add(organizationLocal);
        } else if (subCatValue.contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
            parent.licenseDocuments.add(dataClassLocal);
        } else if (subCatValue.contains(FOAF.DOCUMENT.getLocalName())) {
            parent.documents.add(dataClassLocal);
        }
    }
}
