// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import java.util.HashMap;
import java.util.Map;

import se.ams.dcatprocessor.specification.DcatCardinality;
import se.ams.dcatprocessor.specification.DcatProperty;
import se.ams.dcatprocessor.specification.DcatSpecification;

/**
 * Holds the cardinality of every property, per DCAT class.
 *
 * The cardinality sits on the nodes in the specification, but keyed by node id
 * and spread across groups and extended nodes. This builds the lookup validation
 * needs, keyed by property, once at startup.
 * @author nacbr
 */
public class CardinalityHandler {

	private static CardinalityHandler instance;

	private final DcatSpecification specification;
	
	private Map<DcatClass, Map<String, DcatCardinality>> cardinalities = new HashMap<>();
	
	private CardinalityHandler(DcatSpecification specification) {
		this.specification = specification;
		loadCardinalities();
	}
	
	public static CardinalityHandler getInstance() {
		if(instance == null) {
			instance = new CardinalityHandler(new DcatSpecification());
		}
		return instance;
	}

	public static void resetInstance(){
		instance = null;
	} 
		
	private void loadCardinalities() {
		for (DcatClass dcatClass : DcatClass.values()) {
			DcatProperty classNode = specification.node(dcatClass.getTemplateId());

			// A class the loaded bundle does not have is not supported and is excluded.
			if (classNode == null) {
				continue;
			}

			Map<String, DcatCardinality> properties = new HashMap<>();
			for (DcatProperty node : classNode.getItems()) {
				addCardinality(node, properties);
			}
			cardinalities.put(dcatClass, properties);
		}
	}

	private void addCardinality(DcatProperty node, Map<String, DcatCardinality> properties) {
		DcatCardinality cardinality = cardinalityOf(node);

		// The common case: the node is one property and carries it directly.
		if (node.getProperty() != null) {
			properties.put(node.getProperty(), cardinality);
			return;
		}

		// The node states only what it extends, so the property sits on the extended node.
		if (node.getExtendsId() != null) {
			properties.put(specification.node(node.getExtendsId()).getProperty(), cardinality);
			return;
		}

		// The node is a group, get the cardinality for the group items recursive.
		for (DcatProperty alternative : node.getItems()) {
			addCardinality(alternative, properties);
		}
	}

	private DcatCardinality cardinalityOf(DcatProperty node) {
		DcatCardinality cardinality = node.getCardinality();

		// Some nodes state only what they extend and take the whole cardinality, condition included, from the node they extend.
		if (cardinality == null && node.getExtendsId() != null) {
			cardinality = specification.node(node.getExtendsId()).getCardinality();
		}

		// A node without a cardinality is optional and unbound (0..n).
		if (cardinality == null) {
			return new DcatCardinality(0, DcatCardinality.MAX, null);
		}
		return cardinality;
	}

	/**
	 * Returns all the loaded cardinalities for a primary class
	 * @param dcatClass - The primary class
	 * @return - The Map with all the cardinalities
	 */
	public Map<String, DcatCardinality> getCardinalities(DcatClass dcatClass) {
		if(dcatClass == null) {
			throw new IllegalArgumentException("DCATClass is null");
		}
		Map<String, DcatCardinality> properties = cardinalities.get(dcatClass);

		if (properties == null) {
			throw new DcatException(dcatClass + " is not part of the loaded specification version");
		}
		return properties;
	}
}
