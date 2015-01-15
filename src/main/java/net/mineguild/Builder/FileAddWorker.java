package net.mineguild.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.ChecksumUtil.Entry;
import net.mineguild.Launcher.utils.ChecksumUtil.ModPackEntry;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.ModPack.Mod;
import net.mineguild.ModPack.ModInfo;
import net.mineguild.ModPack.ModPackFile;

import com.google.common.collect.Maps;


public class FileAddWorker<T> extends SwingWorker<Map<String, T>, Void> {

  @Override
  protected void done() {
    super.done();
    firePropertyChange("done", null, true);
  }

  private Collection<File> files;
  private float progressPerFile;
  private File baseDirectory;
  private boolean mods;
  private int progress;

  public FileAddWorker(Collection<File> files, File baseDirectory, boolean mods) {
    this.files = files;
    this.baseDirectory = baseDirectory;
    this.progressPerFile = 100f / files.size();
    this.mods = mods;
  }

  public void updateProgress() {
    setProgress((int) (progressPerFile * ++progress));
  }


  @Override
  protected Map<String, T> doInBackground() throws Exception {
    Collection<Entry<T>> result =
        new Parallel.ForEach<File, Entry<T>>(files).withFixedThreads(OSUtils.getNumCores() * 2)
            .apply(new Parallel.F<File, Entry<T>>() {

              @Override
              public Entry<T> apply(File e) {
                try {
                  ModPackEntry entry = ChecksumUtil.getFile(baseDirectory, e);
                  if (mods) {
                    if (entry.getKey().endsWith(".jar") || entry.getKey().endsWith(".zip")) {
                      List<ModInfo> modInfo = JsonFactory.loadModInfoFromJar(e);
                      if (modInfo != null) {
                        Entry<Mod> ret =
                                new Entry<Mod>(entry.getKey(), Mod.fromModPackFile(entry.getValue()));
                        ret.getValue().setInfo(modInfo.get(0));
                        updateProgress();
                        return (Entry<T>) ret;
                      }
                    }
                    updateProgress();
                    return null;
                  } else {
                    Entry<ModPackFile> ret =
                            new Entry<ModPackFile>(entry.getKey(), entry.getValue());
                    if (entry.getKey().endsWith(".jar") || entry.getKey().endsWith(".zip")) {
                      try {
                        List<ModInfo> modInfo = JsonFactory.loadModInfoFromJar(e);
                        if (modInfo != null) {
                          return null;
                        }
                      } catch (IOException e2){
                        Logger.logError("ModJar check failed!", e2);
                      }
                    }
                    updateProgress();
                    return (Entry<T>) ret;
                  }


                } catch (Exception e1) {
                  Logger.logError("Unable to add file!", e1);
                }
                updateProgress();
                return null;
              }
            }).values();
                Map<String, T> ret = Maps.newTreeMap();
                for (Entry<T> entry : result) {
                  ret.put(entry.getKey(), entry.getValue());
                }
                return ret;
              }


            }
