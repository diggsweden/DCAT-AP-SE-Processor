// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import se.ams.dcatprocessor.testutil.TestHelper;

/**
 * Tests the REST-API of the PreprocessorController
 * @author nacbr
 *
 */
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PreprocessorControllerRestTest {
	
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static String localHost = "http://localhost:";
	private static String pathFile = "/dcat-generation/files/";
	
	private static String testFilePath = TestHelper.USER_DIR + "/src/test/java/se/ams/dcatprocessor/rest/apidef/obl_rek_raml_test.raml";

	/**
	 * Delete testfile between tests
	 * @throws Exception
	 */
	@BeforeEach
	void setup() throws Exception {
		Files.deleteIfExists(Paths.get(testFilePath));
		TestHelper.resetSingeltons();
	}
	
	/**
	 * Basic testing of the REST-api
	 */
	//Existing folder location but no API-files
	@Test
	public void testEmptyAPIFilesFolder() throws Exception {
		String actual = this.restTemplate.getForObject(localHost
				+ port + pathFile + "?dir=" + TestHelper.USER_DIR + "/src/test/java/se/ams/dcatprocessor/rest/apidef/", String.class);
		String expected = "Hittade inga filer";

		assertEquals(expected, actual);
	}

	//Nonexisting folder location
	@Test
	public void testNonExistingAPIFilesFolder() throws Exception {
		String actual = this.restTemplate.getForObject(localHost
				+ port + pathFile + "?dir=" + TestHelper.USER_DIR + "/src/test/java/se/ams/dcatprocessor/rest/blah/", String.class);
		String expected = "Hittade inga filer";

		assertEquals(expected, actual);
	}

	//Correct folder location and existing API-file in folder
	@Test
	public void testValidAPIFile() throws Exception {
		TestHelper.copyFile(TestHelper.TEST_FILE_DIR + "apidef/raml_1/obl_rek_raml.raml", testFilePath);
		String actual = this.restTemplate.getForObject(localHost + port + pathFile + "?dir=" + TestHelper.USER_DIR + "/src/test/java/se/ams/dcatprocessor/rest/apidef/", String.class);

		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		assertTrue(actual.length() > 500); //We get a .rdf file back but we don't care what it contains
	}
}
