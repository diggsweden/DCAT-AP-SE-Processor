// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.specification.DcatCardinality;
import se.ams.dcatprocessor.specification.DcatCardinality.Condition;

class CardinalityHandlerTest {

	private static Map<String, DcatCardinality> dataset;
	private static Map<String, DcatCardinality> distribution;

	@BeforeAll
	static void load() {
		CardinalityHandler.resetInstance();
		dataset = CardinalityHandler.getInstance().getCardinalities(DcatClass.DATASET);
		distribution = CardinalityHandler.getInstance().getCardinalities(DcatClass.DISTRIBUTION);
	}

	@Test
	void testThatMandatoryPropertiesAreLoaded() {
		assertEquals(1, dataset.get("dcterms:title").getMin());
		assertEquals(1, dataset.get("dcterms:publisher").getMin());
		assertEquals(1, distribution.get("dcat:accessURL").getMin());
	}

	@Test
	void testThatOptionalPropertiesAreLoaded() {
		assertEquals(0, dataset.get("dcat:keyword").getMin());
		assertEquals(DcatCardinality.MAX, dataset.get("dcat:keyword").getMax());
		assertEquals(1, dataset.get("dcterms:issued").getMax());
	}

	@Test
	void testThatCardinalityIsReadThroughExtends() {
        DcatCardinality hvdCategory = dataset.get("dcatap:hvdCategory");
		// The three hvdCategory nodes state only what they extend
		assertNotNull(hvdCategory);
		assertEquals(1, hvdCategory.getMin());
	}

	@Test
	void testThatHvdCategoryKeepsItsCondition() {
        DcatCardinality hvdCategory = dataset.get("dcatap:hvdCategory");
		Condition condition = hvdCategory.getCondition();

		assertNotNull(condition, "hvdCategory must keep the condition from the node it extends");
		assertEquals("dcatap:applicableLegislation", condition.property());
		assertEquals("http://data.europa.eu/eli/reg_impl/2023/138/oj", condition.value());
	}

	@Test
	void testThatPropertiesWithoutAConditionHaveNone() {
		assertNull(dataset.get("dcterms:title").getCondition());
	}
}