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

package se.ams.dcatprocessor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import se.ams.dcatprocessor.testutil.TestHelper;

/**
 * Tests the REST-API of the PreprocessorController
 * @author nacbr
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PreprocessorControllerRestTest {
	
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static String localHost = "http://localhost:";
	private static String pathFile = "/dcat-generation/files/";
	private static String userDir = System.getProperty("user.dir");
	
	private static String testFilePath = TestHelper.USER_DIR + "/src/test/java/se/ams/dcatprocessor/rest/apidef/obl_rek_raml_test.raml";

	/**
	 * Delete testfile between tests
	 * @throws Exception
	 */
	@BeforeEach
	void setup() throws Exception {
		Files.deleteIfExists(Paths.get(testFilePath));
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
	public void testInvalidAPIFile() throws Exception {
		TestHelper.copyFile(TestHelper.TEST_FILE_DIR + "apidef/raml_1/obl_rek_raml.raml", testFilePath);
		String actual = this.restTemplate.getForObject(localHost + port + pathFile + "?dir=" + userDir + "/src/test/java/se/ams/dcatprocessor/rest/apidef/", String.class);

		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		assertTrue(actual.length() > 500); //We get a .rdf file back but we don't care what it contains
	}


}

