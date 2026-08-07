// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApiInfoControllerRestTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Map<String, String> apiInfo;

    @BeforeEach
    void fetchApiInfo() {
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
            "/api-info",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        apiInfo = response.getBody();
        assertNotNull(apiInfo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "apiName", 
        "apiVersion", 
        "apiReleased", 
        "apiDocumentation", 
        "apiStatus"
    })
    void testThatAttributeIsPresentAndNotBlank(String attribute) {
        assertNotNull(apiInfo.get(attribute), attribute + " is missing");
        assertFalse(apiInfo.get(attribute).isBlank(), attribute + " is blank");
    }

    @Test
    void testThatApiReleasedIsAnIsoDate() {
        assertDoesNotThrow(() -> LocalDate.parse(apiInfo.get("apiReleased")));
    }

    @Test
    void testThatApiStatusIsAKnownLifecycleState() {
        assertTrue(Set.of("beta", "active", "deprecated").contains(apiInfo.get("apiStatus")));
    }
}