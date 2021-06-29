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

package se.ams.dcatprocessor.models;

import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import se.ams.dcatprocessor.rdf.namespace.SCHEMA;
import se.ams.dcatprocessor.rdf.namespace.SPDX;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConverterHelpClass {

    public static String uriToDcat = "to_dcat_files/to_dcat/";
    public static String uriToDcatCatalog = "to_dcat_files/to_catalog/";
    public static String uriToDcatSupportive = "to_dcat_files/supportive_files/";
    public static String mandatoryFileName = "mandatory.json";
    public static String convertFileName = "convertmapToDcat.json";
    public static String uriToLanguageDcat = uriToDcatSupportive+"languages.json";
    public static String catalogFileName = "catalog.json";

    public static String toDcatString = "metadata";
    public static String toDcatMandatoryString = "isMandatory";
    public static String toDcatAboutString = "about";
    public static String languages = "dcterms:language";

    public static HashMap<String, String> supportiveFile;
    static {
        supportiveFile = new HashMap<>();
        supportiveFile.put("dcterms:accessRights", "accessRights.json");
        supportiveFile.put("dcatap:availability", "availability.json");
        supportiveFile.put("rdf:type", "contactType.json");
        supportiveFile.put("dcterms:license", "license.json");
        supportiveFile.put("formatMedia", "media.json");
        supportiveFile.put("formatGeo", "geographicmedia.json");
        supportiveFile.put("dcat:theme", "theme.json");
        supportiveFile.put("publisher-dcterms:type", "type.json");
        supportiveFile.put("creator-dcterms:type", "type.json");
        supportiveFile.put("Attribution-dcterms:type", "type.json");
        supportiveFile.put("DataService-dcterms:type", "arcitectureStyle.json");
        supportiveFile.put("dcat:hadRole", "userRole.json");
        supportiveFile.put("adms:status", "status.json");
        supportiveFile.put("dcterms:accrualPeriodicity", "accrualPeriodicity.json");
    }

    public static HashMap<String, String> addressObject;
    static {
        addressObject = new HashMap<>();
        addressObject.put("StreetAddress", "vcard:street-address");
        addressObject.put("PostalCode", "vcard:postal-code");
        addressObject.put("Locality", "vcard:locality");
        addressObject.put("Country", "vcard:country-name");
    }

    public static List<String> tagWithUri = Arrays.asList("about","foaf:homepage", "license", "homepage", "dcat:accessURL", "dcat:downloadURL", "dcat:landingPage",
            "dcterms:isReferencedBy", "dcterms:relation", "dcat:endpointURL", "dcat:endpointDescription", "dcat:accessService", "dcterms:relation", "dcat:qualifiedRelation",
            "dcat:landingPage", "odrs:attributionURL", "odrs:jurisdiction", "odrs:reuserGuidelines", "schema:mainEntityOfPage", "dcat:servesDataset", "dcat:hasVersion", "dcat:isVersionOf",
            "dcterms:source");
    public static List<String> tagWithUriMail = Arrays.asList("foaf:mbox", "vcard:hasEmail");

    public static Boolean isNestedObjects(String objectKeyName) {
         if (objectKeyName.equals(DCAT.CONTACT_POINT.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.TEMPORAL.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.CREATOR.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.PUBLISHER.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.RIGHTS_STATEMENT.getLocalName()) ||
                 objectKeyName.equals(PROV.ATTRIBUTION.getLocalName()) ||
                 objectKeyName.equals(SPDX.CHECKSUM.getLocalName()) ||
                 objectKeyName.equals(SCHEMA.OFFERS.getLocalName()) ||
                 objectKeyName.equals(FOAF.DOCUMENT.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.STANDARD.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName()) ||
                 objectKeyName.equals(DCAT.QUALIFIED_RELATION.getLocalName()) ||
                 objectKeyName.equals(DCTERMS.SPATIAL.getLocalName())) {
             return true;
         }
         else {
             return false;
         }
    }

    public static Boolean isNestedLanguageObjects(String objectKeyName) {
        if (objectKeyName.equals(DCTERMS.LICENSE_DOCUMENT.getLocalName()) ||
                objectKeyName.equals(SPDX.CHECKSUM.getLocalName()) ||
                objectKeyName.equals(SCHEMA.OFFERS.getLocalName()) ||
                objectKeyName.equals(FOAF.DOCUMENT.getLocalName()) ||
                objectKeyName.equals(DCTERMS.STANDARD.getLocalName()) ||
                objectKeyName.equals(DCTERMS.RIGHTS_STATEMENT.getLocalName())) {
            return true;
        }
        else {
            return false;
        }
    }
}


