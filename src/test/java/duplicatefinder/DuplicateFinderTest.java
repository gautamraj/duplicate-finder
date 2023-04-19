package duplicatefinder;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DuplicateFinderTest {

  private FileSystem fileSystem;

  @Before
  public void setUp() {
    fileSystem = Jimfs.newFileSystem();
  }

  @Test
  public void testSimple() throws Exception {
    Files.createDirectories(fileSystem.getPath("foo/"));
    Files.createDirectories(fileSystem.getPath("bar/"));

    // Small files
    String smallRandom1 = RandomStringUtils.randomAlphabetic(100);
    String smallRandom2 = RandomStringUtils.randomAlphabetic(100);
    Files.writeString(fileSystem.getPath("foo/aaa_small"), smallRandom1);
    Files.writeString(fileSystem.getPath("foo/bbb_small"), smallRandom2);
    // Same file, same directory.
    Files.writeString(fileSystem.getPath("foo/ccc_small"), smallRandom1); // same as aaa

    // Big files
    String bigRandom1 = RandomStringUtils.randomAlphabetic(100000);
    String bigRandom2 = RandomStringUtils.randomAlphabetic(100000);
    Files.writeString(fileSystem.getPath("foo/aaa_big"), bigRandom1);
    Files.writeString(fileSystem.getPath("foo/bbb_big"), bigRandom2);
    // Same file, different directory.
    Files.writeString(fileSystem.getPath("bar/ccc_big"), bigRandom1); // same as aaa

    DuplicateFinder duplicateFinder = new DuplicateFinder(fileSystem);
    List<List<Path>> duplicates = duplicateFinder.getDuplicates(ImmutableList.of("foo/", "bar/"));
    assertThat(duplicates).hasSize(2);
    assertThat(duplicates)
        .containsExactlyInAnyOrder(
            Lists.newArrayList(
                fileSystem.getPath("foo/aaa_big"), fileSystem.getPath("bar/ccc_big")),
            Lists.newArrayList(
                fileSystem.getPath("foo/aaa_small"), fileSystem.getPath("foo/ccc_small")));
  }
}
