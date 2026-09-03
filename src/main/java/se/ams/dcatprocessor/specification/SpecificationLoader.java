// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.ams.dcatprocessor.rdf.DcatException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
* TODO: Read bundlePath from application.properties. 
* CardinalityHandler and SingleInputValidator are static instances, 
* both uses DcatSpecification which needs the SpecificationLoader.
* This prevents the usage of @Value to read from the properties file.
*/

/**
 * Reads the DCAT-AP-SE bundle from the classpath.
 */
public class SpecificationLoader {

    private static final String DCAT_AP_SE_BUNDLE = "dcat-ap-se-301-bundle.json";
    private final String bundlePath;

    public SpecificationLoader() {
        this(DCAT_AP_SE_BUNDLE);
    }

    /** Reads a named bundle. Used by tests and by future version bump. */
    public SpecificationLoader(String bundlePath) {
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