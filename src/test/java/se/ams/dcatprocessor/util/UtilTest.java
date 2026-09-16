// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.util;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	@ParameterizedTest
    @ValueSource(strings = {
		"file.json",
		"file.yaml",
		"file.raml",
		"file.yml",
	})
	void testThatValidFileExtensionsIsValidated(String name){
		assertTrue(Util.validateFileExtension(name));
	}

	@ParameterizedTest
	@NullAndEmptySource
    @ValueSource(strings = {
		"file.pdf",
		"file.txt",
		"file.csv",
		"file.jso",
	})
	void testThatInvalidFileExtensionsFail(String name){
		assertFalse(Util.validateFileExtension(name));
	}
}
