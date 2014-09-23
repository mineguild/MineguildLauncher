package net.mineguild.Launcher.download;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mineguild.Launcher.log.Logger;

@SuppressWarnings("serial")
public class MultithreadDownloadDialog extends JDialog implements PropertyChangeListener {

  private static Image icon = null;
  public JLabel status;
  public JLabel speedLabel;
  Boolean canceled = false;
  private JButton buttonCancel;
  private JProgressBar overall;
  private List<DownloadInfo> info;
  private long totalFilesSize = 0;
  private AssetDownloader task;
  private JPanel mainPanel;

  {
    initGUI();
  }

  /**
   * @wbp.parser.constructor
   */
  public MultithreadDownloadDialog(Frame parent, List<DownloadInfo> info, String title) {
    super(parent);
    this.setTitle(title);
    try {
      icon = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
      setIconImage(icon);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    this.info = info;
    pack();
    setMinimumSize(new Dimension(300, getHeight()));
    setLocationRelativeTo(null);
    getRootPane().setDefaultButton(buttonCancel);
    buttonCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        canceled = true;
        task.cancel(false);
        dispose();
      }
    });
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        canceled = true;
        task.cancel(false);
      }
    });
  }

  public MultithreadDownloadDialog(Frame parent, List<DownloadInfo> info, String title,
      long totalSize) {
    this(parent, info, title);
    this.totalFilesSize = totalSize;
  }

  public MultithreadDownloadDialog(List<DownloadInfo> dlinfo, String title, Frame parent) {
    this(parent, dlinfo, title);
    for (DownloadInfo info : dlinfo) {
      totalFilesSize += info.size;
    }
  }

  public boolean run() {
    task = new AssetDownloader(info, totalFilesSize);
    task.setMultithread(true);
    task.addPropertyChangeListener(this);
    task.run();
    try {
      boolean success = task.get();
      dispose();
      return success;
    } catch (Exception e) {
      Logger.logError("Download didnt work!", e);
      e.printStackTrace();
      dispose();
      return false;
    }
  }


  public void initGUI() {
    mainPanel = new JPanel();
    getContentPane().add(mainPanel, BorderLayout.CENTER);
    GridBagLayout gbl_mainPanel = new GridBagLayout();
    gbl_mainPanel.columnWeights = new double[] {1.0, 0.0};
    gbl_mainPanel.rowWeights = new double[] {0.0, 0.0, 0.0};
    mainPanel.setLayout(gbl_mainPanel);
    // c.fill = GridBagConstraints.HORIZONTAL;
    status = new JLabel("Current Status");
    GridBagConstraints gbc_status = new GridBagConstraints();
    gbc_status.anchor = GridBagConstraints.WEST;
    gbc_status.gridx = 0;
    gbc_status.gridy = 0;
    mainPanel.add(status, gbc_status);
    // Overall progressbar
    overall = new JProgressBar();
    overall.setStringPainted(true);
    GridBagConstraints gbc_overall = new GridBagConstraints();
    gbc_overall.fill = GridBagConstraints.HORIZONTAL;
    gbc_overall.gridwidth = 2;
    gbc_overall.insets = new Insets(0, 0, 5, 0);
    gbc_overall.gridx = 0;
    gbc_overall.gridy = 1;
    mainPanel.add(overall, gbc_overall);
    overall.setToolTipText("Overall Progress");
    JPanel speedPanel = new JPanel();
    GridBagConstraints gbc_speedPanel = new GridBagConstraints();
    gbc_speedPanel.anchor = GridBagConstraints.WEST;
    gbc_speedPanel.insets = new Insets(0, 0, 0, 5);
    gbc_speedPanel.gridx = 0;
    gbc_speedPanel.gridy = 2;
    mainPanel.add(speedPanel, gbc_speedPanel);
    // Speed desc
    JLabel lbl = new JLabel("Current Speed:");
    speedLabel = new JLabel("0 kb/s");
    speedPanel.add(lbl);
    speedPanel.add(speedLabel);

    JPanel buttonPanel = new JPanel();
    buttonCancel = new JButton("Cancel");
    buttonPanel.add(buttonCancel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("overall")) {
      int progress = (Integer) evt.getNewValue();
      overall.setValue(progress);
    } else if (evt.getPropertyName().equals("info")) {
      @SuppressWarnings("unchecked")
      HashMap<String, Object> info = (HashMap<String, Object>) evt.getNewValue();
      status.setText(String.format("Downloading file %d of %d", (Integer) info.get("currentFile"),
          (Integer) info.get("overallFiles")));
    } else if (evt.getPropertyName().equals("note")) {
      status.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals("speed")) {
      speedLabel.setText(String.format("%.2f KB/s", (Double) evt.getNewValue()));
    }
  }
}
