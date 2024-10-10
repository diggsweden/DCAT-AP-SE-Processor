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

package se.ams.dcatprocessor.rdf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.ams.dcatprocessor.util.DcatPropertyHandler;

//TODO: CardinalityHandler needs to be redesigned to read properties dynamically from a file specifiecd in application.properties or similar
/**
 * Handler for reading cardinalities for DCAT-elements from a property-file
 * @author nacbr
 */
@ApplicationScoped
public class CardinalityHandler {

	@Inject
	DcatPropertyHandler dcatPropertyHandler;

	private Map<DcatClass, Map<String, Cardinality>> cardinalities;
	

	@PostConstruct
	void init() {
		loadCardinalities();
	}

	//Regexp for checking correctness of property key=value format
	private static final String CHARACTER_DOT = "\\.";
	
	/**
	 * Loads the cardinalities for the respective DCAT-element into a hashmap
	 */
	public void loadCardinalities() {
		
		cardinalities = new HashMap<DcatClass, Map<String, Cardinality>>();
		
		DcatClass[] dcatClasses = DcatClass.values();
		for (DcatClass dcatClass : dcatClasses) {
			cardinalities.put(dcatClass, new HashMap<>());
		}

		String[] keys = dcatPropertyHandler.getPropertyKeys();
		
		for (int i = 0; i < keys.length; i++) {
			
			String[] propertyKeySplit = keys[i].split(CHARACTER_DOT);
			
			DcatClass dcatClass = DcatClass.getEnum(propertyKeySplit[0]);
			
			if(dcatClass == null) {
				throw new IllegalArgumentException("Property: " + propertyKeySplit[0] + " is not a DCAT-vocabulary");
			}
			
			/**
			 * Get the cardinalityvalue for the property
			 */
			String cardinalityValue = dcatPropertyHandler.getPropertyValueCardinality(keys[i]);
			
			/**
			 * Create a Cardinality with the string value 1, 0..n or 1..n
			 */
			Cardinality c = createCardinality(cardinalityValue);
			
			/**
			 * Store the Cardinality in a map where the key is the corresponding DCAT-class Catalog, Dataset etc
			 * The value is another map with the where each property dcterms, dcat, foaf etc is the key to
			 * each corresponding cardinality value
			 */
			cardinalities.get(dcatClass).put(propertyKeySplit[1], c);	
			
		}
	
		
	}
	
	/* 
	 * Regexp for allowed notations of cardinality
	 */
	private final Pattern PROPERTY_VALUE_FORMAT_1 = Pattern.compile("[1]");
	private final Pattern PROPERTY_VALUE_FORMAT_0_n = Pattern.compile("[0][\\.]{2}[n]{1}");
	private final Pattern PROPERTY_VALUE_FORMAT_0_1 = Pattern.compile("[0][\\.]{2}[1]{1}");
	private final Pattern PROPERTY_VALUE_FORMAT_1_n = Pattern.compile("[1][\\.]{2}[N|n]{1}");

	/**
	 * Creates a Cardinality object from a string if the string is a valid
	 * cardinality notation
	 * @param value - To be checked
	 * @return - Cardinality object
	 * @throws IllegalArgumentException - If the notation is not correct
	 */
	private Cardinality createCardinality(String value) throws IllegalArgumentException{
		/*
		 * Check if values of the propertyfile matches either 1, 0..n or 1..n
		 */
		if(PROPERTY_VALUE_FORMAT_1.matcher(value).matches()) {
			return new Cardinality(1, 1);	
		} else if(PROPERTY_VALUE_FORMAT_0_n.matcher(value).matches()) {
			return new Cardinality(0, "n");
		} else if(PROPERTY_VALUE_FORMAT_0_1.matcher(value).matches()) {
			return new Cardinality(0, 1);
		} else if(PROPERTY_VALUE_FORMAT_1_n.matcher(value).matches()) {
			return new Cardinality(1, "n");
		} else {
			throw new IllegalArgumentException("Propertyfile: Illegal value for cardinality found "
												+ value + " Allowed values are 1, 1..n, 0..1 or 0..n");
		}
		
	}
	
	/**
	 * Returns all the loaded cardinalities for a primary class
	 * @param dcatClass - The primary class
	 * @return - The Map with all the cardinalities
	 */
	public Map<String, Cardinality> getCardinalities(DcatClass dcatClass) {
		if(dcatClass == null) {
			throw new IllegalArgumentException("DCATClass is null");
		}
		return cardinalities.get(dcatClass);
	}
	
	
}
