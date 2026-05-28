// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	//Fetch the typevalue from catalog.dcterms\:publisher=1|Class
	@Test
	void testThatPropertyValueTypeCanBeFetchedCorrectly1() throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler instance = DcatPropertyHandler.getInstance();
			String[] types = instance.getPropertyValueTypes("catalog.dcterms:publisher");
			assertNotNull(types);
			assertEquals(1, types.length);
			assertEquals("class", types[0]);
			
		} catch (IllegalArgumentException e) {
			fail("Unexpected IllegalArgumentException when loading a correct propertyfile");
		}
		
	}
	
	//Fetch the typevalue from catalog.dcterms\:description=1..n|xsd\:String
	@Test
	void testThatPropertyValueTypeCanBeFetchedCorrectly2() throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler instance = DcatPropertyHandler.getInstance();
			String[] types = instance.getPropertyValueTypes("catalog.dcterms:description");
			assertNotNull(types);
			assertEquals(1, types.length);
			assertEquals("xsd:string", types[0]);
			
		} catch (IllegalArgumentException e) {
			fail("Unexpected IllegalArgumentException when loading a correct propertyfile");
		}
		
	}

	//Fetch the typevalue from catalog.dcterms\:modified=0..1|xsd\:date,xsd\:dateTime,xsd\:gYear
	@Test
	void testThatPropertyValueTypeCanBeFetchedCorrectly3() throws Exception {
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
	
	//Illegal cardinality 0..2 i propertyfile
	@Test
	void testThatIllegalPropertyvaluesAndStringsAreHandledCorrectly2() throws Exception{
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_2.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		try {
			DcatPropertyHandler.getInstance();
			fail("Expected IllegalArgumentException due to incorrect values");
		} catch (IllegalArgumentException e) {
			assertEquals("Propertyfile: Illegal value for cardinality found 2..n|class Allowed values are 1, 1..n, 0..1 or 0..n followed by | and then a string", e.getMessage());
		}
		
	}
	
	// Illegal cardinality 3 i propertyfile
	@Test
	void testThatIllegalPropertyvaluesAndStringsAreHandledCorrectly3() throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_3.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);

		try {
			DcatPropertyHandler.getInstance();
			fail("Expected IllegalArgumentException due to incorrect values");
		} catch (IllegalArgumentException e) {
			assertEquals("Propertyfile: Illegal value for cardinality found 3|class Allowed values are 1, 1..n, 0..1 or 0..n followed by | and then a string", e.getMessage());
		}

	}
	
	// Illegal property key format in propertyfile
	@Test
	void testThatIllegalPropertyvaluesAndStringsAreHandledCorrectly4() throws Exception {
		/**
		 * Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		 */
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_4.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);

		try {
			DcatPropertyHandler.getInstance();
			fail("Expected IllegalArgumentException due to incorrect values");
		} catch (IllegalArgumentException e) {
			assertEquals("Propertykey catalog_dcterms:isPartOf has invalid format. Permitted format is xx.xx:xx", e.getMessage());
		}

	}
	
}
