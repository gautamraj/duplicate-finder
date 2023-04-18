package duplicatefinder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Runs the duplicate finder. */
public class Main {

  public static class Args {

    @Parameter(
        names = {"-d", "--directory"},
        required = true)
    private List<String> directories = new ArrayList<>();

    @Parameter(names = "-debug", description = "Debug mode")
    private boolean debug = false;
  }

  public static void main(String[] argv) throws Exception {
    Args args = new Args();
    JCommander.newBuilder().addObject(args).build().parse(argv);

    System.out.println("Searching " + args.directories + "...");

    DuplicateFinder duplicateFinder =
        new DuplicateFinder(FileSystems.getDefault(), args.directories);
    List<List<Path>> duplicates = duplicateFinder.getDuplicates();
    if (duplicates.isEmpty()) {
      System.out.println("No duplicates found!");
      return;
    }

    for (List<Path> duplicateGroup : duplicates) {
      System.out.println("Found duplicates: " + duplicateGroup);
    }
  }
}
