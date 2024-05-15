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

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import se.ams.dcatprocessor.Manager;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("-f")
                && (args[1].endsWith(".raml") || args[1].endsWith(".yaml") || args[1].endsWith(".json"))) {
            convertFile(args[1]);
        } else if (args.length == 2 && args[0].equals("-d")) {
            convertDir(args[1]);
        } else {
            SpringApplication.run(Application.class, args);
        }
    }


    public static void convertFile(String filename) {
        Manager manager = new Manager();
        try {
            ConversionConfig config = ConversionConfig.fromFile(Path.of(filename));
            String result = manager.createDcat(config);
            assertTrue(!result.isEmpty());
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void convertDir(String dirname) {
        Manager manager = new Manager();
        String result;

        try {
            result = manager.createDcatFromDirectory(dirname);
            assertTrue(!result.isEmpty());
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
