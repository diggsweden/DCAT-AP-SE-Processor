// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import java.util.ArrayList;
import java.util.List;

import se.ams.dcatprocessor.rdf.validate.ValidationError;

public class DcatException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private List<ValidationError> validationErrors = new ArrayList<>();
	
	public DcatException(String message) {
		super(message);
	}

	public DcatException(String message, List<ValidationError> validationErrors) {
		super(message);
		this.validationErrors = validationErrors;
	}
	
	public List<ValidationError> getValidationResults() {
		return validationErrors;
	}
	
	public void setValidationResults(List<ValidationError> validationErrors) {
		this.validationErrors= validationErrors;
	}	
}
