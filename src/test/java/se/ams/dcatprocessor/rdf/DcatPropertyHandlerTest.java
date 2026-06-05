// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import se.ams.dcatprocessor.testutil.TestHelper;
import se.ams.dcatprocessor.util.DcatPropertyHandler;

class DcatPropertyHandlerTest {

	/**
	 * Save the original dcat_specification.properties file before changing it
	 */
	@BeforeAll
	public static void setUp() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED);
	}
	
	@BeforeEach
	public void setup() throws Exception {
		TestHelper.resetSingeltons();
	}
	
	/**
	 * Restore the original dcat_specification.properties file after all tests 
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE);
	}

	@ParameterizedTest
    @CsvSource({
		"catalog.dcterms:publisher, 	class", 		//Fetch the typevalue from catalog.dcterms\:publisher=1|Class
		"catalog.dcterms:description,	xsd:string", 	//Fetch the typevalue from catalog.dcterms\:description=1..n|xsd\:String
		"voice.vcard:hasValue,			phoneNumber", 	//Fetch the typevalue from voice.vcard\:hasValue=1|phoneNumber
		"distribution.dcat:byteSize,	xsd:integer", 	//Fetch the typevalue from distribution.dcat\:byteSize=0..1|xsd:integer
	})
	void testThatPropertyValueTypeCanBeFetchedCorrectly(String key, String type) throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler instance = DcatPropertyHandler.getInstance();
			String[] types = instance.getPropertyValueTypes(key);
			assertNotNull(types);
			assertEquals(1, types.length);
			assertEquals(type, types[0]);
			
		} catch (IllegalArgumentException e) {
			fail("Unexpected IllegalArgumentException when loading a correct propertyfile");
		}
		
	}

	//Fetch the typevalue from catalog.dcterms\:modified=0..1|xsd\:date,xsd\:dateTime,xsd\:gYear
	@Test
	void testThatPropertyValueTypeCanBeFetchedCorrectly() throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler instance = DcatPropertyHandler.getInstance();
			String[] types = instance.getPropertyValueTypes("catalog.dcterms:modified");
			assertNotNull(types);
			assertEquals(3, types.length);
			assertEquals("xsd:date", types[0]);
			assertEquals("xsd:dateTime", types[1]);
			assertEquals("xsd:gYear", types[2]);
			
		} catch (IllegalArgumentException e) {
			fail("Unexpected IllegalArgumentException when loading a correct propertyfile");
		}
		
	}

	@ParameterizedTest
    @CsvSource({
		"dcat_specification_test_2.properties,	'Propertyfile: Illegal value for cardinality found 2..n|class Allowed values are 1, 1..n, 0..1 or 0..n followed by | and then a string'",	// Illegal cardinality 0..2 i propertyfile
		"dcat_specification_test_3.properties,	'Propertyfile: Illegal value for cardinality found 3|class Allowed values are 1, 1..n, 0..1 or 0..n followed by | and then a string'", 		// Illegal cardinality 3 i propertyfile
		"dcat_specification_test_4.properties, 	'Propertykey catalog_dcterms:isPartOf has invalid format. Permitted format is xx.xx:xx'"													// Illegal property key format in propertyfile
	})
	void testThatIllegalPropertyvaluesAndStringsAreHandledCorrectly(String filename, String errorMessage) throws Exception{
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + filename);
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler.getInstance();
			fail("Expected IllegalArgumentException due to incorrect values");
		} catch (IllegalArgumentException e) {
			assertEquals(errorMessage, e.getMessage());
		}	
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"invalid-key", 
	})
	void testThatGetPropertyValueTypesReturnsNullWhenNoMatch(String key) throws Exception{
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		DcatPropertyHandler handler = DcatPropertyHandler.getInstance();

		String[]result = handler.getPropertyValueTypes(key);
		assertNull(result);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"invalid-key", 
	})
	void testThatGetPropertyValueCardinalityReturnsNullWhenNoMatch(String key) throws Exception{
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);


		String result = DcatPropertyHandler.getInstance().getPropertyValueCardinality(key);
		assertNull(result);
	}
}
