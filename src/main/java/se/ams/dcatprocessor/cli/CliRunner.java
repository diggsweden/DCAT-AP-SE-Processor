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

package se.ams.dcatprocessor.cli;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import se.ams.dcatprocessor.processor.Manager;

@Component
public class CliRunner implements ApplicationRunner{

    private final ObjectProvider<Manager> managerProvider;
    private final ApplicationContext context;

    public CliRunner(ObjectProvider<Manager> managerProvider, ApplicationContext context) {
        this.managerProvider = managerProvider;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> nonOptionArgs = args.getNonOptionArgs();

        int flagIndex = IntStream.range(0, nonOptionArgs.size())
        .filter(i -> CliFlags.SUPPORTED_FLAGS.contains(nonOptionArgs.get(i)))
        .findFirst()
        .orElse(-1);

        // If no CLI args: run application with webserver
        if(flagIndex == -1){
            return;
        }
        
        // No value for flag specified: exit application
        if (flagIndex + 1 >= nonOptionArgs.size()) {
            System.out.println("Invalid CLI arguments");
            exitSpringApplication();
            return;
        }

        String flag = nonOptionArgs.get(flagIndex);
        String flagValue = nonOptionArgs.get(flagIndex + 1);

        switch(flag){
            case "-f" -> createDcatFromFile(flagValue);
            case "-d" -> createDcatFromDirectory(flagValue);
        }
    }

    private void createDcatFromFile(String filename){
        Manager manager = managerProvider.getObject();
        String result = manager.createDcatFromFile(filename);
        System.out.println(result); 
        exitSpringApplication();
    }

    private void createDcatFromDirectory(String dirname) {
        Manager manager = managerProvider.getObject();

        try {
            String result = manager.createDcatFromDirectory(dirname);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("Error generating dcat from directory: " + dirname);
        }
        exitSpringApplication();
    }

    private void exitSpringApplication(){
        SpringApplication.exit(context, () -> 0);
    }
}
