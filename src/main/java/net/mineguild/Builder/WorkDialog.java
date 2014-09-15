package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.X_Modpack;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class WorkDialog extends JDialog implements PropertyChangeListener {

  public static WorkDialog instance;
  private JProgressBar bar;
  private X_Modpack targetModpack;
  private FileAddWorker worker;

  public WorkDialog(JFrame owner) {
    super(owner, ModalityType.APPLICATION_MODAL);
    setUndecorated(true);
    instance = this;
    JLabel label = new JLabel("Adding files...");
    bar = new JProgressBar();
    bar.setStringPainted(true);
    add(label, BorderLayout.NORTH);
    add(bar, BorderLayout.EAST);
    pack();
    setModal(true);
    setLocationRelativeTo(null);
  }

  public void start(final X_Modpack targetModpack) {
    Collection<File> fileList =
        FileUtils.listFiles(new File(targetModpack.getBasePath(), "mods"),
            Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER);
    fileList.addAll(FileUtils.listFiles(new File(targetModpack.getBasePath(), "config"),
        Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER));
    worker = new FileAddWorker(fileList);
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
        targetModpack.addModpackFiles(worker.get());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dispose();
    }
  }


}
