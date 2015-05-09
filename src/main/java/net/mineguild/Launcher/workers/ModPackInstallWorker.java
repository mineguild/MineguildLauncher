package net.mineguild.Launcher.workers;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackInstaller;
import net.mineguild.ModPack.Side;

public class ModPackInstallWorker extends SwingWorker<List<DownloadInfo>, Void> {

  private InstallAction action;
  private ModPack remotePack;
  private ModPack localPack;
  private File instancePath;
  private File backupDirectory;

  public ModPackInstallWorker(ModPack remotePack, ModPack localPack, File instancePath,
      File backupDirectory, InstallAction action) {
    this.action = action;
    this.remotePack = remotePack;
    this.instancePath = instancePath;
    this.localPack = localPack;
    this.backupDirectory = backupDirectory;
  }

  @Override
  protected List<DownloadInfo> doInBackground() throws Exception {
    List<DownloadInfo> dlinfo = null;
    File modsDir = new File(instancePath, "mods");
    switch (action) {
      case CLEAR_FORCE:
        firePropertyChange("status", null, "Clearing mods folder...");
        ModPackInstaller.clearFolder(instancePath, remotePack, Side.CLIENT, backupDirectory);
        break;
      case CLEAR:
        firePropertyChange("status", null, "Clearing whole instance folder...");
        ModPackInstaller.clearFolder(modsDir, localPack, remotePack, Side.CLIENT, backupDirectory);
        break;
      case CHECK:
        firePropertyChange("status", null, "Checking for needed files...");
        dlinfo = ModPackInstaller.checkNeededFiles(instancePath, remotePack, Side.CLIENT);
        break;
      default:
        break;

    }
    return dlinfo;
  }

  @Override
  protected void done() {
    firePropertyChange("done", null, null);
  }

  public static enum InstallAction {
    CLEAR_FORCE, CLEAR, CHECK
  }

}
