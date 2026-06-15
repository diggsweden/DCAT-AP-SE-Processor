// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.json.JSONObject;

import se.ams.dcatprocessor.rdf.CardinalityHandler;
import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.SingleInputValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;
import se.ams.dcatprocessor.util.DcatPropertyHandler;

public class TestHelper {
	
	public static final String PS = File.separator;
	
	/**
	 * Filepaths used in many tests
	 */
	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String EXTENSION_DIR = TestHelper.PS + "src" + TestHelper.PS + "test" + TestHelper.PS + "resources" + TestHelper.PS;
	public static final String TEST_FILE_DIR = USER_DIR + EXTENSION_DIR;
	public static final String TARGET_FILE_DIR = USER_DIR + TestHelper.PS + "target" + TestHelper.PS + "classes" + TestHelper.PS;
	public static final String TARGET_FILE_DIR_UP_ONE_LEVEL = USER_DIR + TestHelper.PS + "target" + TestHelper.PS;
	public static final String TEST_DECAT_SPECIFICATION_PROPERTIES_FILE = TARGET_FILE_DIR + "dcat_specification.properties";
	public static final String DECAT_SPECIFICATION_PROPERTIES_FILE = TestHelper.doubleSeparator(TARGET_FILE_DIR + "dcat_specification.properties");
	public static final String DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED = TestHelper.doubleSeparator(TARGET_FILE_DIR_UP_ONE_LEVEL + "dcat_specification.properties");
	
	
	public static void assertOneValidationError(Map<String, List<ValidationError>> validationErrors, String fileName, ErrorType errorType, String key, String value, String description) {
		Set<String> keySet = validationErrors.keySet();
		assertEquals(1, keySet.size());
		ValidationError validationError = validationErrors.get(keySet.iterator().next()).get(0);
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
		
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		//Validation generated no errors
		assertTrue(multipleURIValidator.validate());
		
		//And there are no ValidationErrors stored
		assertTrue(!validationErrorStorage.hasValidationErrors());
	}
	
	public static void assertFileNameSetValidationOkAndZeroValidationErrors(SingleInputValidator singleInputValidator, String fileName, String key, String value) {
		
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		//Validation generated no errors	
		assertTrue(singleInputValidator.validateData(key, value));
		
		//Filename is set
		assertEquals(fileName, singleInputValidator.getCurrentFileName());
		
		//And there are no ValidationErrors stored
		assertTrue(!validationErrorStorage.hasValidationErrors());
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
	 * Set the instance of singelton classes to null, to force them to re-instansiate
	 */
	public static void resetSingeltons() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        DcatPropertyHandler.resetInstance();
        SingleInputValidator.resetInstance();
		CardinalityHandler.resetInstance();
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
}
