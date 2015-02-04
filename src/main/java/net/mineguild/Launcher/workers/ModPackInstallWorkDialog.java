package net.mineguild.Launcher.workers;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.mineguild.Launcher.download.DownloadInfo;

@SuppressWarnings("serial")
public class ModPackInstallWorkDialog extends JDialog implements PropertyChangeListener {

  public static ModPackInstallWorkDialog instance;
  private JProgressBar bar;
  private ModPackInstallWorker worker;
  private List<DownloadInfo> result;
  private JLabel label;

  public ModPackInstallWorkDialog(JFrame owner) {
    super(owner, ModalityType.APPLICATION_MODAL);
    instance = this;
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    getContentPane().add(panel, BorderLayout.CENTER);
    panel.setLayout(new BorderLayout(0, 0));
    label = new JLabel("Working...");
    panel.add(label, BorderLayout.NORTH);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    bar = new JProgressBar();
    panel.add(bar, BorderLayout.SOUTH);
    bar.setIndeterminate(true);
    pack();
    setModal(true);
    setLocationRelativeTo(null);
  }

  public void start(ModPackInstallWorker worker) {
    this.worker = worker;
    worker.addPropertyChangeListener(this);
    worker.execute();
    setVisible(true);
    dispose();
  }

  public List<DownloadInfo> getResult() {
    return result;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      bar.setValue((Integer) event.getNewValue());
    } else if (event.getPropertyName().equals("status")) {
      this.label.setText((String) event.getNewValue());
    } else if (event.getPropertyName().equals("done")) {
      try {
        this.result = worker.get();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dispose();
    }
  }


}
