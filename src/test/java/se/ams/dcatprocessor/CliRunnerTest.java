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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class CliRunnerTest {
    
    @Mock
    private Manager manager;

    @Mock
    private ObjectProvider<Manager> managerProvider;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private CliRunner cliRunner;


    @Test
    void testThatCreateDcatFromFileIsCalled() throws Exception {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-f";
        String file = "./folder/testfile.yaml";
        ApplicationArguments args = new DefaultApplicationArguments(flag, file);
        
        cliRunner.run(args);
        
        verify(manager).createDcatFromFile(file);
    }

    @Test
    void testThatCreateDcatFromDirectoryIsCalled() throws Exception {
        when(managerProvider.getObject()).thenReturn(manager);
        String flag = "-d";
        String dirname = "./testFiles";
        ApplicationArguments args = new DefaultApplicationArguments(flag, dirname);
        when(managerProvider.getObject()).thenReturn(manager);

        cliRunner.run(args);
        
        verify(manager).createDcatFromDirectory(dirname);
    }

    @Test
    void testThatInvalidFlagHasNoInteractionsWithService() throws Exception {
        String flag = "-unknown";
        ApplicationArguments args = new DefaultApplicationArguments(flag);
        
        cliRunner.run(args);
        
        verifyNoInteractions(manager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-f", "-d"})
    void testThatMissingFlagValueHasNoInteractionsWithService(String flag) throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments(flag);
        
        cliRunner.run(args);
        
        verifyNoInteractions(manager);
    }
}
