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
