// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import se.ams.dcatprocessor.cli.CliFlags;

import java.util.Arrays;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);

        if (isCliArgs(args)) {
            // CLI mode without webserver 
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }

    static boolean isCliArgs(String[] args) {
        return Arrays.stream(args).anyMatch(CliFlags.SUPPORTED_FLAGS::contains);
    }
}
