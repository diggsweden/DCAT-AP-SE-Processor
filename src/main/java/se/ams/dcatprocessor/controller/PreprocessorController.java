// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import se.ams.dcatprocessor.processor.Manager;
import se.ams.dcatprocessor.util.Util;

@RestController
class PreprocessorController {

	private final ObjectProvider<Manager> managerProvider;
	private static final Logger logger = LoggerFactory.getLogger(PreprocessorController.class);

	public record ApiSource(String name, String content) { }

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
	@RequestMapping(value = "/dcat-generation/files/", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String restEndpointProduceRdf(@RequestParam(name = "dir", defaultValue = "/apidef") String dir) {
		Manager manager = managerProvider.getObject();
		String result = "";

		try {
			result = manager.createDcatFromDirectory(dir);
		} catch (Exception e) {
			result = e.getMessage();
		}
		return result;
	}

	/**
	 * Generates DCAT-AP-SE in RDF/XML from one or more API specifications
	 * supplied in the request body.
	 *
	 * Expected body:
	 * { "sources": [ { "name": "catalog.json", "content": "..." } ] }
	 *
	 * @param request SpecRequest: the specifications. Each ApiSource carries a file
	 *                name and the raw
	 *                specification text. The extension of the name selects the
	 *                parser (.json, .yaml, .yml or .raml),
	 *                so it must be present and match the format of the content.
	 * @return 200 with RDF/XML, 400 if the input is invalid, or 422 with an error
	 *         report.
	 */
	@PostMapping(path = "/dcat-generation/spec", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> restEndpointProduceRdfFromSpecRequest(@RequestBody SpecRequest request) {
		List<ApiSource> sources = request.sources();

		List<String> errors = validateSources(sources);
		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest().body(String.join("\n", errors));
		}

		MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
		for (ApiSource apiSource : sources) {
			apiSpecMap.put(apiSource.name(), apiSource.content());
		}

		Manager manager = managerProvider.getObject();
		try {
			String result = manager.createDcat(apiSpecMap);

			// createDcat returns either RDF or a plain-text error report, the content decides the status code
			if (Util.isRdf(result)) {
				return ResponseEntity.ok(result);
			}
			return ResponseEntity.unprocessableContent().body(result);

		} catch (Exception e) {
			logger.error("DCAT generation failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("RDF generation failed");
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
