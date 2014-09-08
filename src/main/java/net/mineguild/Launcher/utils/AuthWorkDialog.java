package net.mineguild.Launcher.utils;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.mineguild.Launcher.Constants;

@SuppressWarnings("serial")
public class AuthWorkDialog extends JDialog implements PropertyChangeListener {

  public static AuthWorkDialog instance;
  private AuthlibDLer worker;
  private JProgressBar bar;
  private JLabel label;

  public AuthWorkDialog(JFrame owner) {
    super(owner, ModalityType.APPLICATION_MODAL);
    instance = this;
    label = new JLabel("DLing Authlib");
    add(label, BorderLayout.NORTH);
    JProgressBar bar = new JProgressBar();
    bar.setStringPainted(true);
    add(bar, BorderLayout.SOUTH);
    pack();
    setModal(false);
    setLocationRelativeTo(null);
  }

  public boolean start() throws InterruptedException {
    worker =
        new AuthlibDLer(new File(OSUtils.getLocalDir(), "libs").getAbsolutePath(),
            Constants.AUTHLIB_VERSION);
    worker.addPropertyChangeListener(this);
    worker.execute();
    Thread.sleep(1000);
    setVisible(true);
    try {
      boolean result = worker.get();
      dispose();
      return result;
    } catch (Exception e) {
      dispose();
      return false;
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      bar.setValue((Integer) event.getNewValue());
    } else if (event.getPropertyName().equals("status")) {
      label.setText((String) event.getNewValue());
    }
  }

}
