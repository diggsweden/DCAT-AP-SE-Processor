// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.ams.dcatprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

class ApplicationTest {

    @ParameterizedTest
    @ValueSource(strings = {"-f", "-d", "-k", "./testFiles"})
    void testThatNonOptionArgumentGivesCliMode(String arg) {
        assertTrue(Application.isCliArgs(new String[] {arg}));
    }

    @Test
    void testThatCliArgsReturnsFalseIfNoArgs() {
        boolean result = Application.isCliArgs(new String[]{});
        assertFalse(result);
    }
    
    @Test
    void testThatNoArgumentsIsNotCliMode() {
        String[] args = new String[0];
        assertFalse(Application.isCliArgs(args));
    }

    @Test
    void testThatSpringOptionIsNotCliMode() {
        String[] args = new String[] {"--server.port=8081"};
        assertFalse(Application.isCliArgs(args));
    }


    @Test
    void testThatNoArgumentsKeepsWebserver() {
        String[] args = new String[0];
        SpringApplication app = Application.createApplication(args);

        assertEquals(WebApplicationType.SERVLET, app.getWebApplicationType());
    }
}
