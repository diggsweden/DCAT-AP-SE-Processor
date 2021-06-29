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
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
            String AnnotationName = (String) ((JSONObject) subConvert.get(key)).get(ConverterHelpClass.toDcatString);

            // Check if tag is Mandatory
            boolean isMandatory;
            String mandatoryKey = key.toString();
            if (subCat.isPresent()) {
                mandatoryKey = subCat + "-" + key;
            }
            isMandatory = jsonObjectMandatoryDcat.containsKey(mandatoryKey);

            // Do if key is CATALOG
            if (key.toString().contains(DCAT.CATALOG.getLocalName())) {
                createSubset(file, key.toString(), AnnotationName, Optional.empty(), Optional.empty(), isMandatory);
            }
            // Do if key is LICENSE_DOCUMENT
            else if (key.toString().equals(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                if (subCat.isPresent()) {
                    if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                        createSubset(file, key.toString(), AnnotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                    }
                }
            }
            // Do if key is A Nested Object
            else if (ConverterHelpClass.isNestedObjects(key.toString())) {
                if (subCat.isPresent() && subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                    createSubset(file, key.toString(), AnnotationName, Optional.of(dataClassLocal), Optional.empty(), isMandatory);
                } else {
                    createSubset(file, key.toString(), AnnotationName, Optional.empty(), Optional.empty(), isMandatory);
                }
            }
            /*
             * Checks for language strings to add them correctly */
            else {
                boolean hasLanguages;
                if (subCat.isPresent()) {
                    if (ConverterHelpClass.isNestedLanguageObjects(subCat.get())) {
                        hasLanguages = loopLanguage(file, AnnotationName, subCat, Optional.of(dataClassLocal), key.toString());
                    } else {
                        hasLanguages = loopLanguage(file, AnnotationName, subCat, Optional.ofNullable(catalog), key.toString());
                    }
                } else {
                    hasLanguages = loopLanguage(file, AnnotationName, Optional.empty(), Optional.ofNullable(catalog), key.toString());
                }

                if (!hasLanguages) {
                    if (file.containsKey(AnnotationName)) {
                        String value = String.valueOf(file.get(AnnotationName));
                        if (subCat.isPresent()) {
                            if (ConverterHelpClass.isNestedObjects(subCat.get())) {
                                addValues(dataClassLocal, value, key.toString(), subCat);
                            } else {
                                addValues(catalog, value, key.toString(), subCat);
                            }
                        }
                    } else if (isMandatory) {
                        if (subCat.isPresent()) {
                            errors.add("Errormessage: " + AnnotationName + " in " + subCat + " is Mandatory");
                        } else {
                            errors.add("Errormessage: " + AnnotationName + " is Mandatory");
                        }
                    }
                }
            }
        }
        if (subCat.isPresent()) {
            if (subCat.get().contains(DCTERMS.PUBLISHER.getLocalName())) {
                if (preData.isPresent()) {
                    (preData.get()).agents.add(dataClassLocal);
                } else {
                    catalog.publisher = dataClassLocal;
                }
            } else if (subCat.get().equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
                catalog.rights = dataClassLocal;
            } else if (subCat.get().equals(DCTERMS.SPATIAL.getLocalName())) {
                catalog.spatial.add(dataClassLocal);
            } else if (subCat.get().contains(DCTERMS.LICENSE_DOCUMENT.getLocalName())) {
                preData.ifPresent(dataClass -> dataClass.licenseDocuments.add(dataClassLocal));
            }
        }
    }

    /*
     * Sets a value to the correct Object */
    @Override
    void addValue(MultiValuedMap<String, String> valueMap, String value, String key, Optional<String> subCat) throws IOException, ParseException {
        JSONObject jsonSupportiveDcat = null;
        if (subCat.isPresent())
            jsonSupportiveDcat = getSupportiveFile(key, subCat.get());

        String[] splitValue = value.split(";");
        for (String s : splitValue) {
            String mapValue = s;
            if (jsonSupportiveDcat != null) {
                mapValue = mapValue.trim();
                if (jsonSupportiveDcat.containsKey(mapValue)) {
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
    void addValues(DataClass dataObj, String value, String key, Optional<String> subCat) throws IOException, ParseException {
        if (key.equals(ConverterHelpClass.toDcatAboutString)) {
            addAbout(dataObj, value);
        } else {
            addValue(dataObj.dcData, value, key, subCat);
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