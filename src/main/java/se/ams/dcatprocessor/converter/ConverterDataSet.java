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

import org.eclipse.rdf4j.model.vocabulary.*;
import org.json.simple.JSONObject;
import se.ams.dcatprocessor.models.*;
import se.ams.dcatprocessor.rdf.namespace.SCHEMA;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ConverterDataSet extends Converter {

    /* Process the spec to find elements for dcat-ap-se and add them to an Object to return */
    @Override
    void processToDcat(JSONObject subConvert, JSONObject file, Optional<String> subCat, Optional<DataClass> preData, Optional<DataClass> preDist) throws Exception {
        DataSet dataSet = new DataSet();
        Distribution distribution = new Distribution();
        Organization organizationLocal = new Organization();
        DataClass dataClassLocal = new DataClass();
        DataClass otherAgent = new DataClass();

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
                mandatoryKey = subCat.get() + "-" + key;
            }
            isMandatory = jsonObjectMandatoryDcat.containsKey(mandatoryKey);

            // Do if key is DISTRIBUTION
            if (key.toString().equals(DCAT.DISTRIBUTION.getLocalName())) {
                ConverterDistribution convertDistribution = new ConverterDistribution();
                convertDistribution.orgConvert = orgConvert;
                convertDistribution.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDistribution.fileHandler = fileHandler;
                convertDistribution.createSubset(file, key.toString(), annotationName, Optional.of(dataSet), Optional.of(distribution), isMandatory);
            }
            // Do if key is LICENSE_DOCUMENT
            else if (key.toString().equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key.toString(), annotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    }
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key.toString())) {
                if (subCat.get().equals(DCAT.DATASET.getLocalName())) {
                    createSubset(file, key.toString(), annotationName, Optional.of(dataSet), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key.toString(), annotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            /* Checks for language strings to add them correctly */
            else {
                boolean hasLanguages = false;
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCAT.DATASET.getLocalName())) {
                        hasLanguages = loopLanguage(file, annotationName, subCat, Optional.of(dataSet), key.toString());
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
                            if (subCat.get().contains(DCAT.DATASET.getLocalName())) {
                                addValues(dataSet, value, key.toString(), subCat);
                            } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                                addValues(organizationLocal, value, key.toString(), subCat);
                            } else if (subCat.get().contains(PROV.ATTRIBUTION.getLocalName())) {
                                if (key.toString().equals("dcat:hadRole")) {
                                    addValues(dataClassLocal, value, key.toString(), subCat);
                                }
                                else {
                                    addValues(otherAgent, value, key.toString(), subCat);
                                }
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
            if (subCat.get().contains(DCAT.DATASET.getLocalName())) {
                fileHandler.dcat_dataset.add(dataSet);
            } else if (subCat.get().contains(DCTERMS.PUBLISHER.getLocalName())) {
                preData.get().agent = dataClassLocal;
            } else if (subCat.get().contains(DCTERMS.CREATOR.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataSet) dataClass).creator = dataClassLocal);
            } else if (subCat.get().contains(PROV.ATTRIBUTION.getLocalName())) {
                dataClassLocal.agent = otherAgent;
                preData.ifPresent(dataClass -> ((DataSet) dataClass).otherAgents.add(dataClassLocal));
            } else if (subCat.get().equals(DCTERMS.TEMPORAL.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataSet) dataClass).temporals.add(dataClassLocal));
            } else if (subCat.get().equals(SCHEMA.OFFERS.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataSet) dataClass).offers.add(dataClassLocal));
            } else if (subCat.get().equals(DCTERMS.STANDARD.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataSet) dataClass).conformsTo.add(dataClassLocal));
            } else if (subCat.get().equals(DCTERMS.SPATIAL.getLocalName())) {
                ((DataSet) preData.get()).spatial.add(dataClassLocal);
            } else if (subCat.get().contains(DCAT.QUALIFIED_RELATION.getLocalName())) {
                ((DataSet) preData.get()).qualifiedRelations.add(dataClassLocal);
            } else if (subCat.get().contains(DCAT.CONTACT_POINT.getLocalName())) {
                preData.ifPresent(dataClass -> ((DataSet) dataClass).organizations.add(organizationLocal));
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