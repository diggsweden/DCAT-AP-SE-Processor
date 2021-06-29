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
