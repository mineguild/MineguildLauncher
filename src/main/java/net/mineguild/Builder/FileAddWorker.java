package net.mineguild.Builder;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.ChecksumUtil.ModPackEntry;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.ModPack.Mod;
import net.mineguild.ModPack.ModInfo;
import net.mineguild.ModPack.ModPackFile;

import com.google.common.collect.Maps;


public class FileAddWorker extends SwingWorker<Map<String, ModPackFile>, Void> {

  @Override
  protected void done() {
    super.done();
    firePropertyChange("done", null, true);
  }

  private Collection<File> files;
  private float progressPerFile;
  private File baseDirectory;
  private int progress;

  public FileAddWorker(Collection<File> files, File baseDirectory) {
    this.files = files;
    this.baseDirectory = baseDirectory;
    this.progressPerFile = 100f / files.size();
  }

  public void updateProgress() {
    setProgress((int) (progressPerFile * ++progress));
  }


  @Override
  protected Map<String, ModPackFile> doInBackground() throws Exception {
    Collection<ModPackEntry> result =
        new Parallel.ForEach<File, ModPackEntry>(files).withFixedThreads(OSUtils.getNumCores() * 2)
            .apply(new Parallel.F<File, ModPackEntry>() {

              @Override
              public ModPackEntry apply(File e) {
                try {
                  ModPackEntry entry = ChecksumUtil.getFile(baseDirectory, e);
                  if (entry.getKey().endsWith(".jar") || entry.getKey().endsWith(".zip")) {
                    List<ModInfo> modInfo = JsonFactory.loadModInfoFromJar(e);
                    if (modInfo != null) {
                      if (modInfo.size() > 0) {
                        ((Mod) entry.getValue()).setInfo(modInfo.get(0));
                      }
                    }
                  }
                  updateProgress();
                  return entry;
                } catch (Exception e1) {
                  Logger.logError("Unable to add file!", e1);
                }
                return null;
              }
            }).values();
    Map<String, ModPackFile> ret = Maps.newTreeMap();
    for (ModPackEntry entry : result) {
      ret.put(entry.getKey(), entry.getValue());
    }
    return ret;
  }


}
