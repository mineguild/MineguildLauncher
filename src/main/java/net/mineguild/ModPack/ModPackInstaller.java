package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.google.common.collect.Lists;

public class ModPackInstaller {

  /**
   * Determines the files that are needed to update/repair/install the ModPack
   * 
   * @param installDirectory The directory as {@link File} to check.
   * @param pack The {@link net.mineguild.ModPack.ModPack} to check against.
   * @param side The {@link net.mineguild.ModPack.Side} that should be installed, should be SERVER
   *        or CLIENT
   * @return A List of needed {@link net.mineguild.Launcher.download.DownloadInfo}
   * @throws Exception
   */
  public static synchronized List<DownloadInfo> checkNeededFiles(final File installDirectory,
      ModPack pack, final Side side) throws Exception {
    checkNotNull(installDirectory);
    List<DownloadInfo> result =
        (List<DownloadInfo>) new Parallel.ForEach<Entry<String, ModPackFile>, DownloadInfo>(pack
            .getFiles().entrySet()).withFixedThreads(OSUtils.getNumCores() * 2)
            .apply(new Parallel.F<Map.Entry<String, ModPackFile>, DownloadInfo>() {

              @Override
              public DownloadInfo apply(Map.Entry<String, ModPackFile> entry) {
                String path = entry.getKey();
                ModPackFile packFile = entry.getValue();
                if (side == Side.BOTH || packFile.getSide() == side
                    || packFile.getSide() == Side.UNIVERSAL) {
                  File localFile = new File(installDirectory, path);
                  if (localFile.exists()) {
                    try {
                      if (!ChecksumUtil.getMD5(localFile).equals(packFile.getHash())) {
                        localFile.delete();
                      }
                    } catch (IOException e) {
                      Logger.logError("Error ocurred during ModPackFile Hash-Checking", e);
                    }
                  }
                  try {
                    if (!localFile.exists()) {
                      String dlPath = packFile.getHash().substring(0, 2) + "/" + packFile.getHash();
                      DownloadInfo ret =
                          new DownloadInfo(new URL(Constants.MG_MMP_FILES + dlPath), localFile,
                              localFile.getName(), Lists.newArrayList(packFile.getHash()), "md5",
                              DLType.ContentMD5, DLType.NONE);
                      ret.size = packFile.getSize();
                      return ret;
                    }
                  } catch (MalformedURLException e) {
                    Logger.logError("Couldn't process url!", e);
                  }
                }
                return null;
              }

            }).values();
    return result;
  }


  /**
   * Clears a folder of all files that are not in the ModPack, or not matching the needed side.
   * 
   * @param target Directory to clear.
   * @param pack The {@link ModPack} to check against.
   * @param backupDirectory The place to backup the cleared files to. No backup if <code>null</code>
   * 
   * @throws NullPointerException if pack or target <code>null</code>.
   */
  public static synchronized void clearFolder(final File target, final ModPack pack,
      final Side side, final File backupDirectory) {
    checkNotNull(target);
    checkNotNull(pack);
    boolean temp = false;
    try {
      temp = !backupDirectory.equals(null);
    } catch (Exception ignored) {
    }
    final boolean doBackup = temp;

    if (doBackup) {
      if (!backupDirectory.exists() || !backupDirectory.isDirectory()) {
        backupDirectory.delete();
        backupDirectory.mkdirs();
      }
    }
    Parallel.ForEach<File, Void> p =
        new Parallel.ForEach<File, Void>(FileUtils.listFiles(target,
            FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()));
    try {
      p.withFixedThreads(OSUtils.getNumCores() * 2).apply(new Parallel.F<File, Void>() {

        @Override
        public Void apply(File f) {
          try {
            String hash = ChecksumUtil.getMD5(f);
            if (pack.getFilesByHashAndSide(hash, side).isEmpty()) {
              if (doBackup) {
                Logger.logInfo(String.format("Moving file %s to backup folder - not in pack!",
                    f.getName()));
                try {
                  FileUtils.moveFileToDirectory(f, new File(backupDirectory, f.getParent()), true);
                } catch (FileExistsException e2) {
                  Logger.logInfo(String.format("Not moving file %s!", f.getName()), e2);
                  f.delete();
                }
              } else {
                Logger.logInfo(String.format("Deleting file %s - not in pack!", f.getName()));
                f.delete();
              }
            }
          } catch (IOException e1) {
            Logger.logError(String.format("Unable to check hash of %s!", f.getName()), e1);
          }

          return null;
        }
      }).values();
    } catch (Exception e) {
      Logger.logError("Parallel execution exception", e);
    }

  }

}
