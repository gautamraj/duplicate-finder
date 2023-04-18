package duplicatefinder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Entrypoint into the example project */
public class Main {
  public static class Args {
    @Parameter(names = {"-d", "--directory"}, required = true)
    private List<String> directories = new ArrayList<>();

    @Parameter(names = "-debug", description = "Debug mode")
    private boolean debug = false;
  }

  public static void main(String[] argv) throws Exception {
    Args args = new Args();
    JCommander.newBuilder().addObject(args).build().parse(argv);

    DuplicateFinder duplicateFinder = new DuplicateFinder(args.directories);
    for (String duplicate : duplicateFinder.getDuplicates()) {
      System.out.println(duplicate);
    }
  }
}
