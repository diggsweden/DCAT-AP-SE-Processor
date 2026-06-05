// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

/**
 * CLass for defining input types 
 * @author nacbr
 *
 */
public enum InputType {

	STRING("xsd:string"),
	DATE("xsd:date"),
	DATETIME("xsd:dateTime"),
	GYEAR("xsd:gYear"),
	INTEGER("xsd:integer"),
	DECIMAL("xsd:decimal"),
	DURATION("xsd:duration"),
	ANYURI("xsd:anyURI"),
	CLASS("class"),
	PHONENUMBER("phoneNumber");	//For VCARD phonenumber
	
	
	private String name;
	
	private InputType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static InputType toEnum(String name) {
		
		if(name == null) {
			return null;
		}
		
		InputType[] inputTypes = InputType.values();
		
		for (InputType inputType : inputTypes) {
			if (inputType.getName().equals(name)) {
				return inputType;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
