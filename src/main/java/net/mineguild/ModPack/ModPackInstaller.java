package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;

public class ModPackInstaller {
  
  public static long totalSize = 0;

  public static synchronized List<DownloadInfo> checkNeededFiles(final File installDirectory,
      Set<ModPackFile> files, final Side side) throws Exception {
    checkNotNull(installDirectory);
    totalSize = 0l;
    List<DownloadInfo> result =
        (List<DownloadInfo>) new Parallel.ForEach<ModPackFile, DownloadInfo>(files)
            .withFixedThreads(OSUtils.getNumCores() * 2)
            .apply(new Parallel.F<ModPackFile, DownloadInfo>() {

              @Override
              public DownloadInfo apply(ModPackFile packFile) {
                if (packFile.getSide() == side || packFile.getSide() == Side.UNIVERSAL) {
                  File localFile = packFile.getFile(installDirectory);
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
                    DownloadInfo ret = localFile.exists() ? null : new DownloadInfo(new URL(
                        Constants.MG_GET_SCRIPT + "?data=" + packFile.getHash()), localFile,
                        packFile.getName(), Lists.newArrayList(packFile.getHash()), "md5",
                        DLType.ContentMD5, DLType.NONE);
                    totalSize += packFile.getSize();
                    ret.size = packFile.getSize();
                    return ret;
                  } catch (MalformedURLException e) {
                    Logger.logError("Couldn't process url!", e);
                  }
                }
                return null;
              }

            }).values();
    return result;
  }

}
