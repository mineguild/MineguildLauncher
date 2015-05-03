package net.mineguild.Launcher.workers;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.LaunchFrame;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.ModPack.ModpackRepository;
import net.mineguild.ModPack.ModpackRepository.VersionRepository;

public class VersionCheckWorker extends SwingWorker<Void, Void> {

  private LaunchFrame frame;

  public VersionCheckWorker(LaunchFrame frame) {
    this.frame = frame;

  }

  @Override
  protected Void doInBackground() throws Exception {
    MineguildLauncher.getRepositories().clear();
    frame.setIgnoreEvents(true);
    VersionRepository select = null;
    for (String repoUrl : MineguildLauncher.getSettings().getRepositories()) {
      try {
        frame.getModpackSelection().removeAllItems();
        ModpackRepository updated = JsonFactory.loadRepository(repoUrl);
        MineguildLauncher.getRepositories().add(updated);
        for (ModpackRepository mRepo : MineguildLauncher.getRepositories()) {
          for (VersionRepository repo : mRepo.getPacks().values()) {

            frame.getModpackSelection().addItem(repo);
          }
          if (mRepo.getPacks().containsKey(MineguildLauncher.getSettings().getLastPack())) {
            select = mRepo.getPacks().get(MineguildLauncher.getSettings().getLastPack());
            frame.getModpackSelection().setSelectedItem(
                mRepo.getPacks().get(MineguildLauncher.getSettings().getLastPack()));
          }
        }
      } catch (Exception e) { // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    frame.setIgnoreEvents(false);
    frame.updateGUI(true);
    frame.getModpackSelection().setSelectedItem(select);
    return null;
  }

  @Override
  protected void done() {
    firePropertyChange("done", null, null);
    super.done();
  }



}
