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
import java.util.Map;

import jakarta.annotation.Nonnull;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.util.Util;

/**
 * Placeholder for ValidationErrors during program execution
 * 
 * @author nacbr
 *
 */
public class ValidationErrorStorage {

	private static ValidationErrorStorage instance;
	
	/**
	 * Stores the validationresults for each file
	 */
	private Map<String, List<ValidationError>> validationErrorsPerFileMap;
	
	public ValidationErrorStorage() {
		validationErrorsPerFileMap = new HashMap<String, List<ValidationError>>();
	}
	
	public static ValidationErrorStorage getInstance() {
		if(instance == null) {
			instance = new ValidationErrorStorage();
		}	
		return instance;
	}

	/**
	 * Resets the saves ValidationErrors
	 */
	public void resetErrors() {
		validationErrorsPerFileMap = new HashMap<String, List<ValidationError>>();
	}

	
	/**
	 * Predefined error message
	 */
	private final String ERROR_FILENAME_IS_NULL = this.getClass() + " Error validating input data. Reason: Filename for the file being validated is not set"; 
	
	/**
	 * Adds a ValidationError to the storage with the fileName as key.
	 * 
	 * @param fileName The key under which the validation error is stored
	 * @param validationError The validation error to store
	 * @throws DcatException If fileName is null
	 */
	public void setValidationError(@Nonnull String fileName, @Nonnull ValidationError validationError) throws DcatException {
		
		/**
		 * The file that is being validated has to be set with method setCurrentFileName(String fileName)
		 */
		Util.checkNotNull(fileName, ERROR_FILENAME_IS_NULL);
		
		//TODO: Add nullcheck also for ValidationError
		
		if(validationErrorsPerFileMap.containsKey(fileName)) {
			validationErrorsPerFileMap.get(fileName).add(validationError);
		} else {
			List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
			validationErrorsList.add(validationError);
			validationErrorsPerFileMap.put(fileName, validationErrorsList);
		}
		
	}
	
	public Map<String, List<ValidationError>> getValidationErrors() {
		return validationErrorsPerFileMap;
	}
	
	public boolean hasValidationErrors() {
		return validationErrorsPerFileMap.keySet().size() > 0;
	}

}
