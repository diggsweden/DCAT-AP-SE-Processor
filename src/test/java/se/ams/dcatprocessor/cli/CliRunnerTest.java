// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.cli;

import java.util.List;

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

@ExtendWith(MockitoExtension.class)
public class CliRunnerTest {
    
    @Mock
    private Manager manager;

    @Mock
    private ObjectProvider<Manager> managerProvider;

    @Mock
    private ApplicationContext context;

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
        
        cliRunner.run(args);
        
        verify(manager).createDcatFromFile(file);
    }

    @Test
    void testThatResultWithErrorsHasNoInteractionWithPrintFile() {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-f";
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments(flag, file);
        when(manager.createDcatFromFile(file)).thenReturn(DcatResult.errors("error creating RDF"));
        
        cliRunner.run(args);

        // DcatResult with errors should result in printed file
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            cliRunner.run(args);
            utilMock.verifyNoInteractions();
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
        
        cliRunner.run(args);
        
        verify(manager).createDcatFromDirectory(dirname);
    }

    @Test
    void testThatInvalidFlagHasNoInteractionsWithService() {
        String flag = "-unknown";
        ApplicationArguments args = new DefaultApplicationArguments(flag);
        
        cliRunner.run(args);
        
        verifyNoInteractions(manager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-f", "-d"})
    void testThatMissingFlagValueHasNoInteractionsWithService(String flag) {
        ApplicationArguments args = new DefaultApplicationArguments(flag);
        
        cliRunner.run(args);
        
        verifyNoInteractions(manager);
    }
}
