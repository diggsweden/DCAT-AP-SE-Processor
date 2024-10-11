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

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.testutil.TestHelper;
import se.ams.dcatprocessor.util.DcatPropertyHandler;

@QuarkusTest
class SingleInputValidatorTest {

	@Inject
	SingleInputValidator singleInputValidator;

	@Inject
	ValidationErrorStorage validationErrorStorage;

	@Inject
	DcatPropertyHandler dcatPropertyHandler;


	/**
	 * Save the original dcat_specification.properties file before changing it
	 */
	@BeforeAll
	public static void setUp() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED);
	}

	@BeforeEach
	public void setup() throws Exception{
		validationErrorStorage.resetErrors();
        /**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
    	String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		dcatPropertyHandler.init();
	}
	
	/**
	 * Restore the original dcat_specification.properties file after all tests 
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE);
	}
	
	@Test
	void testThatValidationFailsWhenInputParametersAreNull1() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData(null, null);
			fail("Expected DCATException when all inputparameters are null");
		} catch (DcatException e) {
			assertEquals("Error validating type: Input key is null", e.getMessage());
		}
	}

	@Test
	void testThatValidationFailsWhenInputParametersAreNull2() throws Exception {
		
		try {
			SingleInputValidator instance = singleInputValidator;
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData("IrrelevantKeyToTriggerNextError", null);
			fail("Expected DCATException when all inputparameters are null");
		} catch (DcatException e) {
			assertEquals("Error validating type: Input value is null", e.getMessage());
		}
	}
	
	@Test
	void testThatValidationFailsWhenInputParametersAreNull3() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			instance.setCurrentFileName("irrelevantfilename.raml");
			instance.validateData(null, "IrrelevantValueToTriggerNextError");
			fail("Expected DCATException when all inputparameters are null");
		} catch (DcatException e) {
			assertEquals("Error validating type: Input key is null", e.getMessage());
		}
	}
	
	
	@Test
	void testThatValidationFailsWhenPropertyKeyDoesNotHaveADefinedType() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
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
		dcatPropertyHandler.init();

		try {
			singleInputValidator.loadInputTypeDefinitions();	//Trigger reloading of properties
			fail("Expected DCATException when key does not have a defined value type");
		} catch (DcatException e) {
			assertEquals("Error loading properties: No corresponding definition found for type xsd:boolean", e.getMessage());
		}
	}
	
	@Test
	void testThatValidationFailsWhenCurrentFileIsNotSet() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			//Provoke an error to discover that the filename was not set when saving ValidationError
			instance.validateData("dcterms:issued", "2001-26");
			fail("Expected DCATException when currentFile is not set");
		} catch (DcatException e) {
			assertEquals("class se.ams.dcatprocessor.rdf.validate.SingleInputValidator Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}
		
	}

	/**
	 * Test that validation works without errors. Correct and incorrect format
	 */
	//Text
	@Test
	void testThatInputValuesCanBeValidatedCorrectly1() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:description", "dcterms:description", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}

	//Text with language
	@Test
	void testThatInputValuesCanBeValidatedCorrectly1_1() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:description", "aze:azərbaycan dili", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}
	
	//dcterms:publisher passes through validation since its a separate class
	@Test
	void testThatInputValuesCanBeValidatedCorrectly2() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:publisher", "Any text", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}
	
	//Date
	@Test
	void testThatInputValuesCanBeValidatedCorrectly3() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:issued", "2001-10-26", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}

	//Datetime
	@Test
	void testThatInputValuesCanBeValidatedCorrectly4() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:issued", "2002-05-30T09:30:10", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}
	
	//Year
	@Test
	void testThatInputValuesCanBeValidatedCorrectly5() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String filename1 = "fileName1.raml";
			instance.setCurrentFileName(filename1);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, filename1, "dcterms:issued", "1982", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}
	
	//Date wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly6() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "arbetsformedlingen_apispec1.raml";
			instance.setCurrentFileName(fileName);
			String key = "dcterms:issued";
			String value = "2001-0-26";
			assertFalse(instance.validateData(key, value));
			String description = "The value " + value + " has wrong format for key " + key + ".";
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}

	//Datetime wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly7() throws Exception {

			
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "arbetsformedlingen_apispec1.raml";
			instance.setCurrentFileName(fileName);
			String key = "dcterms:issued";
			String value = "2002-05-30T09:30";
			assertFalse(instance.validateData(key, value));
			String description = "The value " + value + " has wrong format for key " + key + ".";
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
		
	}
	
	//Year wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly8() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "arbetsformedlingen_apispec1.raml";
			instance.setCurrentFileName(fileName);
			String key = "dcterms:issued";
			String value = "192";
			assertFalse(instance.validateData(key, value));
			String description = "The value " + value + " has wrong format for key " + key + ".";
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
			
		}
		
	}
	
	//URI
	@Test
	void testThatInputValuesCanBeValidatedCorrectly9() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "foaf:homepage", "http://arbetsformedlingen.se", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}
	
	//URI wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly10() throws Exception {

			
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger445.json";
			instance.setCurrentFileName(fileName);
			String key = "foaf:homepage";
			String value = "arbetsformedlingen";
			String description = "The value " + value + " has wrong format for key " + key + ".";
			assertFalse(instance.validateData(key, value));
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}
	
	//Duration
	@Test
	void testThatInputValuesCanBeValidatedCorrectly11() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger12.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "dcat:temporalResolution", "P5Y2M10D", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}

	//Duration wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly12() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "filename1.raml";
			instance.setCurrentFileName(fileName); //Needs to be set to store ValidationErrors correctly
			String key = "dcat:temporalResolution";
			String value = "5Y2M10D";
			String description = "The value " + value + " has wrong format for key " + key + ".";
			assertFalse(instance.validateData(key, value));
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}
	
	//Decimal
	@Test
	void testThatInputValuesCanBeValidatedCorrectly13() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "dcat:spatialResolutionInMeters", "1.093", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}
	
	//Decimal wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly14() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "filename2.raml";
			instance.setCurrentFileName(fileName); //Needs to be set to store ValidationErrors correctly
			String key = "dcat:spatialResolutionInMeters";
			String value = ",01";
			String description = "The value " + value + " has wrong format for key " + key + ".";
			assertFalse(instance.validateData(key, value));
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);	
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}
	
	//Integer
	@Test
	void testThatInputValuesCanBeValidatedCorrectly15() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "dcat:byteSize", "1093", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}

	//Integer wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly16() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "filename43.raml";
			instance.setCurrentFileName(fileName); //Needs to be set to store ValidationErrors correctly
			String key = "dcat:byteSize";
			String value = "12345Y";
			String description = "The value " + value + " has wrong format for key " + key + ".";
			assertFalse(instance.validateData(key, value));
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}
	
	//Phonenumber 1
	@Test
	void testThatInputValuesCanBeValidatedCorrectly17() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "vcard:hasValue", "0771-717 717", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}
	
	//Phonenumber 2
	@Test
	void testThatInputValuesCanBeValidatedCorrectly18() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "vcard:hasValue", "+46104794000", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}
	
	// Phonenumber 2 Should also be able to handle the format tel:+46104794000
	@Test
	void testThatInputValuesCanBeValidatedCorrectly19() throws Exception {

		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "swagger1.json";
			instance.setCurrentFileName(fileName);
			TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, "vcard:hasValue", "tel:+46104794000", validationErrorStorage);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	}
	
	//Phonenumber wrong format
	@Test
	void testThatInputValuesCanBeValidatedCorrectly20() throws Exception {

		
		try {
			SingleInputValidator instance = singleInputValidator;
			String fileName = "filename88.raml";
			instance.setCurrentFileName(fileName); //Needs to be set to store ValidationErrors correctly
			String key = "vcard:hasValue";
			String value = "0771-71u 717";
			String description = "The value " + value + " has wrong format for key " + key + ".";
			assertFalse(instance.validateData(key, value));
			TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
		} catch (DcatException e) {
			fail("Unexpected DCATException when testing normal validation");
		}
	
	}


}
