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

import jakarta.annotation.Nonnull;

import se.ams.dcatprocessor.rdf.Cardinality;
import se.ams.dcatprocessor.util.Util;

/**
 * Placeholder for validation errors created when creating DCAT-AP-SE file
 * 
 * @author nacbr
 *
 */
public class ValidationError {
	
	/**
	 * Enums for defining type of error
	 */
	public enum ErrorType {
	    DUPLICATE_URI_WITHIN_FILE,
	    DUPLICATE_URI_BETWEEN_FILES,
	    MANDATORY_VALUE_MISSING,
	    VALUE_OUTSIDE_OF_SPEC,
	    UNKNOWN_KEY,
	    ILLEGAL_FORMAT
	}
	
	private ErrorType errorType;
	
	/**
	 * Filename where the error occurred
	 */
	private String fileName;
	
	/**
	 * Key where the error occurred
	 */
	private String key;
	
	/**
	 * Value where the error occurred
	 */
	private String value;
	
	/**
	 * Description of the error
	 */
	private String description;
	
	private final String UNABLE_ADD_VALIDATION_ERROR_CURRENT_FILE_MISSING = this.getClass() + " : Error validating input data. Reason: Current filename is not set"; 

	
	/**
	 * Convenience constructor that creates a description of the error
	 * depending on the type of error
	 * 
	 * If the list of filenames contains multiple elements it will be concatenated to one string
	 * 
	 * @param errorType - Type of error
	 * @param fileNames - Filename(s) where the error occurred
	 * @param value - Value where the error occurred
	 */
	public ValidationError(@Nonnull ErrorType errorType, @Nonnull String[] fileNames, @Nonnull String value) {
		this.errorType = errorType;
		this.value = value;
		
		/**
		 * The filename(s) must not be null for ValidationError
		 */
		Util.checkNotNull(fileNames, UNABLE_ADD_VALIDATION_ERROR_CURRENT_FILE_MISSING);
		
		fileName = Util.mergeStringsWithSeparator(fileNames, null);
		
		setDescription(createDescription(errorType, fileName, null, value));	
	}
		
	/**
	 * Convenience constructor that creates a description of the error
	 * depending on the type of error
	 * 
	 * @param errorType - Type of error
	 * @param fileName - Filename where the error occurred
	 * @param key - Key where the error occurred
	 * @param value - Value where the error occurred
	 */
	public ValidationError(@Nonnull ErrorType errorType, @Nonnull String fileName, @Nonnull String key, @Nonnull String value) {
		this.errorType = errorType;
		this.fileName = fileName;
		this.key = key;
		this.value = value;
		
		setDescription(createDescription(errorType, fileName, key, value));
		
		//TODO: Add nullcheck for all arguments
	}
	
	//Predefined errormessage
	private final String ERROR_DESCRIPTION_NUMBER_OUTSIDE_SPEC = "The key A occurs B times but the allowed range is C";
	
	/**
	 * Convenience constructor to be used when the error is NUMBER_OUTSIDE_SPEC
	 * Creates a description for this particular error
	 * 
	 * @param fileName - Filename where the error occurred
	 * @param key - Key where the error occurred
	 * @param value - Value where the error occurred
	 * @param cardinality - Contains the allowed range FYI
	 */
	public ValidationError(@Nonnull String fileName, @Nonnull String key, @Nonnull Integer value, Cardinality cardinality) {
		this.errorType = ErrorType.VALUE_OUTSIDE_OF_SPEC;
		this.fileName = fileName;
		this.key = key;
		this.value = value.toString();
		
		String description = Util.createErrorMsg(ERROR_DESCRIPTION_NUMBER_OUTSIDE_SPEC, new String[] {"A", "B", "C"}, new String[] {key, value.toString(), cardinality.getInterval()}); 
		setDescription(description);
	}

	//Predefined errormessage
	private final String ERROR_DESCRIPTION_UNKNOWN_KEY = "The key A does not exist in specification";
	
	/**
	 * Convenience constructor to be used when the error is UNKNOWN_KEY
	 * Creates a description for this particular error
	 * 
	 * @param fileName - Filename where the error occurred
	 * @param key - Key where the error occurred
	 */
	public ValidationError(@Nonnull String fileName, @Nonnull String key) {
		this.errorType = ErrorType.UNKNOWN_KEY;
		this.fileName = fileName;
		this.key = key;
		
		String description = Util.createErrorMsg(ERROR_DESCRIPTION_UNKNOWN_KEY, new String[] {"A"}, new String[] {key}); 
		setDescription(description);
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	//Predefined errormessages
	private final String ERROR_DESCRIPTION_DUPLICATE_WITHIN_FILE = "URI: X exist multiple times in file: Y";
	private final String ERROR_DESCRIPTION_DUPLICATE_BETWEEN_FILES = "URI: X exist in the following files: Y";
	private final String ERROR_DESCRIPTION_ILLEGAL_FORMAT = "The value Y has wrong format for key Z.";
	private final String ERROR_DESCRIPTION_GENERAL = "Key Y with value Z";
	
	/**
	 * Creates a tailormade description of the validation error depending of
	 * the type of error
	 * @param errorType - Type of error
	 * @param fileName - Filename where the error occurred
	 * @param key - Key where the error occurred
	 * @param value - Value where the error occurred
	 * @return - The description
	 */
	private String createDescription(ErrorType errorType, String fileName, String key, String value) {

		if(errorType.equals(ErrorType.DUPLICATE_URI_WITHIN_FILE)) {
				return Util.createErrorMsg(ERROR_DESCRIPTION_DUPLICATE_WITHIN_FILE, new String[] {"X", "Y"}, new String[] {value, fileName}); 
		}
		
		if(errorType.equals(ErrorType.DUPLICATE_URI_BETWEEN_FILES)) {
			return Util.createErrorMsg(ERROR_DESCRIPTION_DUPLICATE_BETWEEN_FILES, new String[] {"X", "Y"}, new String[] {value, fileName});	
		}
		
		if(errorType.equals(ErrorType.ILLEGAL_FORMAT)) {
			return Util.createErrorMsg(ERROR_DESCRIPTION_ILLEGAL_FORMAT, new String[] {"Y", "Z"}, new String[] {value, key});
		}
				
		//General description as none else was found
		return Util.createErrorMsg(ERROR_DESCRIPTION_GENERAL, new String[] {"Y", "Z"}, new String[] {value, key});
		
	}
	
}
