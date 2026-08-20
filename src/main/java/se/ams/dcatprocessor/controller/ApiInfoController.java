// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@RestController
class ApiInfoController {

    private final Map<String, String> apiInfo;
    private final String documentation;
    private final String swaggerUiPath;

    ApiInfoController(
            @Value("classpath:static/openapi.yaml") Resource specification,
            @Value("${api-info.documentation:}") String documentation,
            @Value("${springdoc.swagger-ui.path:/api-docs}") String swaggerUiPath) throws IOException {
        this.apiInfo = readAPIInfoFacts(specification);
        this.documentation = documentation;
        this.swaggerUiPath = swaggerUiPath;
    }

    @GetMapping(path = "/api-info", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> apiInfo() {
        Map<String, String> response = new LinkedHashMap<>(apiInfo);
        response.put("apiDocumentation", resolveDocumentationUrl());
        return response;
    }

    private static Map<String, String> readAPIInfoFacts(Resource specification) throws IOException {
        JsonNode info = new ObjectMapper(new YAMLFactory())
            .readTree(specification.getInputStream())
            .get("info");

        return Map.of(
            "apiName", info.get("title").asText(),
            "apiVersion", info.get("version").asText(),
            "apiReleased", info.get("x-api-released").asText(),
            "apiStatus", info.get("x-api-status").asText());
    }

    /**
     * The tool is self-hosted, so there is no official documentation URL. Falls back to
     * the responding instance's Swagger UI unless api-info.documentation is configured.
     */
    private String resolveDocumentationUrl() {
        if (StringUtils.hasText(documentation)) {
            return documentation;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(swaggerUiPath)
                .toUriString();
    }
}