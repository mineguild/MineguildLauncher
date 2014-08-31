package net.mineguild.Launcher;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class Constants {
  
  public static IOFileFilter MODPACK_FILE_FILTER = FileFilterUtils.and(FileFilterUtils
      .notFileFilter(FileFilterUtils.and(FileFilterUtils.suffixFileFilter(".dis"),
          FileFilterUtils.suffixFileFilter(".opt"))), FileFilterUtils.sizeFileFilter(1l, true));
  public static IOFileFilter MODPACK_DIR_FILTER = FileFilterUtils.trueFileFilter();
  
  public static String MC_RES = "http://resources.download.minecraft.net/"; // Minecraft resources(assets)
  public static String MC_DL = "https://s3.amazonaws.com/Minecraft.Download/"; // Minecraft jars+jsons
  public static String MC_LIB = "https://libraries.minecraft.net/"; // Minecraft libraries
  
  public static String MG_FORGE = "https://mineguild.net/download/mmp/forge/";
  

}
