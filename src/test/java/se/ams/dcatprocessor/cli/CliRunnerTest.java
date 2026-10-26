// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

import se.ams.dcatprocessor.processor.DcatResult;
import se.ams.dcatprocessor.processor.Manager;
import se.ams.dcatprocessor.rdf.quality.QualityReport;
import se.ams.dcatprocessor.util.Util;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MockitoExtension.class)
public class CliRunnerTest {

    @Mock
    private Manager manager;

    @Mock
    private ObjectProvider<Manager> managerProvider;

    @Mock
    private ApplicationContext context; // Required by CliRunner's constructor

    private CliRunner cliRunner;

    @BeforeEach
    void setup() {
        cliRunner = new CliRunner(managerProvider, context, JsonMapper.builder().build());
    }

    @Test
    void testThatCreateDcatFromFileIsCalled() {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-f";
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments(flag, file);
        QualityReport qualityReport = new QualityReport(true, List.of());
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.success("<rdf:RDF/>", qualityReport));

        ExitCode result;
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            result = cliRunner.execute(args);
            utilMock.verify(() -> Util.printToFile("<rdf:RDF/>", "dcat.rdf"));
        }

        verify(manager).createDcatFromFile(file);
        assertEquals(ExitCode.SUCCESS, result);
    }

    @Test
    void testThatResultWithErrorsHasNoInteractionWithPrintFile() {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-f";
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments(flag, file);
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.errors("error creating RDF"));

        // DcatResult with errors should NOT result in a printed file
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            ExitCode result = cliRunner.execute(args);
            utilMock.verifyNoInteractions();
            assertEquals(ExitCode.GENERATION_FAILED, result);
        }
    }

    @Test
    void testThatCreateDcatFromDirectoryIsCalled() {
        String flag = "-d";
        String dirname = "./testFiles";
        ApplicationArguments args = new DefaultApplicationArguments(flag, dirname);
        QualityReport qualityReport = new QualityReport(true, List.of());

        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromDirectory(dirname)).thenReturn(DcatResult.success("<rdf:RDF/>", qualityReport));

        ExitCode result;
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            result = cliRunner.execute(args);
            utilMock.verify(() -> Util.printToFile("<rdf:RDF/>", "dcat.rdf"));
        }

        verify(manager).createDcatFromDirectory(dirname);
        assertEquals(ExitCode.SUCCESS, result);
    }

    @Test
    void testThatUnexpectedExceptionGivesUnexpectedError() {
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments("-f", file);
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromFile(file)).thenThrow(new RuntimeException("error"));

        assertEquals(ExitCode.UNEXPECTED_ERROR, cliRunner.execute(args));
    }

    @Test
    void testThatInvalidFlagHasNoInteractionsWithService() {
        String flag = "-unknown";
        ApplicationArguments args = new DefaultApplicationArguments(flag);

        ExitCode result = cliRunner.execute(args);

        verifyNoInteractions(managerProvider, manager);
        assertEquals(ExitCode.USAGE_ERROR, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-f", "-d" })
    void testThatMissingFlagValueHasNoInteractionsWithService(String flag) {
        ApplicationArguments args = new DefaultApplicationArguments(flag);

        ExitCode result = cliRunner.execute(args);

        verifyNoInteractions(managerProvider, manager);
        assertEquals(ExitCode.USAGE_ERROR, result);
    }

    @Test
    void testThatFileWriteErrorGivesUnexpectedError() {
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments("-f", file);
        QualityReport qualityReport = new QualityReport(true, List.of());
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.success("<rdf:RDF/>", qualityReport));
    
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.printToFile("<rdf:RDF/>", "dcat.rdf"))
                    .thenThrow(new RuntimeException("write failed"));
        
            assertEquals(ExitCode.UNEXPECTED_ERROR, cliRunner.execute(args));
        }
    }

    @Test
    void testThatExceptionFromFileGenerationGivesUnexpectedError() {
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments("-f", file);
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromFile(file)).thenThrow(new RuntimeException("error"));
    
        ExitCode result = cliRunner.execute(args);
    
        assertEquals(ExitCode.UNEXPECTED_ERROR, result);
    }
    
    @Test
    void testThatExceptionFromDirectoryGenerationGivesUnexpectedError() {
        String dirname = "./testFiles";
        ApplicationArguments args = new DefaultApplicationArguments("-d", dirname);
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromDirectory(dirname)).thenThrow(new RuntimeException("error"));
    
        ExitCode result = cliRunner.execute(args);
    
        assertEquals(ExitCode.UNEXPECTED_ERROR, result);
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentCombinations")
    void testThatInvalidArgumentCombinationsReturnUsageError(String[] arguments) {
        ApplicationArguments args = new DefaultApplicationArguments(arguments);

        ExitCode result = cliRunner.execute(args);

        assertEquals(ExitCode.USAGE_ERROR, result);
        verifyNoInteractions(managerProvider, manager);
    }

    private static Stream<Arguments> invalidArgumentCombinations() {
        return Stream.of(
                Arguments.of((Object) new String[] { "-k", "-d", "./testFiles" }),
                Arguments.of((Object) new String[] { "-f", "-d" }),
                Arguments.of((Object) new String[] { "-d", "-f" }),
                Arguments.of((Object) new String[] { "-f", "./file.yaml", "-d", "./testFiles" }),
                Arguments.of((Object) new String[] { "-d", "./testFiles", "-f", "./file.yaml" }),
                Arguments.of((Object) new String[] { "-f", "./file.yaml", "extra" }),
                Arguments.of((Object) new String[] { "-f", "" }),
                Arguments.of((Object) new String[] { "-h", "-d" }),
                Arguments.of((Object) new String[] { "-d", "-h" }),
                Arguments.of((Object) new String[] { "-help", "-f", "./file.yaml" }));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-h", "-help" })
    void testThatHelpFlagPrintsHelpAndReturnsSuccess(String flag) {
        ApplicationArguments args = new DefaultApplicationArguments(flag);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

            ExitCode result = cliRunner.execute(args);

            assertEquals(ExitCode.SUCCESS, result);
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Usage:"));
            verifyNoInteractions(managerProvider, manager);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testThatSpringBootOptionsAreAllowedWithCliFlags() {
        String file = "./folder/testfile.yaml";

        ApplicationArguments args = new DefaultApplicationArguments(
                "-f", file, "--spring.profiles.active=dev");

        QualityReport qualityReport = new QualityReport(true, List.of());

        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromFile(file))
                .thenReturn(DcatResult.success("<rdf:RDF/>", qualityReport));

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            ExitCode result = cliRunner.execute(args);

            assertEquals(ExitCode.SUCCESS, result);
            utilMock.verify(() -> Util.printToFile("<rdf:RDF/>", "dcat.rdf"));
        }

        verify(manager).createDcatFromFile(file);
    }
}
