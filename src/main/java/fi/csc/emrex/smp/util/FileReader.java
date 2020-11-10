package fi.csc.emrex.smp.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

/**
 * Created by marko.hollanti on 06/10/15.
 */
public class FileReader {

  private final static String ENCODING = StandardCharsets.UTF_8.name();

  private static FileReader instance;

  private FileReader() {
  }

  public static String getFilePath(String filename) throws Exception {
    return getFile(filename).getAbsolutePath();
  }

  public static String getFileContent(String filename) throws Exception {
    if (instance == null) {
      instance = new FileReader();
    }
    return FileUtils
        .readFileToString(FileUtils.toFile(instance.getClass().getResource("/" + filename)),
            ENCODING);
  }

  public static File getFile(String filename) throws Exception {
    if (instance == null) {
      instance = new FileReader();
    }
    return FileUtils.toFile(instance.getClass().getResource("/" + filename));
  }
}
