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
import java.util.Map;
import java.util.Set;

import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.util.Util;

/**
 * Class used for checking that each URI is unique within a
 * file and also between files.
 * Gererates an aggregated validationerror message for each file
 * 
 * @author nacbr
 *
 */
public class MultipleURIValidator {

	/**
	 * Stores the number of times an URI occures within each file
	 * The number is updated each time a URI is added
	 * @see se.ams.dcatprocessor.rdf.RDFWorker
	 */
	private Map<String, Map<String, Integer>> countedURIsPerFileMap;
	
	
	private String currentFileName;
	
	/**
	 * Predefined error message
	 */
	private final String ERROR_CURRENT_FILENAME_NOT_SET = this.getClass() + " Error validating input data. Reason: Filename for the file being validated is not set"; 
	
	public MultipleURIValidator() {
		countedURIsPerFileMap = new HashMap<String, Map<String, Integer>>();
	}
	
	
	/**
	 * Predefined error message
	 */
	protected final String ERROR_SUBMITTED_URI_IS_NULL = this.getClass() + " Error validating input data. Reason: Submitted URI is null"; 
	
	/**
	 * Adds an URI to the file you have set with the method setCurrentFileName()
	 * @link setCurrentFileName()
	 * At the same time it counts the occurrence of the URI within the file
	 * 
	 * @param uriString - The URI you want to add to current file
	 */
	public void addUri(String uriString) {

		/**
		 * Check that the filename for the file being validated is set
		 */
		Util.checkNotNull(getCurrentFileName(), ERROR_CURRENT_FILENAME_NOT_SET);
		
		/**
		 * Check that the filename for the file being validated is set
		 */
		Util.checkNotNull(uriString, ERROR_SUBMITTED_URI_IS_NULL);

		/**
		 * If there is an entry for this URI - Add to that entry
		 * Else create an new entry
		 */
		if (countedURIsPerFileMap.containsKey(uriString)) {

			Map<String, Integer> uriPerFileMap = countedURIsPerFileMap.get(uriString);

			/**
			 * If there is existing data for this URI for a particular file:
			 * Add one occurrence for that file
			 * Else...add new occurrence for that file
			 */
			if (uriPerFileMap.containsKey(currentFileName)) {

				int numberPerFile = uriPerFileMap.get(currentFileName).intValue();
				uriPerFileMap.put(currentFileName, ++numberPerFile);

			} else {
				uriPerFileMap.put(currentFileName, 1);
			}

		} else {
			Map<String, Integer> countsPerFileMap = new HashMap<String, Integer>();
			countsPerFileMap.put(currentFileName, 1);
			countedURIsPerFileMap.put(uriString, countsPerFileMap);
		}
	}
		
	/**
	 * Counts the occurrences for each URI in a file and also between the files.
	 * Generates an ValidationError if an URI is found multiple times within a 
	 * file OR multiple times between files
	 * 
	 * @return True if no ValidationErrors exist. False if ValidationErrors exist
	 */
	public boolean validate() {
		
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		/**
		 * Check if each URI is unique within each file and across all the files
		 */
		Set<String> uriStrings = countedURIsPerFileMap.keySet();
		
		for (String uriString : uriStrings) {
			 Map<String, Integer> uriPerFileMap = countedURIsPerFileMap.get(uriString);
			 
			 Set<String> fileNames = uriPerFileMap.keySet();
			 
			 /**
			  * If the list of filenames is bigger than 1, the URI exist in more than one file
			  */
			 if(fileNames.size() > 1) {
				 /**
				  * Concatenate all the filenams to create an informative errormessage
				  * then add an tailormade ValidationError for this error
				  */
				 String[] names = fileNames.toArray(new String[0]);
				validationErrorStorage.setValidationError(Util.mergeStringsWithSeparator(names, null), new ValidationError(ErrorType.DUPLICATE_URI_BETWEEN_FILES, names, uriString));
			 } 
			 
			 /**
			  * Loop through all the filenames and see if the URI also occurs multiple times WITHIN a file
			  */
			 for (String fileName : fileNames) {
				 Integer number = uriPerFileMap.get(fileName);
				 /**
				  * If the URI also exist multiple times within a file. 
				  * Then create a tailormade ValidationError for this error
				  */
				 if(number > 1) {
					 validationErrorStorage.setValidationError(fileName, new ValidationError(ErrorType.DUPLICATE_URI_WITHIN_FILE, new String[] {fileName}, uriString));
				 }
			}
			
		}
				
		/** 
		 * Validation is OK if there are no ValidationErrors
		 */
		return !validationErrorStorage.hasValidationErrors(); 

	}
	
	public String getCurrentFileName() {
		return currentFileName;
	}
	public void setCurrentFileName(String fileName) {
		currentFileName = fileName;
	}

}
