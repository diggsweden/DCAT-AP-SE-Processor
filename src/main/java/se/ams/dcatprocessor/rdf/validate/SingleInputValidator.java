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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.lang.NonNull;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.util.DcatPropertyHandler;
import se.ams.dcatprocessor.util.Util;

/**
 * Class for validating all the values put into the DCAT-AP specification file
 * 
 * @author nacbr
 *
 */
public class SingleInputValidator {
	
	private static SingleInputValidator instance;
	
	//Map from an InputType to regex
	private static HashMap<InputType, Pattern> inputTypeToRegexMap;
	
	//Map for the key string of the inputvalue to an InputType
	private static HashMap<String, List<InputType>> keyToInputTypeMap;
	
	/**
	 * The name of the file currently being validated
	 */
	private String currentFileName;
	
	private SingleInputValidator() {
		mapInputTypeToRegex();
		loadInputTypeDefinitions();
	}
	

	public static SingleInputValidator getInstance() {
		if(instance == null) {
			instance = new SingleInputValidator();	
		}
		return instance;
	}

	/**
	 * Predefined error messages
	 */
	private final String ERROR_CURRENT_FILENAME_NOT_SET = this.getClass() + " Error validating input data. Reason: Filename for the file being validated is not set"; 
	private static String ERROR_KEY_OR_VALUE_IS_NULL = "Error validating type: Input X is null";
	private static String ERROR_NO_CORRESPONDING_INPUT_TYPE = "Error validating type: Key X is not defined";
	
	//Predefined frequentry used string
	private final String TEL_PREFIX = "tel:";
	
	/**
	 * Checks if a value has the correct format according to the InputType definition(regex) mapped to the key
	 * @param key - The key
	 * @param value - The value
	 * @return T/F depending of the result
	 * @throws DcatException - If there is an error during validation
	 */
	public boolean validateData(@NonNull String key, @NonNull String value) throws DcatException {

		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		//Check that the filename for the file being validated is set
		Util.checkNotNull(currentFileName, ERROR_CURRENT_FILENAME_NOT_SET);
	
		//Check that key or value is not null
		Util.checkNotNull(key, Util.createErrorMsg(ERROR_KEY_OR_VALUE_IS_NULL, new String[] {"X"}, new String[] {"key"}));
		Util.checkNotNull(value, Util.createErrorMsg(ERROR_KEY_OR_VALUE_IS_NULL, new String[] {"X"}, new String[] {"value"}));
		
		List<InputType> inputTypes =  keyToInputTypeMap.get(key);
		
		/**
		 * Check that the input type string has a corresponding InputType
		 * For ex that foaf:name maps to InputType STRING("xsd:string")
		 */
		Util.checkNotNull(inputTypes, Util.createErrorMsg(ERROR_NO_CORRESPONDING_INPUT_TYPE, new String[] {"X"}, new String[] {key}));
		
		/**
		 * Loop through all the InputTypes for a key. Get the corresponding regex and see if it matches
		 */
		for (InputType inputType : inputTypes) {
			Pattern pattern = inputTypeToRegexMap.get(inputType);
			
			/**
			 * Special case for classes...complex types can't be validated as a whole here
			 */
			if(inputType.equals(InputType.CLASS)) {
				return true;
			}
			
			/**
			 * Special case for URI since it's easier to use built-in
			 * functions to validate URI
			 */
			if(inputType.equals(InputType.ANYURI)) {
				if(!Util.isURI(value)) {
					validationErrorStorage.setValidationError(currentFileName, new ValidationError(ErrorType.ILLEGAL_FORMAT, getCurrentFileName(), key, value));
					return false;
				}
				return true;
			}
			
			//TODO: Write a regex for this
			/**
			 * Special case for phonenumer...my BAD
			 * 
			 */
			if(inputType.equals(InputType.PHONENUMBER)) {
				if(value.contains(TEL_PREFIX)) {
					String[] split = value.split(TEL_PREFIX);
					if(pattern.matcher(split[1]).matches()) {
						return true;
					}
				}
					
			}
			
			if(pattern.matcher(value).matches()) {
				return true;
			}
		}
		/**
		 * If we end up here there is a validation error...
		 * Add validation error with current filename as key
		 */
		validationErrorStorage.setValidationError(currentFileName, new ValidationError(ErrorType.ILLEGAL_FORMAT, getCurrentFileName(), key, value));
		
		return false;
		
	}
	
	public String getCurrentFileName() {
		return currentFileName;
	}
	public void setCurrentFileName(String fileName) {
		currentFileName = fileName;
	}
	
	public List<InputType> getInputTypes(String key) {
		return keyToInputTypeMap.get(key);
	}
	

	/**
	 * Maps an InputType to a regex(Pattern)
	 */
	private void mapInputTypeToRegex() {
		//TODO: Improve REGEX Check that they cover all OUR cases...verify with tests
		//Add the patterns to XSD-Types
		inputTypeToRegexMap = new HashMap<InputType, Pattern>();
		inputTypeToRegexMap.put(InputType.STRING, Pattern.compile(".*"));
		inputTypeToRegexMap.put(InputType.INTEGER, Pattern.compile("^\\d{1,10}$"));
		inputTypeToRegexMap.put(InputType.DECIMAL, Pattern.compile("^[0-9]+([\\.][0-9]+)?$"));
		inputTypeToRegexMap.put(InputType.DATE, Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}"));
		inputTypeToRegexMap.put(InputType.DATETIME, Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}[T]{1}[0-9]{2}[:]{1}[0-9]{2}[:]{1}[0-9]{2}"));
		inputTypeToRegexMap.put(InputType.GYEAR, Pattern.compile("[0-9]{4}"));
		inputTypeToRegexMap.put(InputType.DURATION, Pattern.compile("P(?:(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|\\d+W)"));
		inputTypeToRegexMap.put(InputType.PHONENUMBER, Pattern.compile("^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$"));
	}
	
	//Predefined errormessage
	private static String ERROR_NO_CORRESPONDING_INPUT_TYPE_DEFINITION = "Error loading properties: No corresponding definition found for type X";
	
	/**
	 * Delimiter for property-keys
	 * For example if a propertykey looks like this: catalog.dcat:dataset
	 */
	private final String DOT = "\\.";
	
	/**
	 * Gets the properties and stores the inputtype definitions
	 * For example everything after the "|" sign dcterms:issued=0..1|xsd:date,xsd:dateTime,xsd:gYear
	 */
	private void loadInputTypeDefinitions() {
		DcatPropertyHandler instance = DcatPropertyHandler.getInstance();
		
		String[] propertyKeys = instance.getPropertyKeys();
		
		keyToInputTypeMap = new HashMap<String, List<InputType>>();
		
		for (String key : propertyKeys) {
			
			String[] keySplit = key.split(DOT);
			
			/**
			 * If typedefinition does not exist for this key...add the typedefinition
			 * For example dcterms:title
			 */
			if(Util.isNullOrEmpty(keyToInputTypeMap.get(keySplit[1]))) {
				
				ArrayList<InputType> inputTypes = new ArrayList<InputType>();
				
				/**
				 * A propertykey can have multiple inputtypes.
				 * For example: dcterms:issued=0..1|xsd:date,xsd:dateTime,xsd:gYear
				 */
				String[] values = instance.getPropertyValueTypes(key);
				
				for (String value : values) {
					InputType inputType = InputType.toEnum(value);
					//If there is no predefied type for the value its an error
					Util.checkNotNull(inputType,  Util.createErrorMsg(ERROR_NO_CORRESPONDING_INPUT_TYPE_DEFINITION, new String[] {"X"}, new String[] {value}));
					inputTypes.add(inputType);
				}
				
				/**
				 * We want to store the inputtypes using the second part of the key.
				 * For example with a key like this: "catalog.dcat:dataset" we use the second part: dcat:dataset
				 * since this is the key that will be used to validate the real inputs
				 */
				keyToInputTypeMap.put(keySplit[1], inputTypes);
			}
			
		}
	}

}
