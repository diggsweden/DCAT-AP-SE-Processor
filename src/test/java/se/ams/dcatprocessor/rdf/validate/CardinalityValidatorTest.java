/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatClass;
import se.ams.dcatprocessor.rdf.DcatException;

@QuarkusTest
class CardinalityValidatorTest {

	@Inject
	CardinalityValidator cardinalityValidator;

	@Test
	void testCurrentFilenameNotSet() throws Exception {
		try {
			cardinalityValidator.validate(DcatClass.CATALOG, new ArrayListValuedHashMap<String, String>(), new ArrayList<>());
			fail("Expected exception when current filename is not set in CardinalityValidator");
		} catch (DcatException e) {
			assertEquals(CardinalityValidator.class + " Error validating input data. Reason: Filename for the file being validated is not set", e.getMessage());
		}
		
		
	}

}
