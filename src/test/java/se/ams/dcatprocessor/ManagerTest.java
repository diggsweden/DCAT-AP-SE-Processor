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

package se.ams.dcatprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatException;

public class ManagerTest {
    private static Manager manager;
  
    // region Testdata
    private static String expectedRDF = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rdf:RDF\n" +
            "\txmlns:dcat=\"http://www.w3.org/ns/dcat#\"\n" +
            "\txmlns:dcterms=\"http://purl.org/dc/terms/\"\n" +
            "\txmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
            "\txmlns:vcard=\"http://www.w3.org/2006/vcard/ns#\"\n" +
            "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
            "\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
            "\txmlns:locn=\"http://www.w3.org/ns/locn#\"\n" +
            "\txmlns:schema=\"https://schema.org/\"\n" +
            "\txmlns:prov=\"http://www.w3.org/ns/prov#\"\n" +
            "\txmlns:adms=\"http://www.w3.org/ns/adms/\"\n" +
            "\txmlns:odrs=\"http://schema.theodi.org/odrs/\"\n" +
            "\txmlns:spdx=\"http://spdx.org/rdf/terms#\"\n" +
            "\txmlns:dcatap=\"http://data.europa.eu/r5r#\"\n" +
            "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n" +
            "<foaf:Agent rdf:about=\"https://www.example.se/result.rdf#publisher\">\n" +
            "\t<foaf:name rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Redpill Linpro AB Catalog</foaf:name>\n" +
            "\t<dcterms:type rdf:resource=\"http://purl.org/adms/publishertype/NationalAuthority\"/>\n" +
            "</foaf:Agent>\n" +
            "<dcat:Catalog rdf:about=\"https://www.af.se\">\n" +
            "\t<dcterms:license rdf:resource=\"https://www.apache.org/licenses/LICENSE-2.0\"/>\n" +
            "\t<foaf:homepage rdf:resource=\"https://www.af.se/home\"/>\n" +
            "\t<dcterms:language rdf:resource=\"http://publications.europa.eu/resource/authority/language/ENG\"/>\n" +
            "\t<dcterms:language rdf:resource=\"http://publications.europa.eu/resource/authority/language/SWE\"/>\n" +
            "\t<dcterms:description xml:lang=\"sv\">Annotation exempel från arbetsförmedlingen</dcterms:description>\n" +
            "\t<dcterms:title xml:lang=\"en\">Annotation example RAML 1.0</dcterms:title>\n" +
            "\t<dcterms:title xml:lang=\"sv\">Annotation exempel RAML 1.0</dcterms:title>\n" +
            "\t<dcterms:issued rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2021-03-01</dcterms:issued>\n" +
            "\t<dcterms:modified rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2021-03-16</dcterms:modified>\n" +
            "\t<dcat:themeTaxonomy rdf:resource=\"http://publications.europa.eu/resource/authority/data-theme\"/>\n" +
            "\t<dcterms:publisher rdf:resource=\"https://www.example.se/result.rdf#publisher\"/>\n" +
            "\t<dcat:dataset>\n" +
            "\t\t<dcat:Dataset rdf:about=\"https://www.example.se/result.rdf#dataset1\">\n" +
            "\t\t\t<dcterms:accessRights rdf:resource=\"http://publications.europa.eu/resource/authority/access-right/PUBLIC\"/>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"en\">profession</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"en\"> info</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"en\"> redpill</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"en\"> linpro</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"sv\">yrke</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"sv\"> info</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"sv\"> redpill</dcat:keyword>\n" +
            "\t\t\t<dcat:keyword xml:lang=\"sv\"> linpro</dcat:keyword>\n" +
            "\t\t\t<dcterms:spatial rdf:resource=\"https://www.geonames.org/6695072/european-union.html\"/>\n" +
            "\t\t\t<dcterms:language rdf:resource=\"http://publications.europa.eu/resource/authority/language/ENG\"/>\n" +
            "\t\t\t<dcterms:language rdf:resource=\"http://publications.europa.eu/resource/authority/language/SWE\"/>\n" +
            "\t\t\t<dcterms:description xml:lang=\"sv\">Exempel beskrivning</dcterms:description>\n" +
            "\t\t\t<dcterms:title xml:lang=\"sv\">Datamängd</dcterms:title>\n" +
            "\t\t\t<dcat:theme rdf:resource=\"http://publications.europa.eu/resource/authority/data-theme/TRAN\"/>\n" +
            "\t\t\t<dcat:theme rdf:resource=\"http://publications.europa.eu/resource/authority/data-theme/EDUC\"/>\n" +
            "\t\t\t<dcterms:issued rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2021-03-05</dcterms:issued>\n" +
            "\t\t\t<dcterms:publisher rdf:resource=\"https://www.example.se/result.rdf#publisher\"/>\n" +
            "\t\t\t<dcterms:temporal rdf:nodeID=\"TESTNODEID\"/>\n" +
            "\t\t\t<dcat:contactPoint rdf:resource=\"http://www.af11.se\"/>\n" +
            "\t\t</dcat:Dataset>\n" +
            "\t</dcat:dataset>\n" +
            "</dcat:Catalog>\n" +
            "<dcterms:PeriodOfTime rdf:nodeID=\"TESTNODEID\">\n" +
            "\t<dcat:startDate rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2021-03-16</dcat:startDate>\n" +
            "\t<dcat:endDate rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2021-03-25</dcat:endDate>\n" +
            "</dcterms:PeriodOfTime>\n" +
            "<vcard:Organization rdf:about=\"http://www.af11.se\">\n" +
			"\t<rdf:type rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Organisation</rdf:type>\n" +        
            "\t<vcard:fn rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Redpill Linpro AB Contact</vcard:fn>\n" +
            "\t<vcard:hasEmail rdf:resource=\"mailTo:admin2@test.se\"/>\n" +
            "\t<vcard:hasAddress rdf:nodeID=\"TESTNODEID\"/>\n" +
            "\t<vcard:hasTelephone rdf:nodeID=\"TESTNODEID\"/>\n" +
            "</vcard:Organization>\n" +
            "<vcard:Address rdf:nodeID=\"TESTNODEID\">\n" +
            "\t<vcard:country-name rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\"> Sverige</vcard:country-name>\n" +
            "\t<vcard:street-address rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">Testgatan 5</vcard:street-address>\n" +
            "\t<vcard:postal-code rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\"> 76543</vcard:postal-code>\n" +
            "\t<vcard:locality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\"> Tranemo</vcard:locality>\n" +
            "</vcard:Address>\n" +
            "<vcard:Voice rdf:nodeID=\"TESTNODEID\">\n" +
            "\t<vcard:hasValue rdf:resource=\"tel:98876554\"/>\n" +
            "</vcard:Voice>\n" +
            "\n" +
            "</rdf:RDF>";

    private static String ramlApidef =  "#%RAML 1.0\n" +
            "title: Annotation exempel api\n" +
            "description: Annotation exempel api är ett påhittat api som använder sig av annotations för att skapa metadata för DCAT-AP-SE.\n" +
            "version: 1.0.0\n" +
            "\n" +
            "annotationTypes:\n" +
            "  dcat-catalog:\n" +
            "    properties:\n" +
            "      about: string\n" +
            "      title: string\n" +
            "      description: string\n" +
            "      publisher:\n" +
            "        name :string\n" +
            "        type :string\n" +
            "        homepage :string\n" +
            "        mbox :string\n" +
            "      license: string\n" +
            "      issued: string\n" +
            "      language: string\n" +
            "      modified: string\n" +
            "      homepage: string\n" +
            "      location:\n" +
            "        centroid: string\n" +
            "        bbox: string\n" +
            "        geometry: string\n" +
            "      rights: string\n" +
            "      hasPart: string\n" +
            "      isPartOf: string\n" +
            "  dcat-dataset:\n" +
            "    properties:\n" +
            "      title: string\n" +
            "      description: string\n" +
            "      publisher:\n" +
            "        name :string\n" +
            "        type :string\n" +
            "        homepage :string\n" +
            "        mbox :string\n" +
            "      creator: string\n" +
            "      qualifiedAttribution: string\n" +
            "      contactPoint:\n" +
            "        type :string\n" +
            "        name :string\n" +
            "        email :string\n" +
            "        phone :string\n" +
            "        adress :string\n" +
            "      distribution:\n" +
            "        belongsTo: string\n" +
            "        title: string\n" +
            "        description: string\n" +
            "        accessURL: string\n" +
            "        downloadURL: string\n" +
            "        format: string\n" +
            "        accessService: string\n" +
            "        temporalResolution: string\n" +
            "        spatialResolutionInMeters: string\n" +
            "        byteSize: string\n" +
            "        language: string\n" +
            "        issued: string\n" +
            "        modified: string\n" +
            "        status: string\n" +
            "        availability: string\n" +
            "        license: string\n" +
            "        rights: string\n" +
            "        checksum: string\n" +
            "        page: string\n" +
            "        conformsTo: string\n" +
            "      keyword: string\n" +
            "      theme: string\n" +
            "      identifier: string\n" +
            "      adms: string\n" +
            "      issued: string\n" +
            "      modified: string\n" +
            "      language: string\n" +
            "      landingPage: string\n" +
            "      conformsTo: string\n" +
            "      location:\n" +
            "        centroid: string\n" +
            "        bbox: string\n" +
            "        geometry: string\n" +
            "      temporal:\n" +
            "        startDate: string\n" +
            "        endDate: string\n" +
            "      temporalResolution: string\n" +
            "      spatialResolutionInMeters: string\n" +
            "      accrualPeriodicity: string\n" +
            "      versionInfo: string\n" +
            "      versionNotes: string\n" +
            "      source: string\n" +
            "      accessRights: string\n" +
            "      offers: string\n" +
            "      hasVersion: string\n" +
            "      isVersionOf: string\n" +
            "      isReferencedBy: string\n" +
            "      relation: string\n" +
            "      qualifiedRelation: string\n" +
            "      page: string\n" +
            "      provenance: string\n" +
            "  dcat-dataservice:\n" +
            "    properties:\n" +
            "      title: string\n" +
            "      description: string\n" +
            "      endpointURL: string\n" +
            "      endpointDescription: string\n" +
            "      publisher:\n" +
            "        name :string\n" +
            "        type :string\n" +
            "        homepage :string\n" +
            "        mbox :string\n" +
            "      contactPoint:\n" +
            "        type :string\n" +
            "        name :string\n" +
            "        email :string\n" +
            "        phone :string\n" +
            "        adress :string\n" +
            "      type: string\n" +
            "      keyword: string\n" +
            "      theme: string\n" +
            "      conformsTo: string\n" +
            "      servesDataset: string\n" +
            "      license: string\n" +
            "      accessRights: string\n" +
            "      landingPage: string\n" +
            "      page: string\n" +
            "  dcat-agent:\n" +
            "    properties:\n" +
            "      name: string\n" +
            "  dcat-organization:\n" +
            "    properties:\n" +
            "      type: string\n" +
            "      name: string\n" +
            "      email: string\n" +
            "\n" +
            "(dcat-catalog):\n" +
            "  about: https://www.af.se\n" +
            "  title-sv: Annotation exempel RAML 1.0\n" +
            "  title-en: Annotation example RAML 1.0\n" +
            "  description-sv: Annotation exempel från arbetsförmedlingen\n" +
            "  publisher:\n" +
            "    about: https://www.example.se/result.rdf#publisher\n" +
            "    name: Redpill Linpro AB Catalog\n" +
            "    type: NationalAuthority\n" +
            "  license: https://www.apache.org/licenses/LICENSE-2.0\n" +
            "  issued: 2021-03-01\n" +
            "  modified: 2021-03-16\n" +
            "  homepage: https://www.af.se/home\n" +
            "\n" +
            "(dcat-dataset):\n" +
            "  about: https://www.example.se/result.rdf#dataset1\n" +
            "  title-sv: Datamängd 1\n" +
            "  description-sv: Exempel beskrivning 1\n" +
            "  publisher:\n" +
            "    about: https://www.example.se/result.rdf#publisher\n" +
            "    name: Redpill Linpro AB\n" +
            "    type: NationalAuthority\n" +
            "  contactPoint:\n" +
            "    about: www.af11.se\n" +
            "    name: Redpill Linpro AB Contact\n" +
            "    type: Organisation\n" +
            "    email: admin2@test.se\n" +
            "    address: Testgatan 5; 76543; Tranemo; Sverige\n" +
            "    phone: 98876554\n" +
            "  keyword-sv: yrke; info; redpill; linpro\n" +
            "  keyword-en: profession; info; redpill; linpro\n" +
            "  theme: TRAN; EDUC\n" +
            "  issued: 2021-03-05\n" +
            "  spatialUrl: https://www.geonames.org/6695072/european-union.html\n" +
            "  temporal:\n" +
            "    startDate: 2021-03-16\n" +
            "    endDate: 2021-03-25\n" +
            "  accessRights: Public\n" +
            "\n";
    // endregion

    @BeforeEach
    void setup() {
        manager = new Manager();
    }

    @Test
    void testValidRaml1() throws Exception {
        File apidefDir = new File("src/test/resources/apidef/raml_1");

        try {
            String result = manager.createDcatFromDirectory(apidefDir.toString());
            // nodeIDs are generated dynamically, changing them allows for comparison
            String convertedRdf = replaceBetween(result, "rdf:nodeID=\"", "\"", true, true, "rdf:nodeID=\"TESTNODEID\"");
            assertEquals(expectedRDF, convertedRdf);
        } catch (DcatException e) {

        }
    }

    @Test
    void testValidJsonOas() throws Exception {
        File apidefDir = new File("src/test/resources/apidef/json_oas");

        try {
            String result = manager.createDcatFromDirectory(apidefDir.toString());
            // nodeIDs are generated dynamically, changing them allows for comparison
            String convertedRdf = replaceBetween(result, "rdf:nodeID=\"", "\"", true, true, "rdf:nodeID=\"TESTNODEID\"");
            assertEquals(expectedRDF, convertedRdf);
        } catch (DcatException e) {

        }
    }

    @Test
    void testValidYaml() throws Exception {
        File apidefDir = new File("src/test/resources/apidef/yaml_oas");

        try {
            String result = manager.createDcatFromDirectory(apidefDir.toString());
            // nodeIDs are generated dynamically, changing them allows for comparison
            String convertedRdf = replaceBetween(result, "rdf:nodeID=\"", "\"", true, true, "rdf:nodeID=\"TESTNODEID\"");
            assertEquals(expectedRDF, convertedRdf);
        } catch (DcatException e) {

        }
    }

    @Test
    void testValidJsonSeparate() throws Exception {
        File apidefDir = new File("src/test/resources/apidef/json_separate");

        try {
            String result = manager.createDcatFromDirectory(apidefDir.toString());
            // nodeIDs are generated dynamically, changing them allows for comparison
            String convertedRdf = replaceBetween(result, "rdf:nodeID=\"", "\"", true, true, "rdf:nodeID=\"TESTNODEID\"");
            assertEquals(expectedRDF, convertedRdf);
        } catch (DcatException e) {

        }
    }

    @Test
    void testCreateDcat() throws Exception {
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        apiSpecMap.put("apifile", ramlApidef);
        try {
            String result = manager.createDcat(apiSpecMap);
            assertTrue(!result.isEmpty());
        } catch (DcatException e) {

        }
    }

    // region Utility methods
    private String replaceBetween(String input,
                                  String start, String end,
                                  boolean startInclusive,
                                  boolean endInclusive,
                                  String replaceWith) {
        start = Pattern.quote(start);
        end = Pattern.quote(end);
        return input.replaceAll("(" + start + ")" + ".*" + "(" + end + ")",
                (startInclusive ? "" : "$1") + replaceWith + (endInclusive ? "" : "$2"));
    }
    // endregion
}
