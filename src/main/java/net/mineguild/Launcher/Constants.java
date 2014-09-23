package net.mineguild.Launcher;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class Constants {

  public static final IOFileFilter MODPACK_FILE_FILTER = FileFilterUtils.and(FileFilterUtils
      .notFileFilter(FileFilterUtils.or(FileFilterUtils.suffixFileFilter("dis"),
          FileFilterUtils.suffixFileFilter("opt"), FileFilterUtils.suffixFileFilter("disabled"))),
      FileFilterUtils.sizeFileFilter(1l, true));
  public static final IOFileFilter MODPACK_DIR_FILTER = FileFilterUtils.trueFileFilter();

  // Minecraft links
  public static final String MC_RES = "http://resources.download.minecraft.net/"; // Minecraft
                                                                                  // resources(assets)
  public static final String MC_DL = "https://s3.amazonaws.com/Minecraft.Download/"; // Minecraft
                                                                                     // jars+jsons
  public static final String MC_LIB = "https://libraries.minecraft.net/"; // Minecraft libraries

  // Mineguild links
  public static final String MG_MMP = "https://mineguild.net/download/mmp/";
  public static final String MG_MMP_FILES = MG_MMP + "files/";
  public static final String MG_FORGE = MG_MMP + "forge/";
  public static final String MG_LIBS = MG_FORGE + "libs/";
  public static final String MG_INFO_SCRIPT = MG_MMP + "php/info.php";
  public static final String MG_GET_SCRIPT = MG_MMP + "php/getfile.php";
  public static final String MG_LOGIN_SCRIPT = MG_MMP + "php/checkuser.php";

  public static final String AUTHLIB_VERSION = "1.5.16";


}
