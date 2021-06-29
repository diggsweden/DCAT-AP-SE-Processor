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

package se.ams.dcatprocessor.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import se.ams.dcatprocessor.ApplicationProperties;

public class DcatPropertyHandler {
	
	private static DcatPropertyHandler instance;

	//Holds the cardinalityvalues
	private HashMap<String, String> cardinalityValue;
	
	//Holds the typevalues
	private HashMap<String, String[]> typeValues;
	
	private static final String DCAT_PROPERTY_FILE_KEY = "dcat.specification.properties";
	
	private DcatPropertyHandler() {
		loadProperties();
	}
	
	
	public static DcatPropertyHandler getInstance() {
		if(instance == null) {
			instance = new DcatPropertyHandler();
		}
		
		return instance;
	}
	
	/**
	 * Delimiter for propertyvalues
	 * Like this: dataset.adms\:identifier=0..n|xsd:\string
	 */
	private final String PIPE = "\\|";

	/**
	 * Delimiter for typevalues in the second part of the propertyvalue.
	 * That is after the | delimiter
	 * Like this: dataset.dcterms\:issued=0..n|xsd\:date,xsd\:dateTime,xsd\:gYear
	 */
	private final String COMMA = "\\,";
	
	/**
	 * Loads the cardinalities for the respective DCAT-element into a hashmap
	 */
	private void loadProperties() {
		cardinalityValue = new HashMap<String, String>();
		typeValues = new HashMap<String, String[]>();
		
		ApplicationProperties ap = new ApplicationProperties();

		Properties properties = new Properties();
		
		try {

			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ap.getProperty(DCAT_PROPERTY_FILE_KEY));
			
			properties.load(inputStream);
			
			inputStream.close();
			
			String[] keys = properties.keySet().toArray(new String[0]);
			
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				
				String value = properties.getProperty(key).trim();
				
				checkPropertyKeyFormat(key);
				checkPropertyValueFormat(value);
				
				//Split the propertyvalue
				String[] valueSplit = value.split(PIPE);
				
				//Add the cardinalityvalue
				cardinalityValue.put(key, valueSplit[0]);
				
				//Add the typevalue
				typeValues.put(key, valueSplit[1].split(COMMA));
	
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String[] getPropertyKeys() {
		return cardinalityValue.keySet().toArray(new String[0]);
	}

	public String getPropertyValueCardinality(String key) {
		if(Util.isNullOrEmpty(key)) {
			return null;
		}
		return cardinalityValue.get(key);
	}

	public String[] getPropertyValueTypes(String key) {
		if(Util.isNullOrEmpty(key)) {
			return null;
		}
		return typeValues.get(key);
	}
	
	//Regexp for checking correctness of property key=value format
	private final Pattern PROPERTY_KEY_FORMAT = Pattern.compile("[a-zA-Z]+\\.{1}[a-zA-Z]+\\:{1}[a-zA-Z\\-]+");
	
	private void checkPropertyKeyFormat(String key) {
		if(!PROPERTY_KEY_FORMAT.matcher(key).matches()) {
			throw new IllegalArgumentException("Propertykey " + key + " has invalid format. Permitted format is xx.xx:xx");
		}
	}
	
	/* 
	 * Regexp for allowed values for properties
	 */
	private final Pattern propertyValueFormat1 = Pattern.compile("[1][\\|]{1}.*");
	private final Pattern propertyValueFormat0_n = Pattern.compile("[0][\\.]{2}[n]{1}[\\|]{1}.*");
	private final Pattern propertyValueFormat0_1 = Pattern.compile("[0][\\.]{2}[1]{1}[\\|]{1}.*");
	private final Pattern propertyValueFormat1_n = Pattern.compile("[1][\\.]{2}[N|n]{1}[\\|]{1}.*");

	/**
	 * Checks if a string (propertyvalue) has the correct format according to property notation
	 * @param value - To be checked
	 * @throws IllegalArgumentException - If the notation is not correct
	 */
	private void checkPropertyValueFormat(String value) throws IllegalArgumentException{
		/*
		 * Check if values of the propertyfile matches either 1, 0..n or 1..n
		 */
		if(!propertyValueFormat1.matcher(value).matches()) {
			
			if(!propertyValueFormat0_n.matcher(value).matches()) {
				
				if(!propertyValueFormat0_1.matcher(value).matches()) {
					
					if(!propertyValueFormat1_n.matcher(value).matches()) {
						
						throw new IllegalArgumentException("Propertyfile: Illegal value for cardinality found "
								+ value + " Allowed values are 1, 1..n, 0..1 or 0..n followed by | and then a string");
					}
				}
			}	
		}
		
	}
	
}
