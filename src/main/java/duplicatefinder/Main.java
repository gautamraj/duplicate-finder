package duplicatefinder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

/** Runs the duplicate finder. */
public class Main {

  public static class Args {

    @Parameter(
        names = {"-d", "--directory"},
        required = true)
    private List<String> directories = new ArrayList<>();

    @Parameter(
        names = {"-v", "--verbose"},
        description = "Verbose mode")
    private boolean isVerbose = false;
  }

  public static void main(String[] argv) throws Exception {
    Args args = new Args();
    JCommander.newBuilder().addObject(args).build().parse(argv);

    if (args.isVerbose) {
      Configurator.setRootLevel(Level.DEBUG);
    }

    System.out.println("Searching " + args.directories + "...");

    // Call the DuplicateFinder and get all duplicate groups.
    DuplicateFinder duplicateFinder = new DuplicateFinder(FileSystems.getDefault());
    List<List<Path>> duplicates = duplicateFinder.getDuplicates(args.directories);

    if (duplicates.isEmpty()) {
      System.out.println("No duplicates found!");
      return;
    }

    for (List<Path> duplicateGroup : duplicates) {
      System.out.println("Found duplicates: " + duplicateGroup);
    }
  }
}
