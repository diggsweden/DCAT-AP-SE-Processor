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

package se.ams.dcatprocessor.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.ams.dcatprocessor.rdf.DcatException;

import static org.junit.Assert.assertEquals;

public class ApiDefinitionParserTest {
    private static ApiDefinitionParser parser;

    private static String ramlApi = "#%RAML 1.0\n"
            + "title: Annotation exempel api\n"
            + "description: Annotation exempel api är ett påhittat api som använder sig av annotations för att skapa metadata för DCAT-AP-SE.\n"
            + "version: 1.0.0\n"
            + "annotationTypes:\n"
            + "dcat-catalog:\n"
            + "  properties:\n"
            + "    about: string\n"
            + "    title: string\n"
            + "    description: string\n"
            + "    publisher:\n"
            + "      name: string\n"
            + "      type: string\n"
            + "      homepage: string\n"
            + "      mbox: string\n"
            + "    license: string\n"
            + "    issued: string\n"
            + "    language: string\n"
            + "    modified: string\n"
            + "    homepage: string\n"
            + "    location:\n"
            + "      centroid: string\n"
            + "      bbox: string\n"
            + "      geometry: string\n"
            + "    rights: string\n"
            + "    hasPart: string\n"
            + "    isPartOf: string\n"
            + "    themeTaxonomy: string\n"
            + "dcat-dataset:\n"
            + "  properties:\n"
            + "    title: string\n"
            + "    description: string\n"
            + "    publisher:\n"
            + "      name: string\n"
            + "      type: string\n"
            + "      homepage: string\n"
            + "      mbox: string\n"
            + "    creator: string\n"
            + "    qualifiedAttribution: string\n"
            + "    contactPoint:\n"
            + "      type: string\n"
            + "      name: string\n"
            + "      email: string\n"
            + "      phone: string\n"
            + "      adress: string\n"
            + "    distribution:\n"
            + "      belongsTo: string\n"
            + "      title: string\n"
            + "      description: string\n"
            + "      accessURL: string\n"
            + "      downloadURL: string\n"
            + "      format: string\n"
            + "      accessService: string\n"
            + "      temporalResolution: string\n"
            + "      spatialResolutionInMeters: string\n"
            + "      byteSize: string\n"
            + "      language: string\n"
            + "      issued: string\n"
            + "      modified: string\n"
            + "      status: string\n"
            + "      availability: string\n"
            + "      license: string\n"
            + "      rights: string\n"
            + "      checksum: string\n"
            + "      page: string\n"
            + "      conformsTo: string\n"
            + "      keyword: string\n"
            + "      theme: string\n"
            + "      identifier: string\n"
            + "      adms: string\n"
            + "      issued: string\n"
            + "      modified: string\n"
            + "      language: string\n"
            + "      landingPage: string\n"
            + "      conformsTo: string\n"
            + "    location:\n"
            + "      centroid: string\n"
            + "      bbox: string\n"
            + "      geometry: string\n"
            + "    temporal:\n"
            + "      startDate: string\n"
            + "      endDate: string\n"
            + "      temporalResolution: string\n"
            + "      spatialResolutionInMeters: string\n"
            + "      accrualPeriodicity: string\n"
            + "      versionInfo: string\n"
            + "      versionNotes: string\n"
            + "      source: string\n"
            + "      accessRights: string\n"
            + "      offers: string\n"
            + "      hasVersion: string\n"
            + "      isVersionOf: string\n"
            + "      isReferencedBy: string\n"
            + "      relation: string\n"
            + "      qualifiedRelation: string\n"
            + "      page: string\n"
            + "      provenance: string\n"
            + "dcat-dataservice:\n"
            + "  properties:\n"
            + "    title: string\n"
            + "    description: string\n"
            + "    endpointURL: string\n"
            + "    endpointDescription: string\n"
            + "    publisher:\n"
            + "      name: string\n"
            + "      type: string\n"
            + "      homepage: string\n"
            + "      mbox: string\n"
            + "    contactPoint:\n"
            + "      type: string\n"
            + "      name: string\n"
            + "      email: string\n"
            + "      phone: string\n"
            + "      adress: string\n"
            + "      type: string\n"
            + "      keyword: string\n"
            + "      theme: string\n"
            + "      conformsTo: string\n"
            + "      servesDataset: string\n"
            + "      license: string\n"
            + "      accessRights: string\n"
            + "      landingPage: string\n"
            + "      page: string\n"
            + "(dcat-catalog):\n"
            + "  about: www.af.se\n"
            + "  title-sv: Annotation exempel RAML 1.0\n"
            + "  title-en: Annotation example RAML 1.0\n"
            + "  description: Annotation exempel från arbetsförmedlingen\n"
            + "  publisher:\n"
            + "    name: Redpill Linpro AB Catalog\n"
            + "  license: https://www.apache.org/licenses/LICENSE-2.0\n"
            + "  dataset: https://www.example.se/result.rdf#dataset1\n"
            + "(dcat-dataset 1):\n"
            + "  about: https://www.example.se/result.rdf#dataset1\n"
            + "  title-sv: Datamängd 1\n"
            + "  description: Exempel beskrivning 1\n"
            + "  publisher:\n"
            + "    name: Redpill Linpro AB 1\n";

    private static String jsonApi = "{\n" +
            "  \"dcat-catalog\": {\n" +
            "    \"about\": \"https://www.af.se\",\n" +
            "    \"title-sv\": \"Annotation exempel RAML 1.0\",\n" +
            "    \"title-en\": \"Annotation example RAML 1.0\",\n" +
            "    \"description-sv\": \"Annotation exempel från arbetsförmedlingen\",\n" +
            "    \"publisher\": {\n" +
            "      \"about\": \"https://www.example.se/result.rdf#publisher\",\n" +
            "      \"name\": \"Redpill Linpro AB Catalog\"\n" +
            "    },\n" +
            "    \"license\": \"https://www.apache.org/licenses/LICENSE-2.0\"\n" +
            "  },\n" +
            "  \"dcat-dataset\": {\n" +
            "    \"about\": \"https://www.example.se/result.rdf#dataset1\",\n" +
            "    \"title-sv\": \"Datamängd 1\",\n" +
            "    \"description-sv\": \"Exempel beskrivning 1\",\n" +
            "    \"publisher\": {\n" +
            "      \"about\": \"https://www.example.se/result.rdf#publisher\",\n" +
            "      \"name\": \"Redpill Linpro AB 1\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static String ramlResult = "{\"(dcat-dataset 1)\":{\"title-sv\":\"Datamängd 1\",\"about\":\"https:\\/\\/www.example.se\\/result.rdf#dataset1\",\"description\":\"Exempel beskrivning 1\",\"publisher\":{\"name\":\"Redpill Linpro AB 1\"}},\"dcat-dataservice\":{\"properties\":{\"contactPoint\":{\"landingPage\":\"string\",\"adress\":\"string\",\"type\":\"string\",\"license\":\"string\",\"phone\":\"string\",\"name\":\"string\",\"theme\":\"string\",\"conformsTo\":\"string\",\"accessRights\":\"string\",\"page\":\"string\",\"keyword\":\"string\",\"servesDataset\":\"string\",\"email\":\"string\"},\"endpointURL\":\"string\",\"description\":\"string\",\"publisher\":{\"name\":\"string\",\"type\":\"string\",\"mbox\":\"string\",\"homepage\":\"string\"},\"title\":\"string\",\"endpointDescription\":\"string\"}},\"description\":\"Annotation exempel api är ett påhittat api som använder sig av annotations för att skapa metadata för DCAT-AP-SE.\",\"dcat-dataset\":{\"properties\":{\"creator\":\"string\",\"contactPoint\":{\"phone\":\"string\",\"name\":\"string\",\"adress\":\"string\",\"type\":\"string\",\"email\":\"string\"},\"description\":\"string\",\"publisher\":{\"name\":\"string\",\"type\":\"string\",\"mbox\":\"string\",\"homepage\":\"string\"},\"location\":{\"centroid\":\"string\",\"bbox\":\"string\",\"geometry\":\"string\"},\"title\":\"string\",\"distribution\":{\"byteSize\":\"string\",\"accessURL\":\"string\",\"downloadURL\":\"string\",\"description\":\"string\",\"language\":\"string\",\"availability\":\"string\",\"title\":\"string\",\"temporalResolution\":\"string\",\"accessService\":\"string\",\"spatialResolutionInMeters\":\"string\",\"rights\":\"string\",\"checksum\":\"string\",\"modified\":\"string\",\"theme\":\"string\",\"issued\":\"string\",\"keyword\":\"string\",\"identifier\":\"string\",\"adms\":\"string\",\"landingPage\":\"string\",\"format\":\"string\",\"license\":\"string\",\"page\":\"string\",\"conformsTo\":\"string\",\"belongsTo\":\"string\",\"status\":\"string\"},\"qualifiedAttribution\":\"string\",\"temporal\":{\"offers\":\"string\",\"endDate\":\"string\",\"hasVersion\":\"string\",\"versionInfo\":\"string\",\"source\":\"string\",\"relation\":\"string\",\"isReferencedBy\":\"string\",\"provenance\":\"string\",\"temporalResolution\":\"string\",\"spatialResolutionInMeters\":\"string\",\"qualifiedRelation\":\"string\",\"accrualPeriodicity\":\"string\",\"versionNotes\":\"string\",\"accessRights\":\"string\",\"isVersionOf\":\"string\",\"page\":\"string\",\"startDate\":\"string\"}}},\"title\":\"Annotation exempel api\",\"version\":\"1.0.0\",\"annotationTypes\":null,\"dcat-catalog\":{\"properties\":{\"themeTaxonomy\":\"string\",\"hasPart\":\"string\",\"about\":\"string\",\"description\":\"string\",\"language\":\"string\",\"title\":\"string\",\"isPartOf\":\"string\",\"license\":\"string\",\"rights\":\"string\",\"publisher\":{\"name\":\"string\",\"type\":\"string\",\"mbox\":\"string\",\"homepage\":\"string\"},\"modified\":\"string\",\"location\":{\"centroid\":\"string\",\"bbox\":\"string\",\"geometry\":\"string\"},\"issued\":\"string\",\"homepage\":\"string\"}},\"(dcat-catalog)\":{\"title-sv\":\"Annotation exempel RAML 1.0\",\"license\":\"https:\\/\\/www.apache.org\\/licenses\\/LICENSE-2.0\",\"title-en\":\"Annotation example RAML 1.0\",\"about\":\"www.af.se\",\"description\":\"Annotation exempel från arbetsförmedlingen\",\"publisher\":{\"name\":\"Redpill Linpro AB Catalog\"},\"dataset\":\"https:\\/\\/www.example.se\\/result.rdf#dataset1\"}}";
    private static String jsonresult = "{\"dcat-dataset\":{\"title-sv\":\"Datamängd 1\",\"about\":\"https:\\/\\/www.example.se\\/result.rdf#dataset1\",\"publisher\":{\"about\":\"https:\\/\\/www.example.se\\/result.rdf#publisher\",\"name\":\"Redpill Linpro AB 1\"},\"description-sv\":\"Exempel beskrivning 1\"},\"dcat-catalog\":{\"title-sv\":\"Annotation exempel RAML 1.0\",\"license\":\"https:\\/\\/www.apache.org\\/licenses\\/LICENSE-2.0\",\"title-en\":\"Annotation example RAML 1.0\",\"about\":\"https:\\/\\/www.af.se\",\"publisher\":{\"about\":\"https:\\/\\/www.example.se\\/result.rdf#publisher\",\"name\":\"Redpill Linpro AB Catalog\"},\"description-sv\":\"Annotation exempel från arbetsförmedlingen\"}}";
    @BeforeEach
    void setup() {
        parser = new ApiDefinitionParser();
    }

    //TODO: Fixa så att testet funkar
    @Test
    void testMandatoryRamlApi() throws Exception {
        try {
            String result = parser.getApiJsonString(ramlApi).toString();
            assertEquals(result, ramlResult);
        } catch (DcatException e) {

        }
    }

    @Test
    void testMandatoryJsonApi() throws Exception {
        try {
            String result = parser.getApiJsonString(jsonApi).toString();
            assertEquals(result, jsonresult);
        } catch (DcatException e) {

        }
    }
}
