// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import se.ams.dcatprocessor.controller.Result;
import se.ams.dcatprocessor.converter.Converter;
import se.ams.dcatprocessor.converter.ConverterCatalog;
import se.ams.dcatprocessor.converter.ConverterFiles;
import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.Catalog;
import se.ams.dcatprocessor.parser.ApiDefinitionParser;
import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.RDFWorker;
import se.ams.dcatprocessor.models.FileStorage;
import se.ams.dcatprocessor.rdf.validate.RDFValidationError;
import se.ams.dcatprocessor.rdf.validate.RDFValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@Scope("prototype")
public class Manager {

    private final ObjectProvider<RDFWorker> rdfWorkerProvider;
    private final ErrorReporter errorReporter;
    private final RDFValidator rdfValidator;

    private final ObjectProvider<ConverterFiles> converterFilesProvider;
    private final ObjectProvider<ConverterCatalog> converterCatalogProvider;

    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    
    Catalog catalog = new Catalog();
    List<FileStorage> fileStorages = new ArrayList<>();

    public Manager(
        ObjectProvider<RDFWorker> rdfWorkerProvider,
        ErrorReporter errorReporter,
        RDFValidator rdfValidator,
        ObjectProvider<ConverterFiles> converterFilesProvider,
        ObjectProvider<ConverterCatalog> converterCatalogProvider
    ) {
        this.rdfWorkerProvider = rdfWorkerProvider;
        this.errorReporter = errorReporter;
        this.rdfValidator = rdfValidator;
        this.converterFilesProvider = converterFilesProvider;
        this.converterCatalogProvider = converterCatalogProvider;
    }

    public String createDcatFromDirectory(String dir) throws Exception {
        // create new file
        File f = new File(dir);
        String result = "Hittade inga filer";

        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();

        // returns pathnames for files and directory
        File[] files = f.listFiles((dir1, name) -> name.endsWith(".raml") || name.endsWith(".yaml") || name.endsWith(".json"));

        // for each file in file array
        if (files != null && files.length > 0) {
            for (File file : files) {
                Path path = Path.of(String.valueOf(file));
                try {
                    String content = Files.readString(path);
                    apiSpecMap.put(path.toString(), content);
                } finally {
                }
            }
            result = this.createDcat(apiSpecMap);
        }
        return result;
    }

    public String createDcatFromFile(String filename) {
        if (!validateFileExtension(filename)) {
            return "Invalid file extension: " + filename;
        }

        Path path = Path.of(filename);
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        String result;

        try {
            String content = Files.readString(path);
            apiSpecMap.put(path.toString(), content);
            result = createDcat(apiSpecMap);
            if (result.isEmpty()) throw new RuntimeException("Kunde inte generera en dcat fil");
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    public List<Result> createFromList(List<MultipartFile> apiFiles, Model model) {
        List<Result> results = new ArrayList<>();
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        String result;

        /* Generate DCAT-AP-SE from file */
        for (MultipartFile apiFile : apiFiles) {
            if (!apiFile.isEmpty()) {
                String apiSpecificationFromFile;
                Scanner scanner;
                try {
                    scanner = new Scanner(apiFile.getInputStream(), StandardCharsets.UTF_8.name());
                    apiSpecificationFromFile = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    apiSpecMap.put(apiFile.getOriginalFilename(), apiSpecificationFromFile);
                } catch (Exception e) {        //Catch and show processing errors in web-gui
                    result = e.getMessage();
                    results.add(new Result(result));
                    e.printStackTrace();
                }
            }
        }
        try {
            result = createDcat(apiSpecMap);
        } catch (Exception e) {
            result = e.getMessage();
            results.add(new Result(result));
            e.printStackTrace();
        }
        results.add(new Result(result));

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
        RDFWorker rdfWorker = rdfWorkerProvider.getObject();
        resetValidationErrors();

        HashMap<String, String> exceptions = new HashMap<>();
        Map<String, List<ValidationError>> validationErrorsPerFileMap = new HashMap<>();
        List<RDFValidationError> rdfValidationErrors = new ArrayList<>();
        String result = "Kunde inte generera en dcat fil";

        for (String apiFileName : apiSpecMap.keySet()) {
            Collection<String> api = apiSpecMap.get(apiFileName);
            for (String apiSpecString : api) {
                JSONObject jsonObjectFile = ApiDefinitionParser.getApiJsonString(apiSpecString);

                boolean isSingleFile = apiSpecMap.size() == 1;
                boolean isCatalogFile = apiFileName.contains(ConverterHelpClass.catalogFileName);

                if(isSingleFile || isCatalogFile){
                    addCatalog(jsonObjectFile, apiFileName, exceptions);
                }
                // Single file and non-catalog files produces FileStorage
                if(isSingleFile || !isCatalogFile){
                    addFileStorage(jsonObjectFile, apiFileName, exceptions);
                }
            }
        }
        try {
            // Creates dcat file if catalog exist
            if (catalog != null && catalog.about != null) {
                result = rdfWorker.createDcatFile(catalog, fileStorages);

                // Validate RDF
                rdfValidationErrors = rdfValidator.validate(result);
            }
        } catch (DcatException e) {
            // holds validation errors
            validationErrorsPerFileMap = e.getValidationResults();

            // system exceptions
            if(validationErrorsPerFileMap == null){
                exceptions.put("Error", e.getMessage());
            }
            // RDFWorker exceptions
            else if (e.getValidationResults().isEmpty()) {
                exceptions.put("RDFWorker", e.fillInStackTrace().getMessage());
            }
        }

        String errorReport = errorReporter.buildErrorReport(exceptions, validationErrorsPerFileMap, rdfValidationErrors);

        // If any errors, return report
        if(!errorReport.isEmpty()){
            logger.error(errorReport);
            return errorReport;
        }

        if (result.contains("RDF")) {
            printToFile(result, "dcat.rdf");
        }
        return result;
    }

    private void addCatalog(JSONObject json, String fileName, Map<String, String> exceptions) {
        try {
            ConverterCatalog converterCatalog = converterCatalogProvider.getObject();
            catalog = (Catalog) converterCatalog.catalogToDcat(json);
            catalog.fileName = fileName;
        } catch (Exception e) {
            exceptions.put(fileName, e.fillInStackTrace().getMessage());
        }
    }

    private void addFileStorage(JSONObject json, String fileName, Map<String, String> exceptions) {
        try {
            ConverterFiles converterFiles = converterFilesProvider.getObject();
            FileStorage fileStorage = (FileStorage) converterFiles.fileToDcat(json);
            fileStorage.fileName = fileName;
            fileStorages.add(fileStorage);
        } catch (Exception e) {
            exceptions.put(fileName, e.fillInStackTrace().getMessage());
        }
    }

    private boolean validateFileExtension(String filename){
        if (filename.endsWith(".raml") || filename.endsWith(".yaml") || filename.endsWith(".json")) {
            return true;
        }
        return false;
    }

    private void resetValidationErrors(){
        ValidationErrorStorage.getInstance().resetErrors();
        Converter.deleteErrors();
    }
}