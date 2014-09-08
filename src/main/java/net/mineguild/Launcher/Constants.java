package net.mineguild.Launcher;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class Constants {

  public static final IOFileFilter MODPACK_FILE_FILTER =
      FileFilterUtils
          .and(
              FileFilterUtils.notFileFilter(FileFilterUtils.and(
                  FileFilterUtils.suffixFileFilter(".dis"),
                  FileFilterUtils.suffixFileFilter(".opt"),
                  FileFilterUtils.suffixFileFilter(".disabled"))),
              FileFilterUtils.sizeFileFilter(1l, true));
  public static final IOFileFilter MODPACK_DIR_FILTER = FileFilterUtils.trueFileFilter();

  // Minecraft links
  public static final String MC_RES = "http://resources.download.minecraft.net/"; // Minecraft
                                                                                  // resources(assets)
  public static final String MC_DL = "https://s3.amazonaws.com/Minecraft.Download/"; // Minecraft
                                                                                     // jars+jsons
  public static final String MC_LIB = "https://libraries.minecraft.net/"; // Minecraft libraries

  // Mineguild links
  public static final String MG_FORGE = "https://mineguild.net/download/mmp/forge/";
  public static final String MG_INFO_SCRIPT = "https://mineguild.net/download/mmp/php/info.php";
  public static final String MG_GET_SCRIPT = "https://mineguild.net/download/mmp/php/getfile.php";
  public static final String MG_LOGIN_SCRIPT =
      "https://mineguild.net/download/mmp/php/checkuser.php";


}
