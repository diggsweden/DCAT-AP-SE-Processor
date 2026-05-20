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

package se.ams.dcatprocessor.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UtilTest {

	@Test
	void testNullCheck() {
		assertTrue(Util.isNullOrEmpty(null));
		assertTrue(Util.isNullOrEmpty(""));
		
		assertTrue(Util.isNullOrEmpty(null));
		assertTrue(Util.isNullOrEmpty(new ArrayList<>()));
	}

	@ParameterizedTest
    @ValueSource(strings = {
		"https://example.com",
		"http://example.com",
		"https://example.com/path/to/resource",
		"https://example.com/path?query=value&other=123",
		"ftp://files.example.com/file.txt",
		"https://192.168.1.1/api",
	})
	void testIsURIReturnsTrueForValidURI(String uri){
		boolean result = Util.isURI(uri);
		assertTrue(result);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
    " ",
    "a:b",
    "https://",
    "example.com",
    "/relative/path",
    "//example.com",
    "https://exa mple.com"
	})
	void testIsURIReturnsFalseForInvalidURI(String uri) {
		boolean result = Util.isURI(uri);
	    assertFalse(result);
	}
}
