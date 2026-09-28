// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.converter;

import java.util.Optional;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.models.ApiSpecFile;
import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.DataClass;
import se.ams.dcatprocessor.models.DataService;
import se.ams.dcatprocessor.models.DataSet;
import se.ams.dcatprocessor.rdf.DcatException;

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
    public DataClass fileToDcat(ApiSpecFile apiSpecFile) throws Exception {
        this.sourceFilename = apiSpecFile.name();
        setConvertAndMandatoryFile(ConverterHelpClass.uriToDcat);
        primaryClassesToDcat(apiSpecFile);
        
        if (!errors.isEmpty()) {
            throw new DcatException("Error converting file " + apiSpecFile.name(), errors);
        }
        return fileHandler;
    }
    
    /*
     * This method runs at the top level of an API spec and (if present) creates the primary-class blocks:
     * - dcat-datasetseries
     * - dcat-dataset
     * - dcat-dataservice
     */
    void primaryClassesToDcat(ApiSpecFile apiSpecFile) throws Exception {

        for (String key : orgConvert.keySet()) {

            // Get tag-name for api-spec
            String annotationName = orgConvert.getJSONObject(key).getString(ConverterHelpClass.toDcatString);
            boolean isMandatory = isKeyMandatory(key,  Optional.empty());
            
            // Do if key is DATASETSERIES
            if (key.equals(DCAT.DATASET_SERIES.getLocalName())) { 
                converterDatasetSeries.sourceFilename = this.sourceFilename;
                converterDatasetSeries.orgConvert = orgConvert;
                converterDatasetSeries.fileHandler = fileHandler;
                converterDatasetSeries.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;

                // builds the complete DatasetSeries block with every tag and nested objects
                converterDatasetSeries.createSubset(apiSpecFile.content(), key, annotationName, Optional.empty(), Optional.empty(), isMandatory);
                this.errors.addAll(converterDatasetSeries.errors);
            }
            // Do if key is DATASET
            else if (key.equals(DCAT.DATASET.getLocalName())) {
                convertDataSet.sourceFilename = this.sourceFilename;
                convertDataSet.orgConvert = orgConvert;
                convertDataSet.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataSet.fileHandler = fileHandler;

                // builds the complete Dataset block with every tag and nested objects
                convertDataSet.createSubset(apiSpecFile.content(), key, annotationName, Optional.of(new DataSet()), Optional.empty(), isMandatory);               
                this.errors.addAll(convertDataSet.errors);
            }

            // Do if key is DATASERVICE
            else if (key.equals(DCAT.DATA_SERVICE.getLocalName())) {
                convertDataService.sourceFilename = this.sourceFilename;
                convertDataService.orgConvert = orgConvert;
                convertDataService.jsonObjectMandatoryDcat = jsonObjectMandatoryDcat;
                convertDataService.fileHandler = fileHandler;

                // builds the complete DataService block with every tag and nested objects
                convertDataService.createSubset(apiSpecFile.content(), key, annotationName, Optional.of(new DataService()), Optional.empty(), isMandatory);
                this.errors.addAll(convertDataService.errors);
            }
        }  
    }
}
