// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.testutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.SingleInputValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;

public class TestHelper {
	
	public static final String PS = File.separator;
	
	/**
	 * Filepaths used in many tests
	 */
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String EXTENSION_DIR = TestHelper.PS + "src" + TestHelper.PS + "test" + TestHelper.PS + "resources" + TestHelper.PS;
	public static final String TEST_FILE_DIR = USER_DIR + EXTENSION_DIR;

	public static void assertOneValidationError(List<ValidationError> validationErrors, String fileName, ErrorType errorType, String key, String value, String description) {
		assertEquals(1, validationErrors.size());
		ValidationError validationError = validationErrors.get(0);
		assertValidationError(validationError, fileName, errorType, key, value, description);
	}
	
	public static void assertValidationError(ValidationError validationError, String fileName, ErrorType errorType, String key, String value, String description) {
		assertEquals(errorType, validationError.getErrorType());
		assertEquals(fileName, validationError.getFileName());
		
		if(key != null) {
			assertEquals(key, validationError.getKey());			
		}
		
		if(value != null) {
			assertEquals(value, validationError.getValue());	
		}
		
		assertEquals(description, validationError.getDescription());		
	}

	public static void assertValidationOkAndZeroValidationErrors(MultipleURIValidator multipleURIValidator ) {
		//Validation generated no errors
		assertTrue(multipleURIValidator.validate());
		
		//And there are no ValidationErrors stored
		assertTrue(multipleURIValidator.getValidationErrors().isEmpty());
	}
	
	public static void assertFileNameSetValidationOkAndZeroValidationErrors(SingleInputValidator singleInputValidator, String fileName, String key, String value) {
		
		//Validation generated no errors	
		assertTrue(singleInputValidator.validateData(key, value, "Dataset"));
		
		//Filename is set
		assertEquals(fileName, singleInputValidator.getCurrentFileName());
		
		//And there are no ValidationErrors stored
		assertTrue(singleInputValidator.getValidationErrors().isEmpty());
	}

	public static ValidationError findValidationError(List<ValidationError> validationErrors, ErrorType errorType, String value) {
		for (ValidationError error : validationErrors) {
			boolean valueMatches;
			if (value == null) {
				valueMatches = error.getValue() == null;
			} else {
				valueMatches = value.equals(error.getValue());
			}
			
			if (error.getErrorType() == errorType && valueMatches) {
				return error;
			}
		}
		fail("Expected a ValidationError of type " + errorType + " with value " + value + " but found none");
    	return null; // unreachable, fail() throws
	}
	
	public static void copyFile(String fromFilePath, String toFilePath) throws Exception {
		Path fromFile = Paths.get(fromFilePath);
	    Path toFile = Paths.get(toFilePath);
	    Files.copy(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public static String doubleSeparator(String path) {
		String separator = File.separator;
		return path.replace(separator, separator + separator);
	}

	/**
 	* Test helper for creating mutated variants of an apidef fixture without touching the original files
 	* @param sourceFile the original apidef file (left untouched)
 	* @param tempDir    the destination directory, normally the test's @TempDir
 	* @param mutation   the change to apply to the file's JSON (removing/adding fields)
	* @return path to the mutated copy, ready to pass to createDcatFromFile
	*/
	public static Path copyWith(Path sourceFile, Path tempDir, Consumer<JSONObject> mutation) throws IOException {
	    JSONObject json = new JSONObject(Files.readString(sourceFile));
	    mutation.accept(json);

	    Path target = tempDir.resolve(sourceFile.getFileName());
	    Files.writeString(target, json.toString());

	    return target;
	}

	public static String bundlePathFromApplicationProperties() throws IOException {
		Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
		return properties.getProperty("dcat.bundle-path");
	}
}
