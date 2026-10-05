// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

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
@Component
public class CardinalityHandler {

	private final DcatSpecification specification;
	
	private final Map<DcatClass, Map<String, DcatCardinality>> cardinalities = new HashMap<>();

	public CardinalityHandler(DcatSpecification specification) {
		this.specification = specification;
		loadCardinalities();
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
		DcatCardinality cardinality = specification.cardinalityOf(node);
		String property = specification.propertyOf(node);

		// The node is one property, either stated directly or taken from the node it extends.
		if (property != null) {
			properties.put(property, cardinality);
			return;
		}

		// The node is a group, get the cardinality for the group items recursive.
		for (DcatProperty alternative : node.getItems()) {
			addCardinality(alternative, properties);
		}
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
