package net.mineguild.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.mineguild.ModPack.ModPackFile;

import org.apache.commons.io.FileUtils;

public class UploadFileUtils {

  public static void placeUploadFiles(String basePath, Map<String, ModPackFile> files) {
    File uploadDir = new File("upload");
    uploadDir.mkdir();
    try {
      FileUtils.cleanDirectory(uploadDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Map.Entry<String, ModPackFile> entry : files.entrySet()) {
      File file = new File(basePath, entry.getKey());
      File newDirectory = new File(uploadDir, entry.getValue().getHash().substring(0, 2));
      File newFile = new File(newDirectory, entry.getValue().getHash());
      if (file.exists()) {
        try {
          FileUtils.copyFile(file, newFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void fromUploadFiles(Map<String, String> files) {
    File modpack = new File("modpack");
    File upload = new File("upload");
    modpack.mkdir();
    try {
      FileUtils.cleanDirectory(modpack);
    } catch (IOException ignored) {
    }
    for (Map.Entry<String, String> entry : files.entrySet()) {
      String hash = entry.getValue();
      File fileDir = new File(upload, hash.substring(0, 2));
      File file = new File(fileDir, hash);
      String path = entry.getKey();
      File filePath = new File(modpack, path);
      try {
        System.out.printf("Copying %s to %s\n", file.toString(), filePath.toString());
        FileUtils.copyFile(file, filePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
