// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatClass;
import se.ams.dcatprocessor.rdf.DcatException;

class CardinalityValidatorTest {

	/**
	 * Set the instance of PropertyLoader to null
	 * to force them to re-instansiate since they are Singletons
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	@BeforeEach
	void setProperties() throws IOException, NoSuchFieldException, IllegalAccessException{
		Field instance = CardinalityValidator.class.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(CardinalityValidator.class, null);
	}

	@Test
	void testCurrentFilenameNotSet() throws Exception {
		try {
			CardinalityValidator.getInstance().validate(DcatClass.CATALOG, new ArrayListValuedHashMap<String, String>(), new ArrayList<>());
			fail("Expected exception when current filename is not set in CardinalityValidator");
		} catch (DcatException e) {
			assertEquals(CardinalityValidator.class + " Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}
		
		
	}

}
