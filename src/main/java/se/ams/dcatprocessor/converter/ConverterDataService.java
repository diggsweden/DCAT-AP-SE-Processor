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
import org.json.simple.JSONObject;
import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.DataClass;
import se.ams.dcatprocessor.models.DataService;
import se.ams.dcatprocessor.models.Organization;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public class ConverterDataService extends Converter {

    /*
     * Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataService dataService = new DataService();
        Organization organizationLocal = new Organization();
        DataClass dataClassLocal = new DataClass();

        Object[] dcatKeys = subConvert.keySet().toArray();
        Iterator<?> keysInFile = Arrays.stream(dcatKeys).iterator();

        while (keysInFile.hasNext()) {
            Object key = keysInFile.next();
            if (key.equals(ConverterHelpClass.toDcatString) && (subCat.isPresent())) {
                if (!keysInFile.hasNext()) {
                    break;
                }
                key = keysInFile.next();
            }

            // Get tag-name for api-spec
            String annotationName = (String) ((JSONObject) subConvert.get(key)).get(ConverterHelpClass.toDcatString);

            // Check if tag is Mandatory
            boolean isMandatory;
            String mandatoryKey = key.toString();
            if (subCat.isPresent()) {
                mandatoryKey = subCat + "-" + key;
            }
            isMandatory = jsonObjectMandatoryDcat.containsKey(mandatoryKey);

            // Do if key is LICENSE_DOCUMENT
            if (key.toString().equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                        createSubset(file, key.toString(), annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
                    } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key.toString(), annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    }
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key.toString())) {
                if (subCat.isEmpty()) {
                    createSubset(file, key.toString(), annotationName, Optional.empty(), Optional.empty(), isMandatory);
                } else if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                    createSubset(file, key.toString(), annotationName, Optional.of(dataService), Optional.empty(), isMandatory);
                } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                    createSubset(file, key.toString(), annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key.toString(), annotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            /*
             * Checks for language strings to add them correctly */
            else {
                boolean hasLanguages = false;
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DATA_SERVICE.getLocalName())) {
                        hasLanguages = loopLanguage(file, annotationName, subCat, Optional.of(dataService), key.toString());
                    } else if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = loopLanguage(file, annotationName, subCat, Optional.of(dataClassLocal), key.toString());
                    }
                }

                if (!hasLanguages) {
                    if (key.equals(VCARD4.ADDRESS.getLocalName())) {
                        if (file.containsKey(annotationName)) {
                            String value = String.valueOf(file.get(annotationName));
                            addValues(organizationLocal, value, key.toString(), subCat);
                        } else {
                            loopObject(file, key.toString(), annotationName, organizationLocal, subCat);
                        }
                    } else if (file.containsKey(annotationName)) {
                        String value = String.valueOf(file.get(annotationName));
                        if (subCat.isPresent()) {
                            if (subCat.get().contains(DCAT.DATA_SERVICE.getLocalName())) {
                                addValues(dataService, value, key.toString(), subCat);
                            } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                                addValues(organizationLocal, value, key.toString(), subCat);
                            } else if (ConverterHelpClass.isNestedObjects(subCat.get())) {
                                addValues(dataClassLocal, value, key.toString(), subCat);
                            }
                        }
                    } else if (isMandatory) {
                        if (subCat.isPresent()) {
                            this.errors.add("Errormessage: " + annotationName + " in " + subCat.get() + " is Mandatory");
                        } else {
                            this.errors.add("Errormessage: " + annotationName + " is Mandatory");
                        }
                    }
                }
            }
        }
        if (subCat.isPresent()) {
            if (subCat.get().contains(DCAT.DATA_SERVICE.getLocalName())) {
                fileHandler.dataService.add(dataService);
            } else if (subCat.get().contains(DCTERMS.PUBLISHER.getLocalName())) {
                if (preData.isPresent()) {
                    if (preData.get().getClass().equals(DataService.class)) {
                        ((DataService) preData.get()).agents.add(dataClassLocal);
                    } else {
                        preData.get().agent = dataClassLocal;
                    }
                }
            } else if (subCat.get().equals(DCTERMS.STANDARD.getLocalName())) {
                if (preData.isPresent()) {
                    preData.get().dcData.put("dcterms:conformsTo",dataClassLocal.about );
//                preData.ifPresent(dataClass -> ((DataService) dataClass).conformsTo.add(dataClassLocal));
                }
            } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataService) dataClass).organizations.add(organizationLocal));
            } else if (subCat.get().contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                preData.ifPresent(dataClass -> dataClass.licenseDocuments.add(dataClassLocal));
            } else if (subCat.get().contains(FOAF.DOCUMENT.getLocalName())) {
                preData.ifPresent(dataClass -> dataClass.documents.add(dataClassLocal));
            }
        }
    }

    @Override
    void loopData(JSONObject file, String key, String AnnotationName, String newKey, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        JSONObject subToConvert = ((JSONObject) orgConvert.get(key));
        subToConvert.remove(ConverterHelpClass.toDcatMandatoryString);
        if (subToConvert.keySet().size() > 0) {
            JSONObject subJsonFile = ((JSONObject) file.get(AnnotationName));
            processToDcat(subToConvert, subJsonFile, Optional.ofNullable(newKey), preData, preDist);
        }
    }
}