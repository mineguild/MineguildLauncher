package net.mineguild.Launcher.download;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicLabelUI;

public class DownloadDialog extends JDialog implements PropertyChangeListener {

  private static Image icon = null;
  private final int statusTextSize = 40;
  public JLabel status;
  public JLabel fileName;
  public JLabel speedLabel;
  Boolean canceled = false;
  private JButton buttonCancel;
  private JProgressBar overall;
  private JProgressBar current;
  private List<DownloadInfo> info;
  private long totalFilesSize = 0;
  private AssetDownloader task;
  private static final MyLabelUI myUI = new MyLabelUI();
  private JPanel mainPanel;

  {
    initGUI();
  }

  /**
   * @wbp.parser.constructor
   */
  public DownloadDialog(List<DownloadInfo> info, String title) {
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
        task.cancel(true);
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

  public DownloadDialog(List<DownloadInfo> info, String title, long totalSize) {
    this(info, title);
    this.totalFilesSize = totalSize;
  }

  public boolean start() {
    task = new AssetDownloader(info, totalFilesSize);
    task.addPropertyChangeListener(this);
    task.run();
    try {
      boolean success = task.get();
      /*
       * if (success) { JOptionPane.showMessageDialog(this,
       * "All files were successfully downloaded!", "Success!", JOptionPane.INFORMATION_MESSAGE); }
       * else { JOptionPane.showMessageDialog(this, "Files are missing!", "Error!",
       * JOptionPane.ERROR_MESSAGE); }
       */
      return success;
    } catch (Exception e) {
      // JOptionPane
      // .showMessageDialog(this, "Files are missing!", "Error!", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return false;
    }
  }


  public void initGUI() {  
    mainPanel = new JPanel();
    getContentPane().add(mainPanel, BorderLayout.CENTER);
    GridBagLayout gbl_mainPanel = new GridBagLayout();
    gbl_mainPanel.columnWeights = new double[]{1.0, 0.0};
    gbl_mainPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
    mainPanel.setLayout(gbl_mainPanel);
    // c.fill = GridBagConstraints.HORIZONTAL;
    status = new JLabel("Current Status");
    GridBagConstraints gbc_status = new GridBagConstraints();
    gbc_status.anchor = GridBagConstraints.WEST;
    gbc_status.gridx = 0;
    gbc_status.gridy = 0;
    mainPanel.add(status, gbc_status);
    fileName = new JLabel("Current File");
    GridBagConstraints gbc_fileName = new GridBagConstraints();
    gbc_fileName.fill = GridBagConstraints.BOTH;
    gbc_fileName.gridx = 0;
    gbc_fileName.gridy = 1;
    mainPanel.add(fileName, gbc_fileName);
    fileName.setUI(myUI);
    // Overall progressbar
    overall = new JProgressBar();
    GridBagConstraints gbc_overall = new GridBagConstraints();
    gbc_overall.fill = GridBagConstraints.HORIZONTAL;
    gbc_overall.gridwidth = 2;
    gbc_overall.insets = new Insets(0, 0, 5, 0);
    gbc_overall.gridx = 0;
    gbc_overall.gridy = 2;
    mainPanel.add(overall, gbc_overall);
    overall.setToolTipText("Overall Progress");
    // Current progressbar
    current = new JProgressBar();
    GridBagConstraints gbc_current = new GridBagConstraints();
    gbc_current.fill = GridBagConstraints.HORIZONTAL;
    gbc_current.gridwidth = 2;
    gbc_current.insets = new Insets(0, 0, 5, 0);
    gbc_current.gridx = 0;
    gbc_current.gridy = 3;
    mainPanel.add(current, gbc_current);
    current.setToolTipText("Current File Progress");
    JPanel speedPanel = new JPanel();
    GridBagConstraints gbc_speedPanel = new GridBagConstraints();
    gbc_speedPanel.anchor = GridBagConstraints.WEST;
    gbc_speedPanel.insets = new Insets(0, 0, 0, 5);
    gbc_speedPanel.gridx = 0;
    gbc_speedPanel.gridy = 4;
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
    if (evt.getPropertyName().equals("progress")) {
      int progress = (Integer) evt.getNewValue();
      if (current.isIndeterminate()) {
        current.setIndeterminate(false);
        current.setStringPainted(true);
      }
      current.setValue(progress);
    } else if (evt.getPropertyName().equals("overall")) {
      int progress = (Integer) evt.getNewValue();
      overall.setValue(progress);
    } else if (evt.getPropertyName().equals("info")) {
      @SuppressWarnings("unchecked")
      HashMap<String, Object> info = (HashMap<String, Object>) evt.getNewValue();
      status.setText(String.format("Downloading file (%d of %d)",
          (Integer) info.get("currentFile"), (Integer) info.get("overallFiles")));
      fileName.setText((String) info.get("fileName"));
    } else if (evt.getPropertyName().equals("note")) {
      status.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals("speed")) {
      speedLabel.setText(String.format("%.2f KB/s", (Float) evt.getNewValue()));
    } else if (evt.getPropertyName().equals("current_inter")) {
      current.setIndeterminate(true);
      current.setStringPainted(false);
      current.setValue(0);
    }
  }

  private static class MyLabelUI extends BasicLabelUI {
    @Override
    protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon,
        Rectangle viewR, Rectangle iconR, Rectangle textR) {
      String s = super.layoutCL(label, fontMetrics, text, icon, viewR, iconR, textR);
      return s;
    }
  }

}
