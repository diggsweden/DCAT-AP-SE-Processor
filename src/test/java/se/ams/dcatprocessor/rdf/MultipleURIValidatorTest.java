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

package se.ams.dcatprocessor.rdf;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;
import se.ams.dcatprocessor.testutil.TestHelper;

class MultipleURIValidatorTest {

	MultipleURIValidator multipleURIValidator;

	ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
	
	private String fileName1 = "RamlApiSpec1.raml";
	private String fileName2 = "RamlApiSpec2.raml";
	private String fileName3 = "RamlApiSpec3.raml";
	private String fileName4 = "RamlApiSpec4.raml";
	
	private String uri1 = "http://www.arbetsformedligen1.se";
	private String uri2 = "http://www.arbetsformedligen2.se";
	private String uri3 = "http://www.arbetsformedligen3.se";
	private String uri4 = "http://www.arbetsformedligen4.se";
	private String uri5 = "http://www.arbetsformedligen5.se";
	private String uri6 = "http://www.arbetsformedligen6.se";
	
	
	@BeforeEach
	void setup() throws Exception {
		//Reset Singleton before each test
		Field instance = ValidationErrorStorage.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(ValidationErrorStorage.class, null);
        
		multipleURIValidator = new MultipleURIValidator();
		validationErrorStorage = ValidationErrorStorage.getInstance();
	}
	
	//Bad
	//No current file set...expect Exception in return
	@Test
	void testCurrentFileNotSet() throws Exception {
		try {
			multipleURIValidator.addUri(uri1);
			fail("Expected Exception when adding an URI when the filename for the file being validated is not set");
		} catch (Exception e) {
			assertEquals("class se.ams.dcatprocessor.rdf.validate.MultipleURIValidator Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}	
			
	}

	//Bad
	//Submitted URI is null...expect Exception in return
	@Test
	void testSubmittedURIIsNull() throws Exception {
		//Set filename to provoke next error
		multipleURIValidator.setCurrentFileName(fileName1);
		try {
			multipleURIValidator.addUri(null);
			fail("Expected Exception when adding an URI that is null");
		} catch (Exception e) {
			assertEquals("class se.ams.dcatprocessor.rdf.validate.MultipleURIValidator Error validating input data. Reason: Submitted URI is null", e.getMessage());
		}	
			
	}
	
	//Two unique URI within the same file
	@Test
	void testValidationOK1() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2);
		
		//Validation ok and generated no validation errors
		TestHelper.assertValidationOkAndZeroValidationErrors(multipleURIValidator);
	}
	
	//Good
	// Three unique URI within the same file
	@Test
	void testValidationOK2() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2);
		multipleURIValidator.addUri(uri3);

		//Validation ok and generated no validation errors
		TestHelper.assertValidationOkAndZeroValidationErrors(multipleURIValidator);
	}
	
	//Good
	//Two unique URI in different files
	@Test
	void testValidationOK3() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1);
		
		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri2);
		
		//Validation ok and generated no validation errors
		TestHelper.assertValidationOkAndZeroValidationErrors(multipleURIValidator);
	}
	
	// Good
	// Three unique URI in three different files together with other URI:s
	@Test
	void testValidationOK4() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2);

		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri3);
		multipleURIValidator.addUri(uri4);
		
		multipleURIValidator.setCurrentFileName(fileName3);
		multipleURIValidator.addUri(uri5);
		multipleURIValidator.addUri(uri6);
		
		//Validation ok and generated no validation errors
		TestHelper.assertValidationOkAndZeroValidationErrors(multipleURIValidator);
	}
	
	//Bad
	//Two non-unique URI within the same file
	@Test
	void testValidationGeneratesValidationError1() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri2); //DIfferent URI
	
		//Validation generated errors
		assertFalse(multipleURIValidator.validate());
		TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);	
	}	
	
	
	//Bad
	//Three non-unique URI within the same file
	@Test
	void testValidationGeneratesValidationError2() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri2); //DIfferent URI
		
		//Validation generated errors
		assertFalse(multipleURIValidator.validate());
		TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);
	}	
	
	// Bad
	// 2 x Two non-unique URI within the same file
	@Test
	void testValidationGeneratesValidationError3() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2); //Same URI
		multipleURIValidator.addUri(uri2);

		//Validation generated errors
		assertFalse(multipleURIValidator.validate());

		Map<String, List<ValidationError>> validationErrorsPerFileMap = validationErrorStorage.getValidationErrors();

		//Assert there are validationerrors for one file only
		Set<String> keySet = validationErrorsPerFileMap.keySet();
		assertEquals(1, keySet.size());

		//Get the list of validationerrors for that file
		List<ValidationError> validationErrors = validationErrorsPerFileMap.get(keySet.iterator().next());
		
		TestHelper.assertValidationError(validationErrors.get(0), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);
		TestHelper.assertValidationError(validationErrors.get(1), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri2, "URI: " + uri2 + " exist multiple times in file: " + fileName1);
	}
	
//	// Bad
//	// Two non-unique URI within two different files
	@Test
	void testValidationGeneratesValidationError4() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		
		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri1); //Same URI

		//Validation generated errors
		assertFalse(multipleURIValidator.validate());

		Map<String, List<ValidationError>> validationErrorsPerFileMap = validationErrorStorage.getValidationErrors();

		//Check that there are validationerrors for only one file
		Set<String> keySet = validationErrorsPerFileMap.keySet();
		assertEquals(1, keySet.size());

		//Check that there is only one validationerror for the file
		List<ValidationError> validationErrors = validationErrorsPerFileMap.get(keySet.iterator().next());
		assertEquals(1, validationErrors.size());
		assertTrue(keySet.contains(fileName1 + "," + fileName2));
		
		//Check that the values are correct
		TestHelper.assertValidationError(validationErrors.get(0), fileName1 + "," + fileName2, ErrorType.DUPLICATE_URI_BETWEEN_FILES, null, uri1, "URI: " + uri1 + " exist in the following files: " + fileName1 + "," + fileName2);
	}
	
	// Bad
	// Three non-unique URI within three different files + some other URI:s that are unique
	@Test
	void testValidationGeneratesValidationError5() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI

		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri2); //Unique URI
		multipleURIValidator.addUri(uri3); //Unique URI

		multipleURIValidator.setCurrentFileName(fileName3);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri4); //Unique URI
		
		multipleURIValidator.setCurrentFileName(fileName4);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri5); //Unique URI
		
		//Validation generated errors
		assertFalse(multipleURIValidator.validate());

		Map<String, List<ValidationError>> validationErrorsPerFileMap = validationErrorStorage.getValidationErrors();

		//Check that there are validationerrors for two files: fileName1 and fileName4
		Set<String> keySet = validationErrorsPerFileMap.keySet();
		assertEquals(1, keySet.size());
		
		/*
		 * Assert that the key contains all the filenames where the error has occurred
		 * We can however not know the order of the filenames in the string
		 */
		String multipleFilesKey = keySet.iterator().next();
		assertTrue(multipleFilesKey.contains(fileName1));
		assertTrue(multipleFilesKey.contains(fileName3));
		assertTrue(multipleFilesKey.contains(fileName4));

		//Check that there is only one validationerror with the name consisting of multiple files
		List<ValidationError> validationErrors = validationErrorsPerFileMap.get(multipleFilesKey);
		assertEquals(1, validationErrors.size());
		
		//Check that the values are correct
		ValidationError validationError = validationErrors.get(0);
		assertEquals(ErrorType.DUPLICATE_URI_BETWEEN_FILES, validationError.getErrorType());
		/*
		 * A validationerror that spans multiple files has all the filenames of the affected files concatenated
		 * We can never really know the order in which the filenames are added
		 */
		String concatenatedFileName = validationError.getFileName();
		assertTrue(concatenatedFileName.contains(fileName1));
		assertTrue(concatenatedFileName.contains(fileName3));
		assertTrue(concatenatedFileName.contains(fileName4));
		
		/*
		 * We can never really know the order in which the filenames are added
		 * in the errordescription
		 */
		String description = validationError.getDescription();
		assertTrue(description.contains(fileName1));
		assertTrue(description.contains(fileName3));
		assertTrue(description.contains(fileName4));
		assertTrue(description.contains("URI: " + uri1 + " exist in the following files: "));
		
		assertEquals(uri1, validationError.getValue());

	}

	
	// Bad!
	// The Final Exam Test
	// Three non-unique URI within three different files 
	// + non-unique within a file
	// + non-unique within and between files
	// + some other URI:s that are unique
	@Test
	void testValidationGeneratesValidationError6() throws Exception {
		
		//Add testdata
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Uri 1
		multipleURIValidator.addUri(uri6); //Uri 6
		multipleURIValidator.addUri(uri6); //Uri 6

		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri2); //Uri 2
		multipleURIValidator.addUri(uri2); //Uri 2
		multipleURIValidator.addUri(uri3); //Uri 3 (Unique)

		multipleURIValidator.setCurrentFileName(fileName3);
		multipleURIValidator.addUri(uri1); //Uri 1
		multipleURIValidator.addUri(uri4); //Uri 4 (Unique)

		multipleURIValidator.setCurrentFileName(fileName4);
		multipleURIValidator.addUri(uri1); //Uri 1
		multipleURIValidator.addUri(uri5); //Uri 5 (Unique)
		multipleURIValidator.addUri(uri2); //Uri 2

		// Validate testdata
		assertFalse(multipleURIValidator.validate());

		Map<String, List<ValidationError>> validationErrorsPerFileMap = validationErrorStorage.getValidationErrors();

		// Check that there are validationerrors for two files: fileName1 and fileName4
		Set<String> keySet = validationErrorsPerFileMap.keySet();
		assertEquals(4, keySet.size());

		/*
		 * Assert that the key contains all the filenames where the error has occurred
		 * We can however not know the order of the filenames in the string
		 */
		String[] fileNames = keySet.toArray(new String[0]);
		
		assertTrue(fileNames[0].contains(fileName1));
		assertTrue(fileNames[0].contains(fileName3));
		assertTrue(fileNames[0].contains(fileName4));
		assertTrue(fileNames[1].contains(fileName1));
		assertTrue(fileNames[2].contains(fileName2));
		assertTrue(fileNames[3].contains(fileName2));
		assertTrue(fileNames[3].contains(fileName4));

		// Check that there is only one validationerror with the name consisting of
		// multiple files
		List<ValidationError> validationErrorsFile1 = validationErrorsPerFileMap.get(fileNames[0]);
		
		assertEquals(1, validationErrorsFile1.size());
	
		// Check that the values are correct
		ValidationError validationError = validationErrorsFile1.get(0);
		assertEquals(ErrorType.DUPLICATE_URI_BETWEEN_FILES, validationError.getErrorType());

		/*
		 * A validationerror that spans multiple files has all the filenames of the
		 * affected files concatenated We can never really know the order in which the
		 * filenames are added
		 */
		String concatenatedFileName = validationError.getFileName();
		assertTrue(concatenatedFileName.contains(fileName1));
		assertTrue(concatenatedFileName.contains(fileName3));
		assertTrue(concatenatedFileName.contains(fileName4));

		/*
		 * We can never really know the order in which the filenames are added in the
		 * errordescription
		 */
		String description = validationError.getDescription();
		assertTrue(description.contains(fileName1));
		assertTrue(description.contains(fileName3));
		assertTrue(description.contains(fileName4));
		assertTrue(description.contains("URI: " + uri1 + " exist in the following files: "));
		assertEquals(uri1, validationError.getValue());

		List<ValidationError> validationErrorsFile2 = validationErrorsPerFileMap.get(fileNames[1]);
		assertEquals(1, validationErrorsFile2.size());
		
		//Check that the values are correct for ValidationError file 2 
		TestHelper.assertValidationError(validationErrorsFile2.get(0), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri6, "URI: " + uri6 + " exist multiple times in file: " + fileName1);

		List<ValidationError> validationErrorsFile3 = validationErrorsPerFileMap.get(fileNames[2]);
		assertEquals(1, validationErrorsFile3.size());
		
		// Check that the values are correct for ValidationError file 3
		TestHelper.assertValidationError(validationErrorsFile3.get(0), fileName2, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri2, "URI: " + uri2 + " exist multiple times in file: " + fileName2);
		
		List<ValidationError> validationErrorsFile4 = validationErrorsPerFileMap.get(fileNames[3]);
		assertEquals(1, validationErrorsFile4.size());

		// Check that the values are correct for ValidationError file 4
		validationError = validationErrorsFile4.get(0);
		assertEquals(ErrorType.DUPLICATE_URI_BETWEEN_FILES, validationError.getErrorType());
		
		/*
		 * A validationerror that spans multiple files has all the filenames of the
		 * affected files concatenated We can never really know the order in which the
		 * filenames are added
		 */
		concatenatedFileName = validationError.getFileName();
		assertTrue(concatenatedFileName.contains(fileName2));
		assertTrue(concatenatedFileName.contains(fileName4));
		
		/*
		 * We can never really know the order in which the filenames are added in the
		 * errordescription
		 */
		description = validationError.getDescription();
		assertTrue(description.contains(fileName2));
		assertTrue(description.contains(fileName4));
		assertTrue(description.contains("URI: " + uri2 + " exist in the following files: "));
		assertEquals(uri2, validationError.getValue());
		
	}

}
