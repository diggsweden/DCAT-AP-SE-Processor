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

import java.util.HashMap;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.LOCN;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;

import se.ams.dcatprocessor.rdf.namespace.ADMS;
import se.ams.dcatprocessor.rdf.namespace.DCATEXT;
import se.ams.dcatprocessor.rdf.namespace.ODRS;
import se.ams.dcatprocessor.rdf.namespace.SCHEMA;
import se.ams.dcatprocessor.rdf.namespace.SPDX;

/**
 * Convenience class for LUT QNAME->IRI
 * 
 * @author nacbr
 *
 */
public class VocabularyStringToIRI {
	
	private static ValueFactory valueFactory = SimpleValueFactory.getInstance(); 
	
	private static final HashMap<String, IRI> RDF_MAP = new HashMap<>();

	static {
		RDF_MAP.put("rdf:type", RDF.TYPE);
	}
	
	
	private static final HashMap<String, IRI> DCAT_MAP = new HashMap<>();

	static {
		DCAT_MAP.put("dcat:catalog", DCAT.CATALOG);
		DCAT_MAP.put("dcat:themeTaxonomy", DCAT.THEME_TAXONOMY);
		DCAT_MAP.put("dcat:dataset", DCAT.DATASET);
		DCAT_MAP.put("dcat:service", DCAT.HAS_SERVICE);
		DCAT_MAP.put("dcat:accessURL", DCAT.ACCESS_URL);
		DCAT_MAP.put("dcat:keyword", DCAT.KEYWORD);
		DCAT_MAP.put("dcat:theme", DCAT.THEME);
		DCAT_MAP.put("dcat:downloadURL", DCAT.DOWNLOAD_URL);
		DCAT_MAP.put("dcat:endpointURL", DCAT.ENDPOINT_URL);
		DCAT_MAP.put("dcat:landingPage", DCAT.LANDING_PAGE);
		DCAT_MAP.put("dcat:servesDataset", DCAT.SERVES_DATASET);
		DCAT_MAP.put("dcat:accessService", DCAT.ACCESS_SERVICE);
		DCAT_MAP.put("dcat:temporalResolution", DCAT.TEMPORAL_RESOLUTION);
		DCAT_MAP.put("dcat:spatialResolutionInMeters", DCAT.SPATIAL_RESOLUTION_IN_METERS);
		DCAT_MAP.put("dcat:byteSize", DCAT.BYTE_SIZE);
		DCAT_MAP.put("dcat:endpointDescription", DCAT.ENDPOINT_DESCRIPTION);
		DCAT_MAP.put("dcat:startDate", DCAT.START_DATE);
		DCAT_MAP.put("dcat:endDate", DCAT.END_DATE);
		DCAT_MAP.put("dcat:centroid", DCAT.CENTROID);
		DCAT_MAP.put("dcat:bbox", DCAT.BBOX);
		DCAT_MAP.put("dcat:hadRole", DCAT.HAD_ROLE);
		DCAT_MAP.put("dcat:qualifiedRelation", DCAT.QUALIFIED_RELATION);
		DCAT_MAP.put("dcat:contactPoint", DCAT.CONTACT_POINT);
		DCAT_MAP.put("dcat:distribution", DCAT.DISTRIBUTION);
		DCAT_MAP.put("dcat:hasVersion", DCATEXT.HAS_VERSION);
		DCAT_MAP.put("dcat:isVersionOf", DCATEXT.IS_VERSION_OF);
	}
		
	private static final HashMap<String, IRI> DCTERMS_MAP = new HashMap<>();
		
	static {
		DCTERMS_MAP.put("dcterms:type", DCTERMS.TYPE);
		DCTERMS_MAP.put("dcterms:title", DCTERMS.TITLE);
		DCTERMS_MAP.put("dcterms:description", DCTERMS.DESCRIPTION);
		DCTERMS_MAP.put("dcterms:publisher", DCTERMS.PUBLISHER);
		DCTERMS_MAP.put("dcterms:issued", DCTERMS.ISSUED);
		DCTERMS_MAP.put("dcterms:language", DCTERMS.LANGUAGE);
		DCTERMS_MAP.put("dcterms:modified", DCTERMS.MODIFIED);
		DCTERMS_MAP.put("dcterms:spatial", DCTERMS.SPATIAL);
		DCTERMS_MAP.put("dcterms:license", DCTERMS.LICENSE);
		DCTERMS_MAP.put("dcterms:rights", DCTERMS.RIGHTS);
		DCTERMS_MAP.put("dcterms:hasPart", DCTERMS.HAS_PART);
		DCTERMS_MAP.put("dcterms:isPartOf", DCTERMS.IS_PART_OF);
		DCTERMS_MAP.put("dcterms:format", DCTERMS.FORMAT);
		DCTERMS_MAP.put("dcterms:conformsTo", DCTERMS.CONFORMS_TO);
		DCTERMS_MAP.put("dcterms:accessRights", DCTERMS.ACCESS_RIGHTS);
		DCTERMS_MAP.put("dcterms:creator", DCTERMS.CREATOR);
		DCTERMS_MAP.put("dcterms:identifier", DCTERMS.IDENTIFIER);
		DCTERMS_MAP.put("dcterms:isReferencedBy", DCTERMS.IS_REFERENCED_BY);
		DCTERMS_MAP.put("dcterms:relation", DCTERMS.RELATION);
		DCTERMS_MAP.put("dcterms:accrualPeriodicity", DCTERMS.ACCRUAL_PERIODICITY);
		DCTERMS_MAP.put("dcterms:provenance", DCTERMS.PROVENANCE_STATEMENT);
		DCTERMS_MAP.put("dcterms:source", DCTERMS.SOURCE);
		DCTERMS_MAP.put("dcterms:temporal", DCTERMS.TEMPORAL);
	}
	
	private static final HashMap<String, IRI> FOAF_MAP = new HashMap<>();
	
	static {
		FOAF_MAP.put("foaf:name", FOAF.NAME);
		FOAF_MAP.put("foaf:homepage", FOAF.HOMEPAGE);
		FOAF_MAP.put("foaf:mbox", FOAF.MBOX);
		FOAF_MAP.put("foaf:page", FOAF.PAGE);
	}
	
	private static final HashMap<String, IRI> PROV_MAP = new HashMap<>();
	static {
		PROV_MAP.put("prov:qualifiedAttribution", PROV.QUALIFIED_ATTRIBUTION);
		PROV_MAP.put("prov:agent", PROV.AGENT_PROP);
	}

	private static final HashMap<String, IRI> OWL_MAP = new HashMap<>();
	static {
		OWL_MAP.put("owl:versionInfo", OWL.VERSIONINFO);
	}
	
	private static final HashMap<String, IRI> VCARD4_MAP = new HashMap<>();
	static {
		VCARD4_MAP.put("vcard:fn", VCARD4.FN);
		VCARD4_MAP.put("vcard:hasEmail", VCARD4.HAS_EMAIL);
		VCARD4_MAP.put("vcard:hasTelephone", VCARD4.HAS_TELEPHONE);
		VCARD4_MAP.put("vcard:hasAddress", VCARD4.HAS_ADDRESS);
		VCARD4_MAP.put("vcard:street-address", VCARD4.STREET_ADDRESS);
		VCARD4_MAP.put("vcard:postal-code", VCARD4.POSTAL_CODE);
		VCARD4_MAP.put("vcard:locality", VCARD4.LOCALITY);
		VCARD4_MAP.put("vcard:country-name", VCARD4.COUNTRY_NAME);
		VCARD4_MAP.put("vcard:hasValue", VCARD4.HAS_VALUE);
	}
	
	private static final HashMap<String, IRI> DCATAP = new HashMap<>();
	static {
		DCATAP.put("dcatap:availability", valueFactory.createIRI("http://data.europa.eu/r5r/availability", ""));
	}
	
	private static final HashMap<String, IRI> LOCATION = new HashMap<>();
	static {
		LOCATION.put("locn:geometry", LOCN.GEOMETRY);
	}
	
	private static final HashMap<String, IRI> SCHEMA_MAP = new HashMap<>();
	static {
		SCHEMA_MAP.put("schema:description", SCHEMA.DESCRIPTION);
		SCHEMA_MAP.put("schema:mainEntityOfPage", SCHEMA.MAIN_ENTITY_OF_PAGE);
		SCHEMA_MAP.put("schema:offers", SCHEMA.OFFERS);
	}
	
	private static final HashMap<String, IRI> ADMS_MAP = new HashMap<>();
	static {
		ADMS_MAP.put("adms:identifier", ADMS.IDENTIFIER);
		ADMS_MAP.put("adms:status", ADMS.STATUS);
		ADMS_MAP.put("adms:versionNotes", ADMS.VERSION_NOTES);
	}
	
	private static final HashMap<String, IRI> ODRS_MAP = new HashMap<>();
	static {
		ODRS_MAP.put("odrs:attributionText", ODRS.ATTRIBUTION_TEXT);
		ODRS_MAP.put("odrs:attributionURL", ODRS.ATTRIBUTION_URL);
		ODRS_MAP.put("odrs:copyrightNotice", ODRS.COPYRIGHT_NOTICE);
		ODRS_MAP.put("odrs:copyrightStatement", ODRS.COPYRIGHT_STATEMENT);
		ODRS_MAP.put("odrs:copyrightYear", ODRS.COPYRIGHT_YEAR);
		ODRS_MAP.put("odrs:copyrightHolder", ODRS.COPYRIGHT_HOLDER);
		ODRS_MAP.put("odrs:jurisdiction", ODRS.JURISDICTION);
		ODRS_MAP.put("odrs:reuserGuidelines", ODRS.REUSER_GUIDELINES);
	}

	private static final HashMap<String, IRI> SPDX_MAP = new HashMap<>();
	static {
		SPDX_MAP.put("spdx:checksum", SPDX.CHECKSUM);		
		SPDX_MAP.put("spdx:checksumValue", SPDX.CHECKSUM_VALUE);
		SPDX_MAP.put("spdx:algorithm", SPDX.ALGORITHM);
		SPDX_MAP.put("spdx:checksumAlgorithm_sha1", SPDX.CHECKSUM_ALGORITHM_SHA1);
	}


	public static IRI getIRI(String qName) {
		if(DCAT_MAP.containsKey(qName)) {
			return DCAT_MAP.get(qName);
		}
		
		if(DCTERMS_MAP.containsKey(qName)) {
			return DCTERMS_MAP.get(qName);
		}
		
		if(FOAF_MAP.containsKey(qName)) {
			return FOAF_MAP.get(qName);
		}
		
		if(PROV_MAP.containsKey(qName)) {
			return PROV_MAP.get(qName);
		}
		
		if(OWL_MAP.containsKey(qName)) {
			return OWL_MAP.get(qName);
		}
		
		if(VCARD4_MAP.containsKey(qName)) {
			return VCARD4_MAP.get(qName);
		}
		
		if(DCATAP.containsKey(qName)) {
			return DCATAP.get(qName);
		}
		
		if(RDF_MAP.containsKey(qName)) {
			return RDF_MAP.get(qName);
		}
		
		if(LOCATION.containsKey(qName)) {
			return LOCATION.get(qName);
		}
		
		if(SCHEMA_MAP.containsKey(qName)) {
			return SCHEMA_MAP.get(qName);
		}
		
		if(ADMS_MAP.containsKey(qName)) {
			return ADMS_MAP.get(qName);
		}
		
		if(ODRS_MAP.containsKey(qName)) {
			return ODRS_MAP.get(qName);
		}
		
		if(SPDX_MAP.containsKey(qName)) {
			return SPDX_MAP.get(qName);
		}
		
		return null;
	}

}
