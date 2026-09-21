// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.testutil.TestHelper;

class MultipleURIValidatorTest {

	MultipleURIValidator multipleURIValidator;

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
		multipleURIValidator = new MultipleURIValidator();
	}
	
	//Bad
	//No current file set...expect Exception in return
	@Test
	void testThatFileNotSetThrowsException() throws Exception {
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
	void testThatNullUriThrowsException() throws Exception {
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
	void testThatUniqueUrisWithinSameFilePassValidation() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2);
		
		//Validation ok and generated no validation errors
		TestHelper.assertValidationOkAndZeroValidationErrors(multipleURIValidator);
	}
	
	//Good
	// Three unique URI within the same file
	@Test
	void testThatMultipleUniqueUrisWithinSameFilePassValidation() throws Exception {
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
	void testThatUniqueUrisAcrossFilesPassValidation() throws Exception {
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
	void testThatUniqueUrisAcrossMultipleFilesPassValidation() throws Exception {
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
	void testThatDuplicateUriWithinFileReturnsValidationError() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri2); //DIfferent URI
	
		//Validation generated errors
		assertFalse(multipleURIValidator.validate());
		TestHelper.assertOneValidationError(multipleURIValidator.getValidationErrors() , fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);	
	}	
	
	
	//Bad
	//Three non-unique URI within the same file
	@Test
	void testThatTripleDuplicateUriWithinFileReturnsSingleValidationError() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri2); //DIfferent URI
		
		//Validation generated errors
		assertFalse(multipleURIValidator.validate());
		TestHelper.assertOneValidationError(multipleURIValidator.getValidationErrors(), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);
	}	
	
	// Bad
	// 2 x Two non-unique URI within the same file
	@Test
	void testThatTwoDuplicateUrisWithinFileReturnTwoValidationErrors() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		multipleURIValidator.addUri(uri1);
		multipleURIValidator.addUri(uri2); //Same URI
		multipleURIValidator.addUri(uri2);

		//Validation generated errors
		assertFalse(multipleURIValidator.validate());

		List<ValidationError> validationErrors = multipleURIValidator.getValidationErrors();
		assertEquals(2, validationErrors.size());
		TestHelper.assertValidationError(validationErrors.get(0), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri1, "URI: " + uri1 + " exist multiple times in file: " + fileName1);
		TestHelper.assertValidationError(validationErrors.get(1), fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri2, "URI: " + uri2 + " exist multiple times in file: " + fileName1);
	}
	
	// Bad
	// Two non-unique URI within two different files
	@Test
	void testThatDuplicateUriBetweenFilesReturnsValidationError() throws Exception {
		multipleURIValidator.setCurrentFileName(fileName1);
		multipleURIValidator.addUri(uri1); //Same URI
		
		multipleURIValidator.setCurrentFileName(fileName2);
		multipleURIValidator.addUri(uri1); //Same URI

		//Validation generated errors
		assertFalse(multipleURIValidator.validate());

		List<ValidationError> validationErrors = multipleURIValidator.getValidationErrors();
		assertEquals(1, validationErrors.size());

		//Check that the values are correct
		TestHelper.assertValidationError(validationErrors.get(0), fileName1 + "," + fileName2, ErrorType.DUPLICATE_URI_BETWEEN_FILES, null, uri1, "URI: " + uri1 + " exist in the following files: " + fileName1 + "," + fileName2);
	}
	
	// Bad
	// Three non-unique URI within three different files + some other URI:s that are unique
	@Test
	void testThatDuplicateUriAcrossMultipleFilesReturnsValidationError() throws Exception {
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

		List<ValidationError> validationErrors = multipleURIValidator.getValidationErrors();
		assertEquals(1, validationErrors.size());

		ValidationError validationError = TestHelper.findValidationError(validationErrors, ErrorType.DUPLICATE_URI_BETWEEN_FILES, uri1);
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
	void testThatAllDuplicateUriTypesAreDetected() throws Exception {
		
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

		List<ValidationError> validationErrors = multipleURIValidator.getValidationErrors();

		// Four distinct validationerrors expected
		assertEquals(4, validationErrors.size());

		// uri1 is duplicated across fileName1, fileName3 and fileName4
		ValidationError uri1BetweenFilesError = TestHelper.findValidationError(validationErrors, ErrorType.DUPLICATE_URI_BETWEEN_FILES, uri1);

		/*
		 * A validationerror that spans multiple files has all the filenames of the
		 * affected files concatenated We can never really know the order in which the
		 * filenames are added
		 */
		String concatenatedFileName = uri1BetweenFilesError.getFileName();
		assertTrue(concatenatedFileName.contains(fileName1));
		assertTrue(concatenatedFileName.contains(fileName3));
		assertTrue(concatenatedFileName.contains(fileName4));

		/*
		 * We can never really know the order in which the filenames are added in the
		 * errordescription
		 */
		String description = uri1BetweenFilesError.getDescription();
		assertTrue(description.contains(fileName1));
		assertTrue(description.contains(fileName3));
		assertTrue(description.contains(fileName4));
		assertTrue(description.contains("URI: " + uri1 + " exist in the following files: "));
		assertEquals(uri1, uri1BetweenFilesError.getValue());

		// uri6 is duplicated within fileName1
		ValidationError uri6WithinFileError = TestHelper.findValidationError(validationErrors, ErrorType.DUPLICATE_URI_WITHIN_FILE, uri6);
		TestHelper.assertValidationError(uri6WithinFileError, fileName1, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri6, "URI: " + uri6 + " exist multiple times in file: " + fileName1);

		// uri2 is duplicated within fileName2
		ValidationError uri2WithinFileError = TestHelper.findValidationError(validationErrors, ErrorType.DUPLICATE_URI_WITHIN_FILE, uri2);
		TestHelper.assertValidationError(uri2WithinFileError, fileName2, ErrorType.DUPLICATE_URI_WITHIN_FILE, null, uri2, "URI: " + uri2 + " exist multiple times in file: " + fileName2);

		// uri2 is also duplicated across fileName2 and fileName4
		ValidationError uri2BetweenFilesError = TestHelper.findValidationError(validationErrors, ErrorType.DUPLICATE_URI_BETWEEN_FILES, uri2);

		/*
		 * A validationerror that spans multiple files has all the filenames of the
		 * affected files concatenated We can never really know the order in which the
		 * filenames are added
		 */
		String concatenatedFileName2 = uri2BetweenFilesError.getFileName();
		assertTrue(concatenatedFileName2.contains(fileName2));
		assertTrue(concatenatedFileName2.contains(fileName4));
		
		/*
		 * We can never really know the order in which the filenames are added in the
		 * errordescription
		 */
		String description2 = uri2BetweenFilesError.getDescription();
		assertTrue(description2.contains(fileName2));
		assertTrue(description2.contains(fileName4));
		assertTrue(description2.contains("URI: " + uri2 + " exist in the following files: "));
		assertEquals(uri2, uri2BetweenFilesError.getValue());
		
	}

}
