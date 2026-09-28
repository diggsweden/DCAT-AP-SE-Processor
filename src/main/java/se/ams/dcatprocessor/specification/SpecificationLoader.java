// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.ams.dcatprocessor.rdf.DcatException;

/**
 * Reads the DCAT-AP-SE bundle from the classpath.
 */
@Component
public class SpecificationLoader {

    private final String bundlePath;

    public SpecificationLoader(@Value("${dcat.bundle-path}") String bundlePath) {
        this.bundlePath = bundlePath;
    }

    public Map<String, DcatProperty> load() {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(bundlePath)) {
            if (inputStream == null) {
                throw new DcatException("DCAT spec not found on classpath: " + bundlePath);
            }
            SpecificationTranslator translator = new SpecificationTranslator();
            return translator.translate(new ObjectMapper().readTree(inputStream));

        } catch (IOException e) {
            throw new DcatException("Failed to load DCAT spec at: " + bundlePath);
        }
    }
}