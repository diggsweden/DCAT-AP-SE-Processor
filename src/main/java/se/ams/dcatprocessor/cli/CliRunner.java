// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.cli;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.processor.DcatResult;
import se.ams.dcatprocessor.processor.Manager;
import se.ams.dcatprocessor.util.Util;

@Component
public class CliRunner implements ApplicationRunner {

    private final ObjectProvider<Manager> managerProvider;
    private final ApplicationContext context;
    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);

    public CliRunner(ObjectProvider<Manager> managerProvider, ApplicationContext context) {
        this.managerProvider = managerProvider;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        // No CLI args: run application with webserver
        if (args.getNonOptionArgs().isEmpty()) {
            return;
        }

        // CLI args provided: run command and exit
        ExitCode exitCode = execute(args);
        exitSpringApplication(exitCode);
    }

    /**
     * Runs the CLI command and returns its exit code.
     *
     * @param args the application arguments
     * @return the exit code describing the outcome
     */
    protected ExitCode execute(ApplicationArguments args) {
        List<String> nonOptionArgs = args.getNonOptionArgs();

        String flag = findSupportedFlag(nonOptionArgs);
        if (flag == null) {
            System.err.println("Error: unknown CLI arguments " + nonOptionArgs);
            return ExitCode.USAGE_ERROR;
        }

        String flagValue = valueAfter(flag, nonOptionArgs);
        if (flagValue == null) {
            System.err.println("Error: missing value for flag " + flag);
            return ExitCode.USAGE_ERROR;
        }

        return switch (flag) {
            case "-f" -> createDcatFromFile(flagValue);
            case "-d" -> createDcatFromDirectory(flagValue);
            default -> ExitCode.USAGE_ERROR;
        };
    }

    private static String findSupportedFlag(List<String> args) {
        return args.stream()
                .filter(CliFlags.SUPPORTED_FLAGS::contains)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the argument directly after the given flag, or {@code null} if the
     * flag is the last argument.
     */
    private static String valueAfter(String flag, List<String> args) {
        int valueIndex = args.indexOf(flag) + 1;
        return valueIndex < args.size() ? args.get(valueIndex) : null;
    }

    private ExitCode createDcatFromFile(String filename) {
        try {
            Manager manager = managerProvider.getObject();
            DcatResult dcatResult = manager.createDcatFromFile(filename);
            return handleDcatResult(dcatResult);
        } catch (RuntimeException e) {
            logger.error("Unexpected error", e);
            return ExitCode.UNEXPECTED_ERROR;
        }
    }

    private ExitCode createDcatFromDirectory(String dirname) {
        try {
            Manager manager = managerProvider.getObject();
            DcatResult dcatResult = manager.createDcatFromDirectory(dirname);
            return handleDcatResult(dcatResult);
        } catch (RuntimeException e) {
            logger.error("Unexpected error", e);
            return ExitCode.UNEXPECTED_ERROR;
        }
    }

    private ExitCode handleDcatResult(DcatResult dcatResult) {
        if (dcatResult.hasErrors()) {
            System.err.println(dcatResult.errorReport());
            return ExitCode.GENERATION_FAILED;
        } else {
            System.out.println(dcatResult.rdf());
            Util.printToFile(dcatResult.rdf(), "dcat.rdf");
            return ExitCode.SUCCESS;
        }
    }

    private void exitSpringApplication(ExitCode code) {
        int exitCode = SpringApplication.exit(context, code);
        System.exit(exitCode);
    }
}
