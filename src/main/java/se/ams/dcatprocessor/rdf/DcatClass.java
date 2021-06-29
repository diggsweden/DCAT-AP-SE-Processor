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

package se.ams.dcatprocessor.rdf;

/**
 * Enums for the primary and supportive classes in the DCAT-AP-SE Specification
 * 
 * @author nacbr
 */
public enum DcatClass {
		
	CATALOG("catalog"),
	DATASET("dataset"), 
	DISTRIBUTION("distribution"),
	DATASERVICE("dataservice"),
	AGENT("agent"),
	ORGANISATION("organization"),
	DOCUMENT("document"),
	LICENSEDOCUMENT("licensedocument"),
	STANDARD("standard"),
	PERIODOFTIME("periodoftime"),
	LOCATION("location"),
	VOICE("voice"),					//VCARD
	ADDRESS("address"),  			//VCARD
	OFFER("offer"),
	QUALIFIED_ATTRIBUTION("qualifiedattribution"),
	RIGHTS_STATEMENT("rightsstatement"),
	CHECKSUM("checksum"),
	RELATIONSHIP("relationship"),
	PROVENANCE_STATEMENT("provenancestatement");
	
	private String name;
	
	private DcatClass(String name) {
		this.name = name;
	}
	
	public static DcatClass getEnum(String name) {

		if(name == null) {
			return null;
		}
		
		DcatClass[] dcatClasses =  DcatClass.values();
		
		for (DcatClass dcatClass : dcatClasses) {
			
			if(dcatClass.getName().equals(name)) {
				return dcatClass;
			}
		}
		
		return null;
	}
	
	public String getName() {
		return name;
	}

	@Override 
	public String toString() { 
	    return name; 
	}

}
