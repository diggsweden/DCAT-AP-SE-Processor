// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.parser;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;
import se.ams.dcatprocessor.rdf.DcatException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class ApiDefinitionParser {

    public static JSONObject getApiJsonString(String fileString) throws IOException, JSONException  {
        String apiJsonString;
        String apiLine1 = fileString.lines().findFirst().orElse("");

        if (apiLine1.contains("openapi") || apiLine1.contains("RAML")) {
            apiJsonString = getFileApiYamlRaml(fileString);
        } else if (apiLine1.contains("{")){
            apiJsonString = fileString;
        }
        else {
            throw new DcatException("not supported api definition");
        }

        JSONObject jsonObjectFile;
        try {
            jsonObjectFile = new JSONObject(apiJsonString);
            
            if (jsonObjectFile.has("info")) {
                jsonObjectFile = jsonObjectFile.getJSONObject("info");
                jsonObjectFile = jsonObjectFile.getJSONObject("x-dcat");
            }
        } catch (JSONException e) {
            throw new DcatException("Failed to parse JSON: " + e.getMessage());
        }
        return jsonObjectFile;
    }

    private static String getFileApiYamlRaml(String apiSpec) throws IOException {
        JsonFactory factory = new JsonFactory();

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonGenerator generator = factory.createGenerator(output, JsonEncoding.UTF8)) {

            Yaml yamlParser = new Yaml();
            yamlParser.addImplicitResolver(Tag.YAML, Pattern.compile("^(!)$"), "!");
            Node compose = yamlParser.compose(new StringReader(apiSpec));
            
            build(compose, generator);
            generator.flush();
            
            String jsonString = output.toString(StandardCharsets.UTF_8);
            return new JSONObject(jsonString).toString();
            
        } catch (JSONException e) {
            throw new IOException("Failed to parse YAML/RAML as JSON: " + e.getMessage(), e);
        }
    }

    private static void build(Node yaml, JsonGenerator generator) throws IOException {
        if (yaml instanceof MappingNode) {
            final MappingNode mappingNode = (MappingNode) yaml;
            generator.writeStartObject();
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    generator.writeFieldName(((ScalarNode) tuple.getKeyNode()).getValue());
                }
                build(tuple.getValueNode(), generator);
            }
            generator.writeEndObject();
        } else if (yaml instanceof SequenceNode) {
            generator.writeStartArray();
            for (Node node : ((SequenceNode) yaml).getValue()) {
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