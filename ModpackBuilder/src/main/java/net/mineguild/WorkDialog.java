package net.mineguild;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

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
        new FileAddWorker(FileUtils.listFiles(targetModpack.getBasePath(), FileFilterUtils.and(
            FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")),
            FileFilterUtils.sizeFileFilter(1l, true)), FileFilterUtils.trueFileFilter()));
    worker.addPropertyChangeListener(this);
    this.targetModpack = targetModpack;
    worker.execute();
    setVisible(true);
    dispose();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      bar.setValue((int) event.getNewValue());
    } else if (event.getPropertyName().equals("done")) {
      try {
        targetModpack.addModpackFiles(worker.get());
      } catch (InterruptedException | ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dispose();
    }
  }


}
