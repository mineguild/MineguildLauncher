package net.mineguild;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class Constants {

  public static IOFileFilter MODPACK_FILE_FILTER = FileFilterUtils.and(FileFilterUtils
      .notFileFilter(FileFilterUtils.and(FileFilterUtils.suffixFileFilter(".dis"),
          FileFilterUtils.suffixFileFilter(".opt"))), FileFilterUtils.sizeFileFilter(1l, true));
  public static IOFileFilter MODPACK_DIR_FILTER = FileFilterUtils.trueFileFilter();

}
