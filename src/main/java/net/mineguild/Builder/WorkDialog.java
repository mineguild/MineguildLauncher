package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.mineguild.Launcher.Constants;
import net.mineguild.ModPack.Mod;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class WorkDialog extends JDialog implements PropertyChangeListener {

  public static WorkDialog instance;
  private JProgressBar bar;
  private ModPack targetModpack;
  private FileAddWorker worker;
  private boolean mods;

  public WorkDialog(JFrame owner, boolean mods) {
    super(owner, ModalityType.APPLICATION_MODAL);
    instance = this;
    this.mods = mods;
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    JLabel label = new JLabel("Adding files...");
    bar = new JProgressBar();
    bar.setStringPainted(true);
    add(label, BorderLayout.NORTH);
    add(bar, BorderLayout.EAST);
    pack();
    setModal(true);
    setLocationRelativeTo(null);
  }

  public void start(final ModPack targetModpack) {
    Collection<File> fileList =
        FileUtils.listFiles(new File(ModpackBuilder.modpackDirectory, "mods"),
            Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER);
    fileList.addAll(FileUtils.listFiles(new File(ModpackBuilder.modpackDirectory, "config"),
        Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER));
    if(mods){
      worker = new FileAddWorker<Mod>(fileList, ModpackBuilder.modpackDirectory, mods);
    } else {
      worker = new FileAddWorker<ModPackFile>(fileList, ModpackBuilder.modpackDirectory, mods);
    }
    worker.addPropertyChangeListener(this);
    this.targetModpack = targetModpack;
    worker.execute();
    setVisible(true);
    dispose();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      bar.setValue((Integer) event.getNewValue());
    } else if (event.getPropertyName().equals("done")) {
      try {
        if(mods){
          targetModpack.getMods().putAll((Map<? extends String, ? extends Mod>) worker.get());
        } else {
          targetModpack.getOther().putAll((Map<? extends String, ? extends ModPackFile>) worker.get());
        }
      } catch (Exception e) {
      }
      dispose();
    }
  }


}
