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

package se.ams.dcatprocessor.rdf.validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;

import se.ams.dcatprocessor.rdf.Cardinality;
import se.ams.dcatprocessor.rdf.CardinalityHandler;
import se.ams.dcatprocessor.rdf.DcatClass;
import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.util.Util;

public class CardinalityValidator {
	
	
	private static CardinalityValidator instance;
	
	private String currentFileName;
	
	private CardinalityValidator() {}
	
	public static CardinalityValidator getInstance() {
		if(instance == null) {
			instance = new CardinalityValidator();
		}
		return instance;
	}
	
	
	/*
	 * Predefined errormessages
	 */
	private final String ERROR_CURRENT_FILENAME_NOT_SET = this.getClass() + " Error validating input data. Reason: Filename for the file being validated is not set";
	
	/**
	 * Compares the inputvalues with the DCAT-AP-SE Specification and throws exception if
	 * values are not included in the specification or if any value occurs more frequently than allowed
	 * in the specification
	 * @param dcatClass //TODO describe
	 * @param values - The input values to check
	 * @param checkedElsewhere //TODO describe
	 * @throws DcatException - Is thrown if there is a disconformity
	 */
	public boolean validate(DcatClass dcatClass, MultiValuedMap<String, String> values, List<String> checkedElsewhere) {
	
		//Check that the filename for the file being validated is set
		Util.checkNotNull(currentFileName, ERROR_CURRENT_FILENAME_NOT_SET);
				
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		Map<String, Cardinality> cardinalities = CardinalityHandler.getInstance().getCardinalities(dcatClass);
		
		HashMap<String, Integer> countedKeyNames = new HashMap<>();

		//Count the number of occurrences for each value-key E.g.dcterms:title and store in countValueName
		Set<String> vKeySet = values.keySet();
		for (String vKey : vKeySet) {
			/**
			 * Check if key exists in the Map
			 */
			if(cardinalities.containsKey(vKey)) {
				countedKeyNames.put(vKey, Integer.valueOf(values.get(vKey).size()));
			} else {
				/**
				 * The key is unknown..a validationerror
				 */
				validationErrorStorage.setValidationError(currentFileName, new ValidationError(currentFileName, vKey));
			}
		}

		/**
		 * Compare the number of occurrences of each value-key(E.g.dcterms:title)
		 * with the allowed range according to the specification
		 */
		Set<String> cKeySet = cardinalities.keySet();
		for (String cKey : cKeySet) {
			Cardinality c = cardinalities.get(cKey);
			Integer number = countedKeyNames.get(cKey);

			/**
			 * Check first if this key is exempted from begin checked
			 */
			if(!checkedElsewhere.contains(cKey)) {
				/**
				 * This key does not exist among the input values.
				 */
				if (number == null) {
					/**
					 * The specification specifies at least one .. create a validationerror
					 */
					if (c.isOneOrMore()) {
						validationErrorStorage.setValidationError(currentFileName, new ValidationError(currentFileName, cKey, 0, c));
					}
				} 
				else {
					// Check that the input value occurs within the allowed range
					if(!c.isInsideCardinality(number.intValue())) {
						validationErrorStorage.setValidationError(currentFileName, new ValidationError(currentFileName, cKey, number, c));
					}				
				}
			}
		}
		return !validationErrorStorage.hasValidationErrors();
	}
		
	

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

}
