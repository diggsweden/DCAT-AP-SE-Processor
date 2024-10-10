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

package se.ams.dcatprocessor.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.SingleInputValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;

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

	public static void assertValidationOkAndZeroValidationErrors(MultipleURIValidator multipleURIValidator, ValidationErrorStorage validationErrorStorage ) {

		//Validation generated no errors
		assertTrue(multipleURIValidator.validate());

		//And there are no ValidationErrors stored
		assertTrue(!validationErrorStorage.hasValidationErrors());
	}

	public static void assertFileNameSetValidationOkAndZeroValidationErrors(SingleInputValidator singleInputValidator, String fileName, String key, String value, ValidationErrorStorage validationErrorStorage) {
		
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

}
