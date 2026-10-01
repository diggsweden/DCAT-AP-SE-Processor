// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.cli;

import org.springframework.boot.ExitCodeGenerator;

/** Process exit codes returned when the application runs in CLI mode. */
public enum ExitCode implements ExitCodeGenerator {

    /** RDF generated successfully. */
    SUCCESS(0),

    /** Unexpected internal error. */
    UNEXPECTED_ERROR(1),

    /** Invalid CLI arguments, e.g. unknown flag or missing value. */
    USAGE_ERROR(2),

    /** Input was processed but RDF could not be generated. */
    GENERATION_FAILED(3);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    @Override
    public int getExitCode() {
        return code;
    }
}