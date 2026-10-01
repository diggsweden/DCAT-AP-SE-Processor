// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor;

import org.springframework.boot.Banner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        createApplication(args).run(args);
    }

    static SpringApplication createApplication(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);

        if (isCliArgs(args)) {
            // CLI mode without webserver
            app.setWebApplicationType(WebApplicationType.NONE);
            app.setBannerMode(Banner.Mode.OFF);
        }
        return app;
    }

    /** CLI mode if any argument is not a Spring option (--name=value). */
    static boolean isCliArgs(String[] args) {
        return !new DefaultApplicationArguments(args).getNonOptionArgs().isEmpty();
    }
}
