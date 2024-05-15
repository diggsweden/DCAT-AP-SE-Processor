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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;
import se.ams.dcatprocessor.rdf.DcatException;

import java.io.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Path;

public class ApiDefinitionParser {

    private static Logger logger = LoggerFactory.getLogger(ApiDefinitionParser.class);

    // TODO: Do we need to provide a method with this signature for the sake of not breaking the API? Or will people adapt?
    //
    // public static JSONObject getApiJsonString(String fileString) throws IOException, ParseException
    
    public static JSONObject getApiJsonString(
        String fileString, Path outJsonFile) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Stream<String> lines;
        String apiJsonString = "";

        lines = fileString.lines();
        String apiLine1 = lines.limit(1).collect(Collectors.joining("\n"));

        if (apiLine1.contains("openapi")) {
            apiJsonString = getFileApiYamlRaml(fileString, outJsonFile);
        } else if (apiLine1.contains("RAML")) {
            apiJsonString = getFileApiYamlRaml(fileString, outJsonFile);
        } else if (apiLine1.contains("{")){
            apiJsonString = fileString;
        }
        else {
            throw new DcatException("not supported api definition");
        }
        JSONObject jsonObjectFile = (JSONObject) parser.parse(apiJsonString);
        if (jsonObjectFile.containsKey("info")) {
            jsonObjectFile = (org.json.simple.JSONObject) (jsonObjectFile.get("info"));
            jsonObjectFile = (org.json.simple.JSONObject) (jsonObjectFile.get("x-dcat"));
        }
        return jsonObjectFile;
    }

    private static String getFileApiYamlRaml(String apiSpec, Path outJsonFile) throws IOException {
        FileOutputStream output = new FileOutputStream(outJsonFile.toFile());
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(output, JsonEncoding.UTF8);

        try {
            Yaml yamlParser = new Yaml();
            yamlParser.addImplicitResolver(Tag.YAML, Pattern.compile("^(!)$"), "!");
            Node compose = yamlParser.compose(new StringReader(apiSpec));
            build(compose, generator);
            generator.close();
            output.close();

            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(outJsonFile.toFile());

            //Read JSON file
            Object obj = jsonParser.parse(reader);
            return obj.toString();
            //logger.debug("JSON string from file:\n"+obj.toString());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void build(Node yaml, JsonGenerator generator) throws IOException {
        if (yaml instanceof MappingNode) {
            final MappingNode mappingNode = (MappingNode) yaml;
            generator.writeStartObject();
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    //logger.debug("MappingNode="+((ScalarNode) tuple.getKeyNode()).getValue());
                    generator.writeFieldName(((ScalarNode) tuple.getKeyNode()).getValue());
                }
                build(tuple.getValueNode(), generator);
            }
            generator.writeEndObject();
        } else if (yaml instanceof SequenceNode) {
            generator.writeStartArray();
            for (Node node : ((SequenceNode) yaml).getValue()) {
                //logger.debug("SequenceNode="+node);
                build(node, generator);
            }
            generator.writeEndArray();
        } else if (yaml instanceof ScalarNode) {
            ScalarNode scalarNode = (ScalarNode) yaml;
            String className;

            if (!scalarNode.getTag().startsWith("!")) {
                className = scalarNode.getTag().getClassName();
                if ("null".equals(className)) {
                    generator.writeNull();
                } else {
                    generator.writeString(scalarNode.getValue());
                }
            } else {
                generator.writeString("!" + scalarNode.getValue());
            }
        }
    }
}
