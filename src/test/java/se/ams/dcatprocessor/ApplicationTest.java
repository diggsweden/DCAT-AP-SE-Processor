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
package se.ams.dcatprocessor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.ams.dcatprocessor.util.CliFlags;


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
