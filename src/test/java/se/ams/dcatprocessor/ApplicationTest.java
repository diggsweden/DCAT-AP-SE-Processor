// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.ams.dcatprocessor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.ams.dcatprocessor.cli.CliFlags;


public class ApplicationTest {
    
    @ParameterizedTest
    @MethodSource("supportedFlagsProvider")
    void testIsCliArgsReturnsTrueIfValidFlag(String flag) {
        boolean result = Application.isCliArgs(new String[]{flag});
        assertTrue(result);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"-x","-invalid", ""})
    void testIsCliArgsReturnsFalseIfUnknownOrEmptyFlag(String flag) {
        boolean result = Application.isCliArgs(new String[]{flag});
        assertFalse(result);
    }

    @Test
    void testIsCliArgsReturnsFalseIfNoArgs() {
        boolean result = Application.isCliArgs(new String[]{});
        assertFalse(result);
    }

    static Stream<String> supportedFlagsProvider() {
        return CliFlags.SUPPORTED_FLAGS.stream(); // returns all supported flags.
    }
}
