// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
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
import se.ams.dcatprocessor.util.Util;

@ExtendWith(MockitoExtension.class)
public class CliRunnerTest {

    @Mock
    private Manager manager;

    @Mock
    private ObjectProvider<Manager> managerProvider;

    @Mock
    private ApplicationContext context; // Required by CliRunner's constructor

    @InjectMocks
    private CliRunner cliRunner;

    @Test
    void testThatCreateDcatFromFileIsCalled() {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-f";
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments(flag, file);
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.success("<rdf:RDF/>"));

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
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromDirectory(dirname)).thenReturn(DcatResult.success("<rdf:RDF/>"));

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

        verifyNoInteractions(manager);
        assertEquals(ExitCode.USAGE_ERROR, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-f", "-d" })
    void testThatMissingFlagValueHasNoInteractionsWithService(String flag) {
        ApplicationArguments args = new DefaultApplicationArguments(flag);

        ExitCode result = cliRunner.execute(args);

        verifyNoInteractions(manager);
        assertEquals(ExitCode.USAGE_ERROR, result);
    }

    @Test
    void testThatFileWriteErrorGivesUnexpectedError() {
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments("-f", file);
        when(managerProvider.getObject()).thenReturn(manager);
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.success("<rdf:RDF/>"));
    
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
}
