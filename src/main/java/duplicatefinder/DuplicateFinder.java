package duplicatefinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DuplicateFinder {
  private static final Logger log = LogManager.getLogger();

  // The "first N bytes" threshold. This should be a multiple of 512 bytes, the typical disk page
  // size. This should be large enough to detect duplicates, but not so large that it's expensive or
  // takes up a lot of memory.
  public static final int FIRST_BYTES_THRESHOLD = 4096;

  private final FileSystem fileSystem;

  public DuplicateFinder(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  /** Search the given directories and return all duplicates. */
  public List<List<Path>> getDuplicates(List<String> directoriesToSearch) throws IOException {
    Map<Long, List<Path>> filesBySize = Maps.newHashMap();
    for (String directory : directoriesToSearch) {
      // Bucket by size
      Path path = fileSystem.getPath(directory);
      groupFilesBySize(path, filesBySize);
    }
    log.debug("Found {} different sizes", filesBySize.size());

    // Remove any buckets with a single item - these cannot be duplicates.
    removeSingleEntries(filesBySize);
    log.debug("Found {} possible duplicates by size", filesBySize.size());

    // Take the existing size buckets and further split them out into buckets by first N bytes.
    List<List<Path>> filesByFirstBytes = furtherBucketByFirstNBytes(filesBySize);
    log.debug(
        "Found {} possible duplicates by first {} bytes",
        FIRST_BYTES_THRESHOLD,
        filesByFirstBytes.size());

    // Finally, group by the full contents.
    List<List<Path>> bucketByContents = furtherBucketByContents(filesByFirstBytes);
    log.debug("Found {} duplicates by contents", bucketByContents.size());
    return bucketByContents;
  }

  /** Bucket files by their size. */
  private static void groupFilesBySize(Path path, Map<Long, List<Path>> filesBySize)
      throws IOException {
    try (Stream<Path> walk = Files.walk(path)) {
      Map<Long, List<Path>> curFilesBySize =
          walk.filter(Files::isRegularFile)
              .collect(
                  Collectors.groupingBy(
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

      curFilesBySize.forEach(
          (k, v) -> filesBySize.computeIfAbsent(k, ignored -> Lists.newArrayList()).addAll(v));
    }
  }

  /** Bucket files that are already grouped by some key, by their first N bytes. */
  private List<List<Path>> furtherBucketByFirstNBytes(Map<Long, List<Path>> filesBySize) {
    List<List<Path>> duplicateGroups = Lists.newArrayList();

    for (List<Path> group : filesBySize.values()) {
      Map<ByteBuffer, List<Path>> filesByFirstNBytes = Maps.newHashMap();
      for (Path path : group) {
        ByteBuffer firstNBytes = getFirstNBytes(path);
        filesByFirstNBytes.computeIfAbsent(firstNBytes, k -> Lists.newArrayList()).add(path);
      }
      duplicateGroups.addAll(
          filesByFirstNBytes.values().stream()
              .filter(i -> i.size() > 1)
              .collect(Collectors.toList()));
    }
    return duplicateGroups;
  }

  /** Do a full equals check using a checksum. */
  private List<List<Path>> furtherBucketByContents(List<List<Path>> previousCandidateDuplicates)
      throws IOException {
    List<List<Path>> duplicateGroups = Lists.newArrayList();
    for (List<Path> group : previousCandidateDuplicates) {
      Map<HashCode, List<Path>> map = Maps.newHashMap();
      for (Path path : group) {
        HashCode checksum = getChecksum(path);
        map.computeIfAbsent(checksum, k -> Lists.newArrayList()).add(path);
      }
      duplicateGroups.addAll(map.values());
    }
    return duplicateGroups;
  }

  /** Read the first N bytes. */
  private static ByteBuffer getFirstNBytes(Path path) {
    try (InputStream inputStream = Files.newInputStream(path)) {
      return ByteBuffer.wrap(inputStream.readNBytes(FIRST_BYTES_THRESHOLD));
    } catch (IOException e) {
      // This could happen if files in the target directory change - give up if this happens.
      throw new IllegalStateException("Failed to read first bytes", e);
    }
  }

  /** Return the SHA-256 checksum of the given file. */
  private static HashCode getChecksum(Path path) throws IOException {
    try (HashingInputStream inputStream =
        new HashingInputStream(Hashing.sha256(), Files.newInputStream(path))) {
      return inputStream.hash();
    }
  }

  /** Remove Map entries that only have one element in their list (in-place). */
  private static <K, V extends List<?>> void removeSingleEntries(Map<K, V> map) {
    map.entrySet().removeIf(e -> e.getValue().size() == 1);
  }
}
