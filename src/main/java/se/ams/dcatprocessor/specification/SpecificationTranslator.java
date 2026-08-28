// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import com.fasterxml.jackson.databind.JsonNode;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.specification.DcatCardinality.Condition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the parsed bundle.json into {@link DcatProperty} objects.
 */
public class SpecificationTranslator {

    /** Every field that occurs in the bundle. */
    private static final List<String> KNOWN_FIELDS = List.of(
            "id", "type", "nodetype", "property", "label", "description", "descriptions",
            "cardinality", "choices", "datatype", "pattern", "constraints", "items", "extends",
            "styles", "cls", "deps", "valueTemplate", "value", "labelProperties", "automatic",
            "content");
    
    /** The bundle writes a handful of properties as full IRIs instead of prefixed names. This needs to be bridged */
    private static final Map<String, String> PREFIX_BY_NAMESPACE = Map.of(
            "http://www.w3.org/ns/adms#", "adms",
            "http://spdx.org/rdf/terms#", "spdx",
            "http://data.europa.eu/r5r/", "dcatap",
            "http://www.w3.org/ns/org#", "org",
            "https://www.w3.org/ns/org#", "org");
    
 
    /** Translates the bundle and returns nodes keyed by id. */
    public Map<String, DcatProperty> translate(JsonNode root) {
        JsonNode templates = root.get("templates");
        if (templates == null || !templates.isArray() || templates.isEmpty()) {
            throw new DcatException("Error reading DCAT-AP-SE specification. Bundle file has no templates");
        }

        Map<String, JsonNode> jsonNodes = new LinkedHashMap<>();
        Map<String, DcatProperty> properties = new LinkedHashMap<>();

        // Every node is created first
        for (JsonNode template : templates) {
            String id = text(template, "id");
            if (id == null) {
                throw new DcatException("Error reading DCAT-AP-SE specification. Bundle file has a top level template without an id");
            }
            if (jsonNodes.put(id, template) != null) {
                throw new DcatException("Error reading DCAT-AP-SE specification. Bundle file has two templates with id: " + id);
            }
            DcatProperty property = toDcatProperty(template);
            properties.put(id, property);
        }

        // The items for the nodes are wired up after all nodes are created.
        for (Map.Entry<String, JsonNode> entry : jsonNodes.entrySet()) {
            String id = entry.getKey();
            JsonNode raw = entry.getValue();

            List<DcatProperty> items = resolveItems(raw, properties);
            properties.get(id).setItems(items);
        }
        return properties;
    }

    private DcatProperty toDcatProperty(JsonNode raw) {
        checkKnownFields(raw);

        DcatProperty property = new DcatProperty();
        property.setId(text(raw, "id"));
        property.setNodetype(text(raw, "nodetype"));
        property.setProperty(toPrefixedName(text(raw, "property")));
        property.setCardinality(toCardinality(raw));
        property.setDatatype(readStringList(raw.get("datatype")));
        property.setPattern(text(raw, "pattern"));
        property.setExtendsId(text(raw, "extends"));
        property.setChoices(readChoices(raw.get("choices")));
        
        return property;
    }

    private List<DcatProperty> resolveItems(JsonNode raw, Map<String, DcatProperty> properties) {
        JsonNode items = raw.get("items");
        if (items == null || !items.isArray()) {
            return List.of();
        }
        List<DcatProperty> childNodes = new ArrayList<>();
        for (JsonNode item : items) {
            if (item.isTextual()) {
                DcatProperty referenced = properties.get(item.asText());
                if (referenced == null) {
                    throw new DcatException("Bundle node " + text(raw, "id") + " references unknown id: " + item.asText());
                }
                childNodes.add(referenced);
            } else {
                DcatProperty inline = toDcatProperty(item);
                inline.setItems(resolveItems(item, properties));
                childNodes.add(inline);
            }
        }
        return List.copyOf(childNodes);
    }

    /**
     * Fails on a field the model does not know about, so that a new field in a
     * future specification version stops the build instead of being dropped.
     */
    private void checkKnownFields(JsonNode raw) {
        for (Map.Entry<String, JsonNode> field : raw.properties()) {
            String name = field.getKey();
            if (!KNOWN_FIELDS.contains(name)) {
                throw new DcatException("Unknown field '" + name + "' in bundle node "
                        + text(raw, "id") + ". The specification has changed; add the field to "
                        + "DcatProperty and to KNOWN_FIELDS.");
            }
        }
    }

	/**
	 * The bundle leaves out min when the property is optional and max when it is
	 * unbounded. Both are filled in here so that a cardinality is always complete.
	 *
	 * The condition sits on the node rather than inside the cardinality object.
	 */
    private DcatCardinality toCardinality(JsonNode raw) {
        JsonNode cardinality = raw.get("cardinality");
        if (cardinality == null || !cardinality.isObject()) {
        	return null;
        }

        Integer jsonMin = integer(cardinality, "min");
        Integer jsonMax = integer(cardinality, "max");

        Integer min = jsonMin == null ? 0 : jsonMin;
        Integer max = jsonMax == null ? DcatCardinality.MAX : jsonMax;

        return new DcatCardinality(min, max, toCondition(raw));
	}

    /**
	 * Reads the condition that makes a minimum apply only in some cases. The
	 * bundle states it as a two element array (deps[]) of a property and the value that property must have.
	 */
	private Condition toCondition(JsonNode raw) {
        List<String> deps = readStringList(raw.get("deps"));
        if (deps.size() != 2) {
        	return null;
        }
        return new Condition(toPrefixedName(deps.get(0)), deps.get(1));
	}

    /** Reads a bare string as a one element list, matching how the bundle writes it. */
    private List<String> readStringList(JsonNode raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw.isTextual()) {
            return List.of(raw.asText());
        }
        if (!raw.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode element : raw) {
            values.add(element.asText());
        }
        return List.copyOf(values);
    }

    /**
     * Replaces a namespace with its prefix: "http://www.w3.org/ns/adms#status"
     * becomes "adms:status". The bundle writes most properties prefixed already
     * but a handful expanded, and callers should only see one shape.
     *
     * A property that is already prefixed, or whose namespace is not in the
     * table, is returned unchanged.
     */
    private String toPrefixedName(String property) {
        if (property == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : PREFIX_BY_NAMESPACE.entrySet()) {
            if (property.startsWith(entry.getKey())) {
                return entry.getValue() + ":" + property.substring(entry.getKey().length());
            }
        }
        return property;
    }

    /** The bundle writes each choice as an object. Only its value is read. */
    private List<String> readChoices(JsonNode raw) {
        if (raw == null || !raw.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode choice : raw) {
            JsonNode value = choice.get("value");
            if (value != null) {
                values.add(value.asText());
            }
        }
        return List.copyOf(values);
    }

    private String text(JsonNode raw, String field) {
        JsonNode value = raw.get(field);

        if(value == null || value.isNull()){
            return null;
        }
        return value.asText();
    }

    private Integer integer(JsonNode raw, String field) {
        JsonNode value = raw.get(field);

        if(value == null || value.isNull()){
            return null;
        }
        return value.asInt();
    }
}