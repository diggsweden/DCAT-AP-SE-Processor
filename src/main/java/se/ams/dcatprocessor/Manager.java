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

package se.ams.dcatprocessor;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import se.ams.dcatprocessor.converter.Converter;
import se.ams.dcatprocessor.converter.ConverterCatalog;
import se.ams.dcatprocessor.converter.ConverterFiles;
import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.Catalog;
import se.ams.dcatprocessor.parser.ApiDefinitionParser;
import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.RDFWorker;
import se.ams.dcatprocessor.models.FileStorage;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Manager {

    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    Catalog catalog = new Catalog();
    List<FileStorage> fileStorages = new ArrayList<>();
    RDFWorker rdfWorker = new RDFWorker();

    public String createDcatFromDirectory(String dir) throws Exception {
        ConversionConfig config = ConversionConfig.fromDirectory(Path.of(dir));
        return config.isValid()? this.createDcat(config) : "Hittade inga filer";
    }

    public List<Result> createFromList(List<MultipartFile> apiFiles, Model model) {
        List<Result> results = new ArrayList<>();
        ConversionConfig config = ConversionConfig.fromList(apiFiles, results);
        try {
            results.add(new Result(null, createDcat(config)));
        } catch (Exception e) {
            results.add(new Result(null, e.getMessage()));
            e.printStackTrace();
        }
        model.addAttribute("results", results);
        return results;
    }

    private void printToFile(String string, String fileName) throws Exception {
        FileOutputStream fos = new FileOutputStream(fileName);
        try {
            fos.write(string.getBytes());
        }
        finally {
            fos.close();
        }
    }

    public String createDcat(MultiValuedMap<String, String> apiSpecMap) throws Exception {
        return createDcat(ConversionConfig.fromApiSpecMap(apiSpecMap));
    }
    
    public String createDcat(ConversionConfig config) throws Exception {
        ConverterCatalog catalogConverter = new ConverterCatalog();
        HashMap<String, String> exceptions = new HashMap<>();
        Map<String, List<ValidationError>> validationErrorsPerFileMap = new HashMap<>();
        MultiValuedMap<String, String> apiSpecMap = config.getApiSpecMap();
        
        String result = "Kunde inte generera en dcat fil";
        for (String apiFileName : apiSpecMap.keySet()) {
            Collection<String> api = apiSpecMap.get(apiFileName);
            for (String apiSpecString : api) {
                JSONObject jsonObjectFile = ApiDefinitionParser.getApiJsonString(
                    apiSpecString, config.getWorkDir().resolve("output.json"));

                // Creates both Catalog and other spec from the same file
                if (apiSpecMap.size() == 1) {
                    try {
                        catalog = (Catalog) catalogConverter.catalogToDcat(jsonObjectFile);
                        catalog.fileName = apiFileName;
                    } catch (Exception e) {
                        exceptions.put(apiFileName, e.fillInStackTrace().getMessage());
                    }
                    try {
                        ConverterFiles filesConverter = new ConverterFiles();
                        FileStorage fileStorage = (FileStorage) filesConverter.fileToDcat(jsonObjectFile);
                        fileStorage.fileName = apiFileName;
                        fileStorages.add(fileStorage);
                    } catch (Exception e) {
                        exceptions.put(apiFileName, e.fillInStackTrace().getMessage());
                    }
                    // Creates Catalog from specific file
                } else if (apiFileName.contains(ConverterHelpClass.catalogFileName)) {
                    try {
                        catalog = (Catalog) catalogConverter.catalogToDcat(jsonObjectFile);
                        catalog.fileName = apiFileName;
                    } catch (Exception e) {
                        exceptions.put(apiFileName, e.fillInStackTrace().getMessage());
                    }
                    // Creates all other spec besides Catalog from file
                } else {
                    try {
                        ConverterFiles filesConverter = new ConverterFiles();
                        FileStorage fileStorage;
                        fileStorage = (FileStorage) filesConverter.fileToDcat(jsonObjectFile);
                        fileStorage.fileName = apiFileName;
                        fileStorages.add(fileStorage);
                    } catch (Exception e) {
                        exceptions.put(apiFileName, e.fillInStackTrace().getMessage());
                    }
                }
            }
        }
        try {
            // Creates dcat file if catalog exist
            if (catalog != null && catalog.about != null) {
                result = rdfWorker.createDcatFile(catalog, fileStorages);
            }
        } catch (DcatException e) {
            if (e.getValidationResults().isEmpty()) {
                exceptions.put("RDFWorker", e.fillInStackTrace().getMessage());
            } else {
                validationErrorsPerFileMap = e.getValidationResults();
            }
        }
        StringBuilder exceptionResult = new StringBuilder();

        // True if ApiDefinitionParser or Converter return errors
        if (!exceptions.isEmpty()) {
            exceptionResult.append("\n");
            exceptions.forEach((key, value) -> exceptionResult.append(key).append(":\n").append(value).append("\n\n"));
        }

        // True if RDFWorker return errors
        if (ValidationErrorStorage.getInstance().hasValidationErrors()) {
            exceptionResult.append("\n");
            validationErrorsPerFileMap.forEach((key, value) -> {
                exceptionResult.append(key).append(":\n");

                for (ValidationError validationError : value) {
                    exceptionResult.append("Errortype: ").append(validationError.getErrorType()).append(" Description: ").append(validationError.getDescription()).append("\n");
                }
                exceptionResult.append("\n");
            });
        }

        ValidationErrorStorage.getInstance().resetErrors();
        Converter.deleteErrors();

        if (exceptionResult.length() > 0) {
            exceptionResult.append("Check DCAT-AP-SE specification for info. https://docs.dataportal.se/dcat/sv/\n---------------------------------\n");
            logger.error("There are Errors in the following files: \n" + exceptionResult);
            return "There are Errors in the following files: \n" + exceptionResult;
        }
        if (result.contains("RDF")) {
            printToFile(result, config.getWorkDir().resolve("dcat.rdf").toString());
        }
        return result;
    }
}
