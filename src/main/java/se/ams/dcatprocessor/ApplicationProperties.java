// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Loads the application properties from default application.properties file
 * and makes them accessible
 * 
 * @author nacbr
 *
 */
public class ApplicationProperties {

	private Properties applicationProperties = new Properties();

	public ApplicationProperties() {
		// application.properties located at src/main/resource
		Resource resource = new ClassPathResource("/application.properties");
		try {
			this.applicationProperties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException ex) {
			Logger.getLogger(ApplicationProperties.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String getProperty(String propertyName) {
		return this.applicationProperties.getProperty(propertyName);
	}
}
