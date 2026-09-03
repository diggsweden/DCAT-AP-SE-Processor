// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

/**
 * Enums for the primary and supportive classes in the DCAT-AP-SE Specification
 * The second value is the id of the template that describes the class in bundle.json.
 * 
 * @author nacbr
 */
public enum DcatClass {
		
	CATALOG("catalog", "dcat:Catalog"),
	DATASET("dataset", "dcat:Dataset"),
	DATASETSERIES("datasetseries", "dcat:DatasetSeries"),
	DISTRIBUTION("distribution", "dcat:Distribution"),
	DATASERVICE("dataservice", "dcat:DataService"),
	AGENT("agent", "dcat:foaf:Agent"),
	ORGANISATION("organization", "dcat:contactPoint"),
	DOCUMENT("document", "foaf:Document"),
	LICENSEDOCUMENT("licensedocument", "dcterms:LicenseDocument"),
	STANDARD("standard", "dcterms:Standard"),
	PERIODOFTIME("periodoftime", "dcat:dcterms:temporal_da"),
	LOCATION("location", "dcat:dcterms:spatial_bb_da"),
	VOICE("voice", "vc:hasTelephone"),
	ADDRESS("address", "vc:hasAddress"),
	OFFER("offer", "schema:Offer"),
	QUALIFIED_ATTRIBUTION("qualifiedattribution", "dcat:prov:qualifiedAttribution"),
	RIGHTS_STATEMENT("rightsstatement", "dcat:odrs:RSSA"),
	CHECKSUM("checksum", "dcat:spdx:checksum_di"),
	RELATIONSHIP("relationship", "dcat:qualifiedRelation"),
	PROVENANCE_STATEMENT("provenancestatement", "dcat:dcterms:provenance_da");
	
	private String name;
	private String templateId;
	
	private DcatClass(String name, String templateId) {
		this.name = name;
		this.templateId = templateId;
	}
	
    public String getTemplateId() {
        return templateId;
    }
	
	public String getName() {
		return name;
	}

	@Override 
	public String toString() { 
	    return name; 
	}

}
