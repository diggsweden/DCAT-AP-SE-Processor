// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.testutil.TestHelper;

class CardinalityHandlerTest {
	
	/**
	 * Save the original dcat_specification.properties file before changing it
	 */
	@BeforeAll
	public static void setUp() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED);
	}

	@BeforeEach
	public void beforeEach() throws Exception {
		TestHelper.resetSingeltons();
	}
	
	/**
	 * Restore the original dcat_specification.properties file after all tests 
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		TestHelper.copyFile(TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE_SAVED, TestHelper.DECAT_SPECIFICATION_PROPERTIES_FILE);
	}
	
	@Test
	void testThatCardinalitiesAreLoadedCorrectly() throws Exception{
		
		//Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_1.properties");
		
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		
		Map<String, Cardinality> cardinalities = CardinalityHandler.getInstance().getCardinalities(DcatClass.CATALOG);
		
		//Expected Cardinality from CATALOG and dcterms:title is 1..n
		Cardinality actual = cardinalities.get("dcterms:title");
		assertNotNull(actual);
		assertEquals(1, actual.getMin());
		assertEquals(65535, actual.getMax());
		assertTrue(actual.isOneOrMore());
		
		//Expected Cardinality from CATALOG and dcterms:publisher is 1
		actual = cardinalities.get("dcterms:publisher");
		assertNotNull(actual);
		assertEquals(1, actual.getMin());
		assertEquals(1, actual.getMax());
		assertTrue(actual.isOne());

		//Expected Cardinality from CATALOG and dcterms:license is 1
		actual = cardinalities.get("dcterms:license");
		assertNotNull(actual);
		assertEquals(1, actual.getMin());
		assertEquals(1, actual.getMax());
		assertTrue(actual.isOne());
		
		//Expected Cardinality from CATALOG and dcterms:issued is 0..1
		actual = cardinalities.get("dcterms:issued");
		assertNotNull(actual);
		assertEquals(0, actual.getMin());
		assertEquals(1, actual.getMax());
		assertTrue(actual.isZeroOrOne());		
				
		//Expected Cardinality from CATALOG and dcat:service is 0..n
		actual = cardinalities.get("dcat:service");
		assertNotNull(actual);
		assertEquals(0, actual.getMin());
		assertEquals(65535, actual.getMax());
		assertTrue(actual.isZeroOrMore());		
				
	}
	
	// Invalid property key name i propertyfile
	@Test
	void testThatIllegalPropertyNameIsHandledCorrectly() throws Exception {
		
		//Copy the propertiesfile we want to use in the test directly to the target files dirctory to have it in the claspath
		
		String testFile = TestHelper.doubleSeparator(TestHelper.TEST_FILE_DIR + "dcat_specification_test_5.properties");
		TestHelper.copyFile(testFile, TestHelper.TEST_DECAT_SPECIFICATION_PROPERTIES_FILE);
		try {
			CardinalityHandler.getInstance();
			fail("Expected IllegalArgumentException due to incorrect values");
		} catch (IllegalArgumentException e) {
			assertEquals("Property: catalogue is not a DCAT-vocabulary", e.getMessage());
		}

	}
	

}
