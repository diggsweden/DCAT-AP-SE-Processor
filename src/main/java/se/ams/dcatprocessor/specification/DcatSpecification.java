// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * The DCAT-AP-SE specification, in memory, answering questions about it.
 */
@Component
public class DcatSpecification {

    private final Map<String, DcatProperty> nodes;
    private final Map<String, List<DcatProperty>> nodesByProperty;

    public DcatSpecification(SpecificationLoader specificationLoader) {
        this.nodes = specificationLoader.load();
        this.nodesByProperty = indexByProperty(nodes);
    }

    public DcatProperty node(String id) {
        return nodes.get(id);
    }

    public Set<String> properties() {
        return nodesByProperty.keySet();
    }

    public boolean containsKey(String property) {
        return nodesByProperty.containsKey(property);
    }

    public List<DcatProperty> nodesFor(String property) {
        return nodesByProperty.getOrDefault(property, List.of());
    }

    public List<String> datatypesFor(String property) {
        for (DcatProperty node : nodesFor(property)) {
            if (!node.getDatatype().isEmpty()) {
                return node.getDatatype();
            }
        }
        return List.of();
    }

    public String patternFor(String property) {
        for (DcatProperty node : nodesFor(property)) {
            if (node.getPattern() != null) {
                return node.getPattern();
            }
        }
        return null;
    }

    private static Map<String, List<DcatProperty>> indexByProperty(Map<String, DcatProperty> nodes) {
        Map<String, List<DcatProperty>> byProperty = new LinkedHashMap<>();
        for (DcatProperty node : nodes.values()) {
            addNode(node, byProperty);
            for (DcatProperty item : node.getItems()) {
                addNode(item, byProperty);
            }
        }
        return Map.copyOf(byProperty);
    }

    private static void addNode(DcatProperty node, Map<String, List<DcatProperty>> byProperty) {
        String property = node.getProperty();
        if (property != null) {
            byProperty.computeIfAbsent(property, key -> new ArrayList<>()).add(node);
        }
    }

    /**
     * The cardinality of a node. Some nodes state only what they extend and take
     * the whole cardinality, condition included, from the node they extend.
     * A node without a cardinality is optional, unbound and not recommended.
     */
    public DcatCardinality cardinalityOf(DcatProperty node) {
        DcatCardinality cardinality = node.getCardinality();

        if (cardinality == null && node.getExtendsId() != null) {
            cardinality = node(node.getExtendsId()).getCardinality();
        }

        if (cardinality == null) {
            return new DcatCardinality(0, DcatCardinality.MAX, 0, null);
        }
        return cardinality;
    }

    /**
     * The property of a node. Some nodes state only what they extend and take
     * the property from the node they extend. A group has no property of its
     * own; its properties sit on its items, so null is returned for a group.
     */
    public String propertyOf(DcatProperty node) {
        if (node.getProperty() != null) {
            return node.getProperty();
        }
        if (node.getExtendsId() != null) {
            return node(node.getExtendsId()).getProperty();
        }
        return null;
    }

    /** The values the specification allows for a property, empty when it states none. */
    public List<String> choicesFor(String property) {
        for (DcatProperty node : nodesFor(property)) {
            if (!node.getChoices().isEmpty()) {
                return node.getChoices();
            }
        }
        return List.of();
    }
}