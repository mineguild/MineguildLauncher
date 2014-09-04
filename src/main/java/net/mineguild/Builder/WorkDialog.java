package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.Modpack;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class WorkDialog extends JDialog implements PropertyChangeListener {

  public static WorkDialog instance;
  private JProgressBar bar;
  private Modpack targetModpack;
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

  public void start(final Modpack targetModpack) {
    worker =
        new FileAddWorker(FileUtils.listFiles(targetModpack.getBasePath(),
            Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER));
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
