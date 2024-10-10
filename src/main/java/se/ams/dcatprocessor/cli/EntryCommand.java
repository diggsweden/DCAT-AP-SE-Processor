package se.ams.dcatprocessor.cli;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import picocli.CommandLine;
import se.ams.dcatprocessor.Manager;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true)
public class EntryCommand implements Runnable {
  @Override
  public void run() {
    System.out.println("Running in web mode (no CLI command provided)");
    Quarkus.waitForExit();
  }
}

@CommandLine.Command(name = "FileCommand", description = "Generate RDF/XML from file")
class FileCommand implements Runnable {

  @CommandLine.Option(names = {"-f", "--file"}, description = "filename to be used for generating RDF/XML")
  String filename;

  @Inject
  Manager manager;

  @Override
  @ActivateRequestContext
  public void run() {
    Path path = Path.of(filename);
    MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
    String result;

    try {
      String content = Files.readString(path);
      apiSpecMap.put(path.toString(), content);
      result = manager.createDcat(apiSpecMap);
      assertFalse(result.isEmpty());
      System.out.println(result);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

@CommandLine.Command(name = "DirectoryCommand", description = "Generate RDF/XML from specified directory")
class DirectoryCommand implements Runnable {

  @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to be used for generating RDF/XML")
  String dirname;

  @Inject
  Manager manager;

  @Override
  @ActivateRequestContext
  public void run() {
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
