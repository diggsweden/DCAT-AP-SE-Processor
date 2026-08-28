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
	NONNEGATIVEINTEGER("xsd:nonNegativeInteger"),
	DECIMAL("xsd:decimal"),
	DURATION("xsd:duration"),
	ANYURI("xsd:anyURI"),
	WKTLITERAL("geo:wktLiteral");

	private String name;
	
	private InputType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
