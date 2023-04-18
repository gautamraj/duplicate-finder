package duplicatefinder;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DuplicateFinder {

  private final FileSystem fileSystem;
  private final List<String> directories;

  public DuplicateFinder(FileSystem fileSystem, List<String> directories) {
    this.fileSystem = fileSystem;
    this.directories = directories;
  }

  public Collection<List<Path>> getDuplicates() throws IOException {
    Map<Long, List<Path>> filesBySize = Maps.newHashMap();
    for (String directory : directories) {
      // Bucket by size
      Path path = fileSystem.getPath(directory);
      groupFilesBySize(path, filesBySize);
    }

    // Remove any buckets with a single item - these cannot be duplicates.
    removeUnique(filesBySize);

    // TODO - group by first N bytes
    // TODO - group by contents

    return filesBySize.values();
  }

  /**
   * Bucket files by their size.
   */
  private static void groupFilesBySize(Path path,
      Map<Long, List<Path>> filesBySize) throws IOException {
    try (Stream<Path> walk = Files.walk(path)) {
      Map<Long, List<Path>> curFilesBySize = walk.filter(
              curPath -> curPath.toFile().isFile())
          .collect(Collectors.groupingBy(
              // Group by filesize as the key.
              curPath -> {
                try {
                  return Files.size(curPath);
                } catch (IOException e) {
                  throw new IllegalStateException("Failed to get file size", e);
                }
              },
              // Maps to a list of all matching paths.
              Collectors.mapping(Function.identity(), Collectors.toList())));

      filesBySize.putAll(curFilesBySize);
    }
  }

  /**
   * Remove Map entries with a single value.
   */
  private static <K, V extends List<?>> void removeUnique(Map<K, V> map) {
    map.entrySet().removeIf(e -> e.getValue().size() == 1);
  }

}
