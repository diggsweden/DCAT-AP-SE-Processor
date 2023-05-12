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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.web.multipart.MultipartFile;


public class ConversionConfig {
    private Path workDir;
    private MultiValuedMap<String, String> apiSpecMap;
    
    public static Path getDefaultWorkDir() {
        return Path.of(System.getProperty("user.dir"));
    }
    
    public boolean isValid() {
        return apiSpecMap != null && !apiSpecMap.isEmpty();
    }

    private ConversionConfig(MultiValuedMap<String, String> apiSpecMap, Path workDir) {
        this.apiSpecMap = apiSpecMap;
        this.workDir = workDir;
    }

    private ConversionConfig copy() {
        return new ConversionConfig(apiSpecMap, workDir);
    }

    public ConversionConfig withWorkDir(Path workDir) {
        ConversionConfig dst = copy();
        dst.workDir = workDir;
        return dst;
    }

    public MultiValuedMap<String, String> getApiSpecMap() {
        assert(isValid());
        return apiSpecMap;
    }

    public static ConversionConfig fromApiSpecMap(MultiValuedMap<String, String> apiSpecMap) {
        return new ConversionConfig(apiSpecMap, getDefaultWorkDir());
    }

    public static ConversionConfig fromKeyValue(String key, String value) {
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        apiSpecMap.put(key, value);
        return ConversionConfig.fromApiSpecMap(apiSpecMap);
    }

    public static ConversionConfig fromFile(Path path) throws IOException {
        return ConversionConfig.fromKeyValue(path.toString(), Files.readString(path));
    }
    
    public static ConversionConfig fromDirectory(Path dir) throws IOException {
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();

        // returns pathnames for files and directory
        File[] files = dir.toFile().listFiles((dir1, name) -> name.endsWith(".raml") || name.endsWith(".yaml") || name.endsWith(".json"));

        // for each file in file array
        if (files != null && files.length > 0) {
            for (File file : files) {
                Path path = Path.of(String.valueOf(file));
                try {
                    String content = Files.readString(path);
                    apiSpecMap.put(path.toString(), content);
                } finally {
                }
            }
        }
        return ConversionConfig.fromApiSpecMap(apiSpecMap);
    }

    public static ConversionConfig fromList(List<MultipartFile> apiFiles, List<Result> outResults) {
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        for (MultipartFile apiFile : apiFiles) {
            if (!apiFile.isEmpty()) {
                String apiSpecificationFromFile;
                Scanner scanner;
                try {
                    scanner = new Scanner(apiFile.getInputStream(), StandardCharsets.UTF_8.name());
                    apiSpecificationFromFile = scanner.useDelimiter("\\A").next();
                    scanner.close();
                    apiSpecMap.put(apiFile.getOriginalFilename(), apiSpecificationFromFile);
                } catch (Exception e) {        //Catch and show processing errors in web-gui
                    outResults.add(new Result(null, e.getMessage()));
                    e.printStackTrace();
                }
            }
        }
        return ConversionConfig.fromApiSpecMap(apiSpecMap);
    }

    public Path getWorkDir() {
        return workDir;
    }
}
