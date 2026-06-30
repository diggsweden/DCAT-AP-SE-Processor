// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatClass;
import se.ams.dcatprocessor.rdf.DcatException;

class CardinalityValidatorTest {

	@Test
	void testThatValidateFailsWhenFilenameNotSet() throws Exception {
		CardinalityValidator.getInstance().setCurrentFileName(null);

		try {
			CardinalityValidator.getInstance().validate(DcatClass.CATALOG, new ArrayListValuedHashMap<String, String>(), new ArrayList<>());
			fail("Expected exception when current filename is not set in CardinalityValidator");
		} catch (DcatException e) {
			assertEquals(CardinalityValidator.class + " Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}
	}
}