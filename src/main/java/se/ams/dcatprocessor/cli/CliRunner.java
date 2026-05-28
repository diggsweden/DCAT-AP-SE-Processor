// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

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
