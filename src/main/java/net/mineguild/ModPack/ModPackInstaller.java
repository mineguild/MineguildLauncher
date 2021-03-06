package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.RelativePath;

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
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static synchronized List<DownloadInfo> checkNeededFiles(final File installDirectory,
      ModPack pack, final Side side) throws InterruptedException, ExecutionException {
    checkNotNull(installDirectory);
    List<DownloadInfo> result =
        (List<DownloadInfo>) new Parallel.ForEach<Entry<String, ModPackFile>, DownloadInfo>(pack
            .getFiles().entrySet()).withFixedThreads(OSUtils.getNumCores() * 2)
            .apply(new Parallel.F<Map.Entry<String, ModPackFile>, DownloadInfo>() {

              @Override
              public DownloadInfo apply(Map.Entry<String, ModPackFile> entry) {
                String path = entry.getKey();
                ModPackFile packFile = entry.getValue();
                if (packFile.sideMatches(side)) {
                  File localFile = new File(installDirectory, path);
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
   * @throws NullPointerException if pack or target <code>null</code>.
   */
  public static synchronized void clearFolder(final File target, final ModPack pack,
      final Side side, final File backupDirectory) throws IOException {
    checkNotNull(target);
    checkNotNull(pack);
    if (!target.exists()) {
      throw new FileNotFoundException(
          String.format("'%s' doesn't exist!", target.getAbsolutePath()));
    }
    if (!target.isDirectory()) {
      throw new IOException(String.format("'%s' is no valid directory!", target.getAbsolutePath()));
    }
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
            Map<String, ModPackFile> files = pack.getFilesByHash(hash);
            if (files.isEmpty()) {
              if (doBackup) {
                Logger.logInfo(String.format("Moving file %s to backup folder - not in pack!",
                    f.getName()));
                try {
                  FileUtils.moveFileToDirectory(
                      f,
                      new File(backupDirectory, RelativePath.getRelativePath(target,
                          new File(f.getParent()))), true);
                } catch (FileExistsException e2) {
                  Logger.logInfo(String.format("Not moving file %s!", f.getName()), e2);
                  f.delete();
                }
              } else {
                Logger.logInfo(String.format("Deleting file %s - not in pack!", f.getName()));
                f.delete();
              }
            } else {
              for (ModPackFile packFile : files.values()) {
                if (packFile.sideMatches(side)) {
                  Logger.logDebug(String.format("Leaving %s in there - side matches", f.getName()));
                } else {
                  if (doBackup) {
                    Logger.logInfo(String.format(
                        "Moving file %s to backup folder - side doesn't match!", f.getName()));
                    try {
                      FileUtils.moveFileToDirectory(f, new File(backupDirectory, f.getParent()),
                          true);
                    } catch (FileExistsException e2) {
                      Logger.logInfo(String.format("Not moving file %s!", f.getName()), e2);
                      f.delete();
                    }
                  } else {
                    Logger.logInfo(String.format("Deleting file %s - side doesn't match!",
                        f.getName()));
                    f.delete();
                  }
                }
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

  /**
   * Clears a folder of all files that are not in the ModPack, or not matching the needed side.
   *
   * @param target Directory to clear.
   * @param pack The {@link ModPack} to check against.
   * @param installedPack The currently installed {@link ModPack}.
   * @param backupDirectory The place to backup the cleared files to. No backup if <code>null</code>
   * @throws NullPointerException if pack or target <code>null</code>.
   */
  public static synchronized void clearFolder(final File target, final ModPack installedPack,
      final ModPack pack, final Side side, final File backupDirectory) throws IOException {
    checkNotNull(target);
    checkNotNull(pack);
    checkNotNull(installedPack);
    if (!target.exists()) {
      throw new FileNotFoundException(
          String.format("'%s' doesn't exist!", target.getAbsolutePath()));
    }
    if (!target.isDirectory()) {
      throw new IOException(String.format("'%s' is no valid directory!", target.getAbsolutePath()));
    }
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
    Parallel.ForEach<Entry<String, ModPackFile>, Void> p =
        new Parallel.ForEach<Entry<String, ModPackFile>, Void>(installedPack.getFiles().entrySet());
    try {
      p.withFixedThreads(OSUtils.getNumCores() * 2)
          .apply(new Parallel.F<Entry<String, ModPackFile>, Void>() {

            @Override
            public Void apply(Entry<String, ModPackFile> entry) {
              String path = entry.getKey();
              String hash = entry.getValue().getHash();
              try {
                boolean isInNewPack = false;
                ModPackFile found = pack.getFileByPath(path);
                if (found != null) {
                  isInNewPack = found.getHash().equals(hash);
                }
                boolean delete = false;
                File localFile = new File(target, path);

                if (!entry.getValue().sideMatches(side)) {
                  Logger.logDebug("Side doesn't match! Deleting.");
                  delete = true;
                }

                if (localFile.exists() && isInNewPack) {
                  if (!entry.getValue().hashMatches(localFile)) {
                    Logger.logDebug(String.format("%s hash doesn't match", path));
                    delete = true;
                  }
                } else if (localFile.exists()) {
                  Logger.logDebug(String.format("%s not in new pack!", path));
                  delete = true;
                }

                if (delete) {
                  if (doBackup) {
                    try {
                      FileUtils.moveFileToDirectory(localFile, new File(backupDirectory,
                          RelativePath.getRelativePath(target, new File(localFile.getParent()))),
                          true);
                    } catch (Exception e) {
                      localFile.delete();
                      Logger.logError(String.format("Deleting %s - error during backup", path), e);
                    }
                  } else {
                    localFile.delete();
                    Logger.logDebug(String.format("Deleting %s", path));
                  }
                }

              } catch (IOException e) {
                Logger.logError("Error during clearFolder", e);
              }
              return null;
            }
          }).values();
    } catch (Exception e) {
      Logger.logError("Parallel execution exception", e);
    }

  }
}
