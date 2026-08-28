// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

	@BeforeEach
	public void setup() throws Exception{
		TestHelper.resetSingeltons();
		ValidationErrorStorage.getInstance().resetErrors();  
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
			instance.validateData("dcterms:nonsense", "http://arbetsformedlingen.se");
			fail("Expected DCATException when key does not have a corresponding typedefinition");
		} catch (DcatException e) {
			assertEquals("Error validating type: Key dcterms:nonsense is not defined", e.getMessage());
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
		"dcterms:publisher,    				https://example.com/publisher1",	// link to an Agent, validated as a URI
		"dcat:temporalResolution,			P5Y2M10D",				// valid duration
		"dcat:spatialResolutionInMeters,	1.093",					// valid decimal
		"dcat:spatialResolutionInMeters,	50.0",					// valid decimal
		"dcat:byteSize,						1093",					// valid nonNegativeInteger
		"dcat:byteSize,						1",						// valid nonNegativeInteger
		"dcterms:issued,					2001-10-26",			// valid Date
		"dcterms:issued,					2002-05-30T09:30:10",	// valid Datetime
		"dcterms:issued,					1982",					// valid year
		"vcard:hasValue,					0771-717 717",			// valid phonenumber format
		"vcard:hasValue,					+46104794000",			// valid phonenumber format
		"vcard:hasValue,					tel:+46104794000",		// valid phonenumber format
		"locn:geometry,     				'LINESTRING(10.0 51.0, 11.0 52.0, 12.0 53.0)'",	// valid LINESTRING
		"dcat:bbox,         				'POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))'",         	// valid POLYGON
		"dcat:centroid,     				POINT(10.0 51.0)",                      		// valid POINT
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
		"foaf:homepage, 					://ams.se", 			// URI wrong format
		"dcat:temporalResolution, 			5Y2M10D", 				// Duration wrong format
		"dcat:spatialResolutionInMeters,	',01'",					// invalid decimal
		"dcat:byteSize,						12345Y",				// invalid nonNegativeInteger
		"dcat:byteSize,						-100",					// invalid nonNegativeInteger
		"dcat:byteSize,						64.5",					// invalid nonNegativeInteger
		"vcard:hasValue,			        0771-71u 7178",			// invalid phonenumber format
		"vcard:hasValue,			        ?46104794000",			// invalid phonenumber format
		"dcterms:issued,			        2001-0-26",				// invalid Date
		"dcterms:issued,			        2002-05-30T09:30",		// invalid Datetime
		"dcterms:issued,			        192",					// invalid Year
		"locn:geometry,     				LINESTRING 10.0 51.0",	// missing parentheses
		"dcat:bbox,         				Sverige",         		// invalid geometry
		"dcat:centroid,     				POINTX(10.0 51.0)",     // unknown geometry type
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

	@ParameterizedTest
	@CsvSource({
		"dcat:theme,           http://publications.europa.eu/resource/authority/data-theme/ENER",
		"adms:status,          http://publications.europa.eu/resource/authority/distribution-status/DEVELOP",
		"dcatap:hvdCategory,   http://data.europa.eu/bna/c_dd313021",
		"dcterms:accessRights, http://publications.europa.eu/resource/authority/access-right/PUBLIC",
		"rdf:type,             http://www.w3.org/2006/vcard/ns#Organization",
	})
	void testThatValuesInTheSpecificationListPassValidation(String key, String value) throws Exception {
		SingleInputValidator instance = SingleInputValidator.getInstance();
		instance.setCurrentFileName("fileName1.raml");
		TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, "fileName1.raml", key, value);
	}

	@ParameterizedTest
	@CsvSource({
		"dcat:theme,         http://publications.europa.eu/resource/authority/data-theme/NONSENSE",
		"adms:status,        http://purl.org/adms/status/UnderDevelopment",
		"dcatap:hvdCategory, http://data.europa.eu/bna/c_ffffffff",
	})
	void testThatValuesOutsideTheSpecificationListFailValidation(String key, String value) throws Exception {
		ValidationErrorStorage storage = ValidationErrorStorage.getInstance();
		SingleInputValidator instance = SingleInputValidator.getInstance();
		String fileName = "swagger445.json";
		instance.setCurrentFileName(fileName);
		String description = "The value " + value + " is not one of the values allowed for key " + key + ".";

		assertFalse(instance.validateData(key, value));
		TestHelper.assertOneValidationError(storage.getValidationErrors(), fileName, ErrorType.UNKNOWN_VALUE, key, value, description);
	}

	@ParameterizedTest
	@CsvSource({
		"dcat:accessURL,	https://example.se/data.csv",	// accessURL allows http and https
		"dcat:downloadURL,	ftp://example.se/data.csv",		// downloadURL also allows ftp and ftps
		"dcterms:subject,	https://www.dataportal.se/terminology/grunddata/person",
	})
	void testThatValuesMatchingTheSpecificationPatternPassValidation(String key, String value) throws Exception {
		SingleInputValidator instance = SingleInputValidator.getInstance();
		String fileName = "fileName1.raml";
		instance.setCurrentFileName(fileName);
		TestHelper.assertFileNameSetValidationOkAndZeroValidationErrors(instance, fileName, key, value);
	}

	@ParameterizedTest
	@CsvSource({
		"dcat:accessURL,	ftp://example.se/data.csv",			// accessURL allows only http and https
		"dcterms:subject,	http://eurovoc.europa.eu/100142",	// not the grunddata terminology
	})
	void testThatValuesBreakingTheSpecificationPatternFailValidation(String key, String value) throws Exception {
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		SingleInputValidator instance = SingleInputValidator.getInstance();
		String fileName = "swagger445.json";
		instance.setCurrentFileName(fileName);
		String description = "The value " + value + " has wrong format for key " + key + ".";

		assertFalse(instance.validateData(key, value));
		TestHelper.assertOneValidationError(validationErrorStorage.getValidationErrors(), fileName, ErrorType.ILLEGAL_FORMAT, key, value, description);
	}
}
