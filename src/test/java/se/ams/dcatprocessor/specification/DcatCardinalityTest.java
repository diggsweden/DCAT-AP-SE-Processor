// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DcatCardinalityTest {

	@Test
	void testThatOptionalWithPrefIsRecommended() {
		DcatCardinality cardinality = new DcatCardinality(0, DcatCardinality.MAX, 1, null);

		assertTrue(cardinality.isRecommended());
	}

	@Test
	void testThatMandatoryWithPrefIsNotRecommended() {
		DcatCardinality cardinality = new DcatCardinality(1, DcatCardinality.MAX, 1, null);

		assertFalse(cardinality.isRecommended());
	}

	@Test
	void testThatOptionalWithoutPrefIsNotRecommended() {
		DcatCardinality cardinality = new DcatCardinality(0, DcatCardinality.MAX, 0, null);

		assertFalse(cardinality.isRecommended());
	}
}