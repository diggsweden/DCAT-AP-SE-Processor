// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import se.ams.dcatprocessor.converter.ConverterCatalog;
import se.ams.dcatprocessor.converter.ConverterFiles;
import se.ams.dcatprocessor.models.ApiSource;
import se.ams.dcatprocessor.models.ApiSpecFile;
import se.ams.dcatprocessor.models.Catalog;
import se.ams.dcatprocessor.models.ConverterHelpClass;
import se.ams.dcatprocessor.models.FileStorage;
import se.ams.dcatprocessor.parser.ApiDefinitionParser;
import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.RDFWorker;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.util.Util;

@Service
@Scope("prototype")
public class Manager {

    private final ObjectProvider<RDFWorker> rdfWorkerProvider;
    private final ErrorReporter errorReporter;

    private final ObjectProvider<ConverterFiles> converterFilesProvider;
    private final ObjectProvider<ConverterCatalog> converterCatalogProvider;

    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    
    Catalog catalog;
    List<FileStorage> fileStorages;

    public Manager(
        ObjectProvider<RDFWorker> rdfWorkerProvider,
        ErrorReporter errorReporter,
        ObjectProvider<ConverterFiles> converterFilesProvider,
        ObjectProvider<ConverterCatalog> converterCatalogProvider
    ) {
        this.rdfWorkerProvider = rdfWorkerProvider;
        this.errorReporter = errorReporter;
        this.converterFilesProvider = converterFilesProvider;
        this.converterCatalogProvider = converterCatalogProvider;
    }

    public DcatResult createDcatFromDirectory(String dir) {
        File f = new File(dir);
        
        // Filter out files with wrong file extension
        File[] files = f.listFiles((dir1, name) -> Util.validateFileExtension(name));
        
        if (files == null || files.length == 0) {
            return DcatResult.errors("No API specification files found in: " + dir);
        }
        
        List<ApiSource> sources = new ArrayList<>();
        
        for (File file : files) {
            Path path = file.toPath();
            try {
                sources.add(new ApiSource(path.toString(), Files.readString(path)));
            } catch (IOException e) {
                logger.error("Could not read file: " + path, e);
                return DcatResult.errors("Could not read file: " + path);
            }
        }

        return createDcat(sources);
    }

    public DcatResult createDcatFromFile(String filename) {
        if (!Util.validateFileExtension(filename)) {
            return DcatResult.errors("Invalid file extension: " + filename);
        }

        Path path = Path.of(filename);
        List<ApiSource> sources = new ArrayList<>();

        try {
            sources.add(new ApiSource(path.toString(), Files.readString(path)));
        } catch (IOException e) {
            logger.error("Could not read file: " + filename, e);
            return DcatResult.errors("Could not read file: " + filename);
        }

        return createDcat(sources);
    }

    /**
     * Converts a list of API specifications into a DCAT-AP-SE RDF document.
     *
     * Each source is parsed and converted into a catalog, a file storage, or both.
     * Errors are collected rather than thrown.
     * RDF generation only runs when the conversion produced no errors.
     *
     * @param sources - The API specifications to convert, each carrying a name and its content
     * @return DcatResult - Containing the generated RDF, or an error report if anything failed
     */
    public DcatResult createDcat(List<ApiSource> sources) {
        RDFWorker rdfWorker = rdfWorkerProvider.getObject();

        catalog = new Catalog();
        fileStorages = new ArrayList<>();

        Map<String, String> exceptions = new HashMap<>();
        List<ValidationError> validationErrors = new ArrayList<>();
        String result = "";

        boolean isSingleFile = sources.size() == 1;

        for (ApiSource source : sources) {
            // Creates catalog and fileStorages
            processApiSpec(source, isSingleFile, exceptions, validationErrors);
        }

        try {
            // Only report the generic no-catalog error when nothing more specific explains why the catalog is missing.
            if ((catalog == null || catalog.about == null)
                    && exceptions.isEmpty()
                    && validationErrors.isEmpty()) {
                throw new DcatException("No catalog found. The specification must contain a dcat-catalog block.");
            }
        
            if (validationErrors.isEmpty() && exceptions.isEmpty()) {
                result = rdfWorker.createDcatFile(catalog, fileStorages);
            }
            
        } catch (DcatException e) {
            handleDcatException(e, exceptions, validationErrors);
        } catch (IOException | RuntimeException e) {
            logger.error("Unexpected error while generating RDF", e);
            exceptions.put(ErrorReporter.GENERIC_ERROR_KEY, "Could not generate DCAT.");
        }

        String errorReport = errorReporter.buildErrorReport(exceptions, validationErrors);

        if(!errorReport.isEmpty()){
            return DcatResult.errors(errorReport);
        }

        return DcatResult.success(result);
    }

    /**
     * Parses one API specification and converts it into a catalog, a file storage, or both.
     *
     * @param source - The API specification to convert, carrying its name and content
     * @param isSingleFile - True when this is the only source, meaning it must supply both catalog and datasets
     * @param exceptions - Collects parse failures and system errors, keyed by source name
     * @param validationErrors - Collects validation errors from the converters
     */
    private void processApiSpec(ApiSource source, boolean isSingleFile,
        Map<String, String> exceptions, List<ValidationError> validationErrors) {
        ApiSpecFile apiSpecFile;
        
        try {
            apiSpecFile = ApiDefinitionParser.getApiSpecFile(source);
        } catch (DcatException e) {
            exceptions.put(source.name(), e.getMessage());
            return;
        }

        boolean isCatalogFile = source.name().contains(ConverterHelpClass.catalogFileName);

        if (isSingleFile || isCatalogFile) {
            addCatalog(apiSpecFile, exceptions, validationErrors);
        }
        // Single file and non-catalog files produces FileStorage
        if (isSingleFile || !isCatalogFile) {
            addFileStorage(apiSpecFile, exceptions, validationErrors);
        }
    }

    private void addCatalog(ApiSpecFile apiSpecFile, Map<String, String> exceptions, List<ValidationError> converterErrors) {
        try {
            ConverterCatalog converterCatalog = converterCatalogProvider.getObject();
            catalog = (Catalog) converterCatalog.catalogToDcat(apiSpecFile);
            catalog.fileName = apiSpecFile.name();
        } catch (DcatException e) {
            converterErrors.addAll(e.getValidationResults());
        } catch (Exception e) {
            exceptions.put(apiSpecFile.name(), e.getMessage());
        }
    }

    private void addFileStorage(ApiSpecFile apiSpecFile, Map<String, String> exceptions, List<ValidationError> converterErrors) {
        try {
            ConverterFiles converterFiles = converterFilesProvider.getObject();
            FileStorage fileStorage = (FileStorage) converterFiles.fileToDcat(apiSpecFile);
            fileStorage.fileName = apiSpecFile.name();
            fileStorages.add(fileStorage);
        } catch (DcatException e) {
            converterErrors.addAll(e.getValidationResults());
        } catch (Exception e) {
            exceptions.put(apiSpecFile.name(), e.getMessage());
        }
    }

    private void handleDcatException(DcatException e, Map<String, String> exceptions, List<ValidationError> validationErrors ) {
        List<ValidationError> exceptionValidationErrors = e.getValidationResults();
            
        if (exceptionValidationErrors.isEmpty()) {
            // The exception is a system exception (no ValidationError exists), add message as a general exception
            exceptions.put(ErrorReporter.GENERIC_ERROR_KEY, e.getMessage());
        } else {
            // add validation errors from the exception to validationErrors
            validationErrors.addAll(exceptionValidationErrors);
        }
    }
}