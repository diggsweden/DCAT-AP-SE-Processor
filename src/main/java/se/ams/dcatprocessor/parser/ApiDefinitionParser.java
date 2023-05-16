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

public class ApiDefinitionParser {

    private static Logger logger = LoggerFactory.getLogger(ApiDefinitionParser.class);

    public enum ApiSpecSyntax {
        Json,
        YamlRaml,
        Unknown
    };

    public static JSONObject getApiJsonString(String fileString) throws IOException, ParseException {
        JSONObject fullObj = parseToJsonObject(fileString);
        JSONObject inner = getIn(fullObj, new String[]{"info", "x-dcat"});
        return inner == null? fullObj : inner;
    }

    private static JSONObject getIn(JSONObject obj, String[] path) {
        for (String k: path) {
            obj = (JSONObject)obj.get(k);
            if (obj == null) {
                return null;
            }
        }
        return obj;
    }
    
    public static ApiSpecSyntax guessSyntax(String fileString) {
        String trimmedString = fileString.trim();
        if (trimmedString.startsWith("{") && trimmedString.endsWith("}")) {
            return ApiSpecSyntax.Json;
        }
        
        Stream<String> lines;
        lines = fileString.lines();
        String apiLine1 = lines.limit(1).collect(Collectors.joining("\n"));

        if (apiLine1.contains("RAML") || apiLine1.contains("openapi")) {
            return ApiSpecSyntax.YamlRaml;
        }

        return ApiSpecSyntax.Unknown;
    }
    
    private static JSONObject parseToJsonObject(String fileString) throws IOException, ParseException {
        switch (guessSyntax(fileString)) {
        case Json: return (JSONObject)((new JSONParser()).parse(fileString));
        case YamlRaml: return convertYamlRamlToJsonObject(fileString);
        case Unknown: {throw new DcatException("not supported api definition");}
        }
        return null;
    }

    private static JSONObject convertYamlRamlToJsonObject(String apiSpec) throws IOException, ParseException {
        FileOutputStream output = new FileOutputStream("output.json"); // TODO: Could we use a ByteArrayOutputStream for this, instead?
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(output, JsonEncoding.UTF8);

        Yaml yamlParser = new Yaml();
        yamlParser.addImplicitResolver(Tag.YAML, Pattern.compile("^(!)$"), "!");
        Node compose = yamlParser.compose(new StringReader(apiSpec));
        build(compose, generator);
        generator.close();
        output.close();

        JSONParser jsonParser = new JSONParser();
        FileReader reader = new FileReader("output.json");
        return (JSONObject)jsonParser.parse(reader);
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
