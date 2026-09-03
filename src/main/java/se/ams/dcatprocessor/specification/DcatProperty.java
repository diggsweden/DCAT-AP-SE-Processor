// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import java.util.Collections;
import java.util.List;


/** The fields of a bundle.json node that this application needs. */
public class DcatProperty {

    private String id;
    private String nodetype;
    private String property;
    private String pattern;
    private String extendsId;
    private DcatCardinality cardinality;
    private List<String> datatype = Collections.emptyList();
    private List<DcatProperty> items = Collections.emptyList();
    private List<String> choices = Collections.emptyList();
    
    /** 
     * Unique identifier, e.g. "dcat:dcterms:title_da". 
     * Null for a node written in its parent's items rather than as a template of its own.
    */
    public String getId() {
        return id;
    }

    /**
     * "URI", "RESOURCE", "LITERAL", "ONLY_LITERAL", "LANGUAGE_LITERAL" or
     * "DATATYPE_LITERAL". Null on nodes that hold no value of their own.
     */
    public String getNodetype() {
        return nodetype;
    }

    public String getProperty() {
        return property;
    }

    /** 
     * Null when the node states no cardinality at all. That is not the same as 0..n
     * Some nodes state only what they extend, and null is what makes the
	 * lookup follow the extends and pick up the condition.
    */ 
    public DcatCardinality getCardinality() {
        return cardinality;
    }

    public List<String> getDatatype() {
        return datatype;
    }

    public String getPattern() {
        return pattern;
    }

    /** Get the child nodes */
    public List<DcatProperty> getItems() {
        return items;
    }

    /** Id of a node whose fields this node inherits. */
    public String getExtendsId() {
        return extendsId;
    }

    /** The values the specification offers for this property. Most properties offer none */
    public List<String> getChoices() {
        return choices;
    }

    void setId(String id) {
        this.id = id;
    }

    void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    void setProperty(String property) {
        this.property = property;
    }

    void setCardinality(DcatCardinality cardinality) {
        this.cardinality = cardinality;
    }

    void setDatatype(List<String> datatype) {
        this.datatype = datatype;
    }

    void setPattern(String pattern) {
        this.pattern = pattern;
    }

    void setItems(List<DcatProperty> items) {
        this.items = items;
    }

    void setExtendsId(String extendsId) {
        this.extendsId = extendsId;
    }

    void setChoices(List<String> choices) {
        this.choices = choices;
    }
}