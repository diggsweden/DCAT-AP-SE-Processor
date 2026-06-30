// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.models.*;

import java.util.*;

@Component
@Scope("prototype")
public class ConverterFiles extends Converter {
    
    private ConverterDatasetSeries converterDatasetSeries;
    private ConverterDataSet convertDataSet;
    private ConverterDataService convertDataService;
    
    public ConverterFiles(
        ConverterDatasetSeries converterDatasetSeries, 
        ConverterDataSet convertDataSet, 
        ConverterDataService convertDataService
    ) {
        this.converterDatasetSeries = converterDatasetSeries;
        this.convertDataSet = convertDataSet;
        this.convertDataService = convertDataService;
    }
    
    /* Takes one specFile for a Api and creates an Object with ApiSpec tags, DataSet, DataService and DatasetSeries. */
    public DataClass fileToDcat(JSONObject apiSpec) throws Exception {
        setConvertAndMandatoryFile(ConverterHelpClass.uriToDcat);
        primaryClassesToDcat(apiSpec);
        
        if (errors.size() > 0) {
            StringBuilder errorResult = new StringBuilder();
            for (String error : errors) {
                errorResult.append(error).append("\n");
            }
            throw new Exception(errorResult.toString());
        }
        return fileHandler;
    }
    
    /*
     * This method maps no fields of its own. It runs only at the top level of
     * an API spec and (if pressent) creates the primary-class blocks:
     * - dcat-datasetseries
     * - dcat-dataset
     * - dcat-dataservice
     */
    void primaryClassesToDcat(JSONObject file) throws Exception {

        for (String key : orgConvert.keySet()) {

            // Get tag-name for api-spec
            String annotationName = orgConvert.getJSONObject(key).getString(ConverterHelpClass.toDcatString);
            boolean isMandatory = isKeyMandatory(key,  Optional.empty());
            
            // Do if key is DATASETSERIES
            if (key.equals(DCAT.DATASET_SERIES.getLocalName())) { 
                converterDatasetSeries.orgConvert = orgConvert;
                converterDatasetSeries.fileHandler = fileHandler;
                converterDatasetSeries.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;

                // builds the complete DatasetSeries block with every tag and nested objects
                converterDatasetSeries.createSubset(file, key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
            }
            // Do if key is DATASET
            else if (key.equals(DCAT.DATASET.getLocalName())) {
                convertDataSet.orgConvert = orgConvert;
                convertDataSet.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataSet.fileHandler = fileHandler;

                // builds the complete Dataset block with every tag and nested objects
                convertDataSet.createSubset(file, key, annotationName, Optional.of(new DataSet()), Optional.empty(), isMandatory);
            }

            // Do if key is DATASERVICE
            else if (key.equals(DCAT.DATA_SERVICE.getLocalName())) {
                convertDataService.orgConvert = orgConvert;
                convertDataService.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataService.fileHandler = fileHandler;

                // builds the complete DataService block with every tag and nested objects
                convertDataService.createSubset(file, key, annotationName, Optional.of(new DataService()), Optional.empty(), isMandatory);
            }
        }  
    }
}
