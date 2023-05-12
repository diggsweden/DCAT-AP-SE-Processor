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

import org.junit.jupiter.api.Test;
import org.apache.commons.collections4.MultiValuedMap;
import se.ams.dcatprocessor.rdf.DcatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConversionConfigTest {
    
    @Test
    void testFromKeyValue() throws Exception {
        ConversionConfig cfg = ConversionConfig.fromKeyValue("k119", "the spec goes here");
        assertEquals(ConversionConfig.getDefaultWorkDir(), cfg.getWorkDir());
        assertTrue(cfg.isValid());
        MultiValuedMap m = cfg.getApiSpecMap();
        assertEquals(1, m.size());
        assertEquals(List.of("the spec goes here"), m.get("k119"));
        checkWorkDirChangeIsWorking(cfg);
    }
    
    @Test
    void testFromFileAndDirectory() throws Exception {
        ConversionConfig[] configsToTest = new ConversionConfig[]{
            ConversionConfig.fromFile(Path.of("src/test/resources/apidef/json_oas/obl_rek_oas.json")),
            ConversionConfig.fromDirectory(Path.of("src/test/resources/apidef/json_oas")),
        };
        for (ConversionConfig cfg: configsToTest) {
            checkJsonOasContents(cfg);
            checkWorkDirChangeIsWorking(cfg);
        }        
    }

    @Test
    void testMultipartFile() throws Exception {
        String filename = "src/test/resources/apidef/json_oas/obl_rek_oas.json";
        Path srcPath = Path.of(filename);
        byte[] bytes = Files.readAllBytes(srcPath);
        MockMultipartFile file = new MockMultipartFile(filename, filename, StandardCharsets.UTF_8.name(), bytes);
        
        ArrayList<Result> results = new ArrayList<Result>();
        ConversionConfig cfg = ConversionConfig.fromList(List.of(file), results);
        checkJsonOasContents(cfg);
        
        // If no exceptions are thrown, then nothing is pushed to results.
        assertTrue(results.isEmpty());

        checkWorkDirChangeIsWorking(cfg);        
    }

    // Common checks for the unit tests
    private ConversionConfig checkWorkDirChangeIsWorking(ConversionConfig src) throws Exception {
        Path tempDir = Files.createTempDirectory("DCAT_workdir");
        ConversionConfig dst = src.withWorkDir(tempDir);
        assertEquals(tempDir, dst.getWorkDir());
        assertEquals(ConversionConfig.getDefaultWorkDir(), src.getWorkDir());
        return dst;        
    }

    private void checkJsonOasContents(ConversionConfig cfg) {
        assertTrue(cfg.isValid());
        MultiValuedMap m = cfg.getApiSpecMap();
        assertEquals(1, m.size());
        Collection<String> coll = m.get("src/test/resources/apidef/json_oas/obl_rek_oas.json");
        assertEquals(1, coll.size());
        String first = coll.iterator().next();
        assertTrue(first.startsWith("{"));
    }
}
