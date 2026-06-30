// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.ams.dcatprocessor.rdf.validate.SingleInputValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;
import se.ams.dcatprocessor.testutil.TestHelper;

class SingleInputValidatorTest {

	/**
	 * Save the original dcat_specification.properties file before changing it
	 */
	@BeforeAll
	public static void setUp() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED);
	}
	
	@BeforeEach
	public void setup() throws Exception{
		TestHelper.resetSingeltons();
		ValidationErrorStorage.getInstance().resetErrors();  
        /**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
    	String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
	}
	
	/**
	 * Restore the original dcat_specification.properties file after all tests 
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE);
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = "IrrelevantValueToTriggerNextError")
	void testThatValidationFailsWhenInputKeyIsNull(String value) throws Exception {

		try {
			SingleInputValidator instance = SingleInputValidator.getInstance();
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData(null, value);
			fail("Expected DCATException when all inputparameters are null");
		} catch (DcatException e) {
			assertEquals("Error validating type: Input key is null", e.getMessage());
		}
	}

	@Test
	void testThatValidationFailsWhenInputValueIsNull() throws Exception {
		
		try {
			SingleInputValidator instance = SingleInputValidator.getInstance();
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData("IrrelevantKeyToTriggerNextError", null);
			fail("Expected DCATException when all inputparameters are null");
		} catch (DcatException e) {
			assertEquals("Error validating type: Input value is null", e.getMessage());
		}
	}
	
	@Test
	void testThatValidationFailsWhenPropertyKeyDoesNotHaveADefinedType() throws Exception {

		try {
			SingleInputValidator instance = SingleInputValidator.getInstance();
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData("dcterms:accessRights", "http://arbetsformedlingen.se");
			fail("Expected DCATException when key does not have a corresponding typedefinition");
		} catch (DcatException e) {
			assertEquals("Error validating type: Key dcterms:accessRights is not defined", e.getMessage());
		}
	}
	
	@Test
	void testThatLoadingOfPropertiesFailsWhenValueTypeIsNotDefined() throws Exception {

		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1_undefined_type.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);

		try {
			SingleInputValidator.getInstance();	//Trigger reloading of properties
			fail("Expected DCATException when key does not have a defined value type");
		} catch (DcatException e) {
			assertEquals("Error loading properties: No corresponding definition found for type xsd:boolean", e.getMessage());
		}
	}
	
	@Test
	void testThatValidationFailsWhenCurrentFileIsNotSet() throws Exception {

		try {
			SingleInputValidator instance = SingleInputValidator.getInstance();
			//Provoke an error to discover that the filename was not set when saving ValidationError
			instance.validateData("dcterms:issued", "2001-26");
			fail("Expected DCATException when currentFile is not set");
		} catch (DcatException e) {
			assertEquals("class se.ams.dcatprocessor.rdf.validate.SingleInputValidator Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}
	
	}

	@ParameterizedTest
	@CsvSource({
	    "dcterms:description, 				any text", 				// valid text value
		"dcterms:description, 				dcterms:description", 	// valid text value
	    "dcterms:description,				aze:azərbaycan dili", 	// valid text with language
		"dcterms:publisher,    				any text",				// valid dcterms:publisher passes through validation since its a separate class
		"dcat:temporalResolution,			P5Y2M10D",				// valid duration
		"dcat:spatialResolutionInMeters,	1.093",					// valid decimal
		"dcat:spatialResolutionInMeters,	50.0",					// valid decimal
		"dcat:byteSize,						1093",					// valid integer
		"dcat:byteSize,						1",						// valid integer
		"dcterms:issued,					2001-10-26",			// valid Date
		"dcterms:issued,					2002-05-30T09:30:10",	// valid Datetime
		"dcterms:issued,					1982",					// valid year
		"vcard:hasValue,					0771-717 717",			// valid phonenumber format
		"vcard:hasValue,					+46104794000",			// valid phonenumber format
		"vcard:hasValue,					tel:+46104794000",		// valid phonenumber format

	})
	void testThatValidValuesPassValidation(String key, String value) throws Exception {	
		SingleInputValidator instance = SingleInputValidator.getInstance();
		String filename1 = "fileName1.raml";
		instance.setCurrentFileName(filename1);
		TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, key, value);
	}

	@ParameterizedTest
	@CsvSource({
	    "foaf:homepage, 					arbetsformedlingen", 	// URI wrong format
		"foaf:homepage, 					www.ams", 				// URI wrong format
		"foaf:homepage, 					ww.ams.se", 			// URI wrong format
		"foaf:homepage, 					htp://ams.se", 			// URI wrong format
		"foaf:homepage, 					://ams.se", 			// URI wrong format
		"dcat:temporalResolution, 			5Y2M10D", 				// Duration wrong format
		"dcat:spatialResolutionInMeters,	',01'",					// invalid decimal
		"dcat:byteSize,						12345Y",				// invalid integer
		"vcard:hasValue,			        0771-71u 7178",			// invalid phonenumber format
		"vcard:hasValue,			        -0711 7178",			// invalid phonenumber format
		"vcard:hasValue,			        ?46104794000",			// invalid phonenumber format
		"dcterms:issued,			        2001-0-26",				// invalid Date
		"dcterms:issued,			        2002-05-30T09:30",		// invalid Datetime
		"dcterms:issued,			        192",					// invalid Year
	})
	void testThatInvalidValuesFailValidation(String key, String value) throws Exception {
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();	
		SingleInputValidator instance = SingleInputValidator.getInstance();
		String fileName = "swagger445.json";
		instance.setCurrentFileName(fileName);
		String description = "The value " + value + " has wrong format for key " + key + ".";

		assertFalse(instance.validateData(key, value));
		TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
	}
}
