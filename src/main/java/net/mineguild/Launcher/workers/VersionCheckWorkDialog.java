package net.mineguild.Launcher.workers;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;

import net.mineguild.Launcher.LaunchFrame;

@SuppressWarnings("serial")
public class VersionCheckWorkDialog extends JDialog implements PropertyChangeListener {

  public static VersionCheckWorkDialog instance;
  private LaunchFrame frame;
  // private JProgressBar bar;
  private VersionCheckWorker worker;

  public VersionCheckWorkDialog(LaunchFrame owner) {
    super(owner, ModalityType.APPLICATION_MODAL);
    this.frame = owner;
    instance = this;
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    JLabel label = new JLabel("Version check running...");
    // bar = new JProgressBar();
    // bar.setStringPainted(true);
    add(label, BorderLayout.NORTH);
    // add(bar, BorderLayout.EAST);
    pack();
    setSize(150, 70);
    setModal(true);
    setLocationRelativeTo(owner);
  }

  public void start() {
    worker = new VersionCheckWorker(frame);
    worker.addPropertyChangeListener(this);
    worker.execute();
    setVisible(true);
    dispose();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      // bar.setValue((Integer) event.getNewValue());
    } else if (event.getPropertyName().equals("done")) {
      dispose();
    }
  }


}
