// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.ams.dcatprocessor.models.ApiSource;
import se.ams.dcatprocessor.processor.DcatResult;
import se.ams.dcatprocessor.processor.Manager;
import se.ams.dcatprocessor.util.Util;


@RequestMapping("/dcat-generation")
@RestController
class PreprocessorController {

	private final ObjectProvider<Manager> managerProvider;
	private static final Logger logger = LoggerFactory.getLogger(PreprocessorController.class);
	private final static String UNEXPECTED_ERROR = "Error generating RDF. An unexpected error occurred.";

	public record SpecRequest(List<ApiSource> sources) { }

	public PreprocessorController(ObjectProvider<Manager> managerProvider) {
		this.managerProvider = managerProvider;
	}

	/**
	 * REST API Endpoint for creating DCAT-AP-SE data in RDF/XML format
	 *
	 * @param dir The directory location where the API-specifications are
	 * @return A String containing DCAT-AP-SE data in RDF/XML format or error
	 *         message
	 */
	@GetMapping(path = "/files/", produces = "text/plain;charset=UTF-8")
	public String restEndpointProduceRdf(@RequestParam(name = "dir", defaultValue = "/apidef") String dir) {
		Manager manager = managerProvider.getObject();
		try {
			DcatResult result = manager.createDcatFromDirectory(dir);
			if(result.hasErrors()) {
				return result.errorReport();
			}

			Util.printToFile(result.rdf(), "dcat.rdf");
			return result.rdf();
			
		} catch (Exception e) {
			logger.error(UNEXPECTED_ERROR + " When generating DCAT from directory: " + dir, e);
			return UNEXPECTED_ERROR;
		}
	}

	/**
	 * REST API Endpoint for creating DCAT-AP-SE data in RDF/XML format, with a quality report of the generated RDF
	 *
	 * @param dir The directory location where the API-specifications are
	 * @return 	200: with RDF/XML and quality report.
	 * 			422: with an error report.
	 * 			500: if generation failed.
	 * 			The body is always a DcatResult as JSON.
	 */
	@GetMapping(path = "/files/report", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DcatResult> restEndpointProduceRdfAndQualityReport(
			@RequestParam(name = "dir", defaultValue = "/apidef") String dir) {
		Manager manager = managerProvider.getObject();
		try {
			DcatResult result = manager.createDcatFromDirectory(dir);
			if (result.hasErrors()) {
				return ResponseEntity.unprocessableContent().body(result);
			}

			Util.printToFile(result.rdf(), "dcat.rdf");
			return ResponseEntity.ok(result);

		} catch (Exception e) {
			logger.error(UNEXPECTED_ERROR + " When generating DCAT from directory: " + dir, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(DcatResult.errors(UNEXPECTED_ERROR));
		}
	}

	/**
	 * Generates DCAT-AP-SE in RDF/XML from one or more API specifications
	 * supplied in the request body.
	 *
	 * Expected body:
	 * { "sources": [ { "name": "catalog.json", "content": "..." } ] }
	 *
	 * @param request SpecRequest: the specifications. Each ApiSource carries a file name and the raw
	 *                specification text. The extension of the name selects the parser (.json, .yaml, .yml or .raml),
	 *                so it must be present and match the format of the content.
	 * 
	 * @return	200: with RDF/XML and quality report.
	 * 			400: if the input is invalid.
	 * 			422: the specifications were read but did not produce valid DCAT-AP-SE.
	 * 			500: if generation failed unexpectedly.
	 *			The body is always a DcatResult as JSON.
	 */
	@PostMapping(path = "/spec", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DcatResult> restEndpointProduceRdfFromSpecRequest(@RequestBody SpecRequest request) {
		List<ApiSource> sources = request.sources();

		List<String> errors = validateSources(sources);
		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest().body(DcatResult.errors(String.join("\n", errors)));
		}
		
		Manager manager = managerProvider.getObject();
		try {
			DcatResult result = manager.createDcat(sources);

			if(result.hasErrors()){
				return ResponseEntity.unprocessableContent().body(result);
			}
			return ResponseEntity.ok(result);

		} catch (Exception e) {
			logger.error(UNEXPECTED_ERROR, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(DcatResult.errors(UNEXPECTED_ERROR));
		}
	}

	private static List<String> validateSources(List<ApiSource> sources) {
		List<String> errors = new ArrayList<>();

		if (sources == null || sources.isEmpty()) {
			errors.add("No specification files supplied");
			return errors;
		}

		for (int i = 0; i < sources.size(); i++) {
			ApiSource apiSource = sources.get(i);
			String label = "File " + (i + 1);

			if (apiSource == null) {
				errors.add(label + " is missing");
				continue;
			}

			if (StringUtils.isBlank(apiSource.name())) {
				errors.add(label + " is missing a file name");
			} else {
				label = "The file '" + apiSource.name() + "'";
				if (!Util.validateFileExtension(apiSource.name())) {
					errors.add(label + " has an invalid file extension. Only .json, .yaml, .yml and .raml are supported.");
				}
			}

			if (StringUtils.isBlank(apiSource.content())) {
				errors.add(label + " is empty");
			}
		}
		return errors;
	}
}
