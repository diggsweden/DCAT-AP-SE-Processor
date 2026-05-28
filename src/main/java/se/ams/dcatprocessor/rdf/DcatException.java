// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import java.util.List;
import java.util.Map;

import se.ams.dcatprocessor.rdf.validate.ValidationError;

public class DcatException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private Map<String, List<ValidationError>> validationErrorsPerFileMap;
	
	public DcatException(String message) {
		super(message);
	}

	public DcatException(String message, Map<String, List<ValidationError>> validationErrorsPerFileMap) {
		super(message);
		this.validationErrorsPerFileMap = validationErrorsPerFileMap;
	}
	
	public Map<String, List<ValidationError>> getValidationResults() {
		return validationErrorsPerFileMap;
	}
	
	public void setValidationResults(Map<String, List<ValidationError>> validationErrorsPerFileMap) {
		this.validationErrorsPerFileMap = validationErrorsPerFileMap;
	}
		
}
