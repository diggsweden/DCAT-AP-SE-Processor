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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

public class ApplicationTest {
    
    @TempDir
    Path tempDir;

    @Test
    public void testThatConvertToFileHandlesEmptyResult() throws Exception{
        Path tempFile = tempDir.resolve("test.json");
        Files.writeString(tempFile, "{}");
        RuntimeException result = null;

        try (MockedConstruction<Manager> mock = mockConstruction(Manager.class,
                (m, ctx) -> when(m.createDcat(any())).thenReturn(""))) {
                
            result = assertThrows(RuntimeException.class,
                () -> Application.convertFile(tempFile.toString()));
        }

        assertTrue(result.getCause().getMessage().contains("Kunde inte generera en dcat fil"));
    }

    @Test
    public void testThatConvertDirHandlesEmptyResult() throws Exception{
        RuntimeException result = null;
 
        try (MockedConstruction<Manager> mock = mockConstruction(Manager.class,
                (m, ctx) -> when(m.createDcatFromDirectory(any())).thenReturn(""))) {
                
            result = assertThrows(RuntimeException.class,
                () -> Application.convertDir("mockDirectory"));
        }

        assertTrue(result.getCause().getMessage().contains("Kunde inte generera en dcat fil"));
    }
}
