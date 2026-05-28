// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

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
