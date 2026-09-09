// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import se.ams.dcatprocessor.controller.PreprocessorController.ApiSource;
import se.ams.dcatprocessor.controller.PreprocessorController.SpecRequest;
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

	@TempDir
	Path tempDir;

	private String pathFiles;
	private String pathSpec;

	@BeforeEach
	void setup() throws Exception {
		TestHelper.resetSingeltons();
		pathFiles = "http://localhost:" + port + "/dcat-generation/files/";
		pathSpec = "http://localhost:" + port + "/dcat-generation/spec";
	}
	
	/**
	 * Basic testing of the REST-api
	 */
	//Existing folder location but no API-files
	
	@Test
	public void testThatFolderWithNoApiFilesReturnsNoFilesFound() throws Exception {
		String actual = this.restTemplate.getForObject(pathFiles + "?dir=" + tempDir.toString(),String.class);
		String expected = "Hittade inga filer";

		assertEquals(expected, actual);
	}

	//Nonexisting folder location
	@Test
	public void testThatNonExistingFolderReturnsNoFilesFound() throws Exception {
		String actual = this.restTemplate.getForObject(pathFiles + "?dir=" + tempDir.resolve("nonexistent").toString(),String.class);
		String expected = "Hittade inga filer";

		assertEquals(expected, actual);
	}

	//Correct folder location and existing API-file in folder
	@Test
	public void testThatValidApiFileReturnsNonEmptyResponse() throws Exception {
		TestHelper.copyFile(TestHelper.TEST_FILE_DIR + "apidef/raml_1/obl_rek_raml.raml", tempDir.resolve("obl_rek_raml_test.raml").toString());
		String actual = this.restTemplate.getForObject(pathFiles + "?dir=" + tempDir.toString(),String.class);

		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		assertTrue(actual.length() > 500); //We get a .rdf file back but we don't care what it contains
	}

	@Test
	public void testThatValidSpecRequestReturnsRDFResponse() throws IOException {
		String json = new ClassPathResource("apidef/json_oas/obl_rek_oas.json").getContentAsString(StandardCharsets.UTF_8);
		SpecRequest request = new SpecRequest(List.of(new ApiSource("obl_rek_oas.json", json)));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("<rdf:RDF"));
	}

	@Test
	public void testThatMissingFileNameOnSpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(List.of(new ApiSource("", "{}")));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("is missing a file name"));
	}

	@Test
	public void testThatNullContentOnSpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(List.of(new ApiSource("api_def.json", null)));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("The file 'api_def.json' is empty"));
	}
	
	@Test
	public void testThatEmptySpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(null);

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("No specification files supplied"));
	}

	@Test
	public void testThatNullAPiSourceSpecRequestReturnsError() throws IOException {
		List<ApiSource> list = new ArrayList<>();
		list.add(null);
		SpecRequest request = new SpecRequest(list);

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("File 1 is missing", response.getBody());
	}

	@Test
	public void testThatEmptyContentOnSpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(List.of(new ApiSource("api_def.json", "")));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("The file 'api_def.json' is empty", response.getBody());
	}

	@Test
	public void testThatInvalidContentOnSpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(List.of(new ApiSource("api_def.json", "{'invalid-dcat': 'value'}")));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(422, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("ERROR - Failed to process API specification"));
	}

	@Test
	public void testThatInvalidExtensionOnSpecRequestReturnsError() throws IOException {
		SpecRequest request = new SpecRequest(List.of(new ApiSource("api_def.pdf", "101001")));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("The file 'api_def.pdf' has an invalid file extension"));
	}

	@Test
	public void testThatMultipleErrorsOnSpecRequestReturnsAllError() throws IOException {
		ApiSource apiSource1 = new ApiSource("api_def1.json", "");
		ApiSource apiSource2 = new ApiSource("", "{}");

		SpecRequest request = new SpecRequest(List.of(apiSource1, apiSource2));

		ResponseEntity<String> response = this.restTemplate.postForEntity(pathSpec, request, String.class);

		assertEquals(400, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().contains("The file 'api_def1.json' is empty"));
		assertTrue(response.getBody().contains("is missing a file name"));
	}
}
