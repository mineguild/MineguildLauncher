package net.mineguild.Launcher.download;

import java.awt.BorderLayout;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class DownloadDialog extends JDialog implements PropertyChangeListener {

  private final static String NON_THIN = "[^iIl1\\.,']";
  private static Image icon = null;
  private final int statusTextSize = 40;
  public JLabel status;
  public JLabel speedLabel;
  Boolean canceled = false;
  private JButton buttonCancel;
  private JProgressBar overall;
  private JProgressBar current;
  private List<DownloadInfo> info;
  private long totalFilesSize = 0;
  private AssetDownloader task;
  
  {
    initGUI();
  }

  public DownloadDialog(List<DownloadInfo> info, String title) {
    this.setTitle(title);
    try {
      icon = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
      setIconImage(icon);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    this.info = info;
    char[] statusChars = new char[statusTextSize];
    Arrays.fill(statusChars, 'x');
    status.setText(String.copyValueOf(statusChars));
    setResizable(false);
    pack();
    setMinimumSize(getSize());
    status.setText(title);
    setResizable(true);
    setLocationRelativeTo(null);
    getRootPane().setDefaultButton(buttonCancel);
    buttonCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        canceled = true;
        task.cancel(true);
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

  private static int textWidth(String str) {
    return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
  }

  public static String ellipsize(String text, int max) {

    if (textWidth(text) <= max) {
      return text;
    }

    // Start by chopping off at the word before max
    // This is an over-approximation due to thin-characters...
    int end = text.lastIndexOf(' ', max - 3);

    // Just one long word. Chop it off.
    if (end == -1) {
      return text.substring(0, max - 3) + "...";
    }

    // Step forward as long as textWidth allows.
    int newEnd = end;
    do {
      end = newEnd;
      newEnd = text.indexOf(' ', end + 1);

      // No more spaces.
      if (newEnd == -1) {
        newEnd = text.length();
      }

    } while (textWidth(text.substring(0, newEnd) + "...") < max);

    return text.substring(0, end) + "...";
  }
  
  public void initGUI(){
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    // Add status label
    c.anchor = GridBagConstraints.LINE_START;
    c.gridx = 0;
    c.insets = new Insets(0, 4, 0, 0);
    c.gridy = 0;
    //c.fill = GridBagConstraints.HORIZONTAL;
    status = new JLabel();
    add(status, c);
    // Overall progressbar
    overall = new JProgressBar();
    overall.setToolTipText("Overall Progress");
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(overall, c);
    // Current progressbar
    current = new JProgressBar();
    current.setToolTipText("Current File Progress");
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridwidth = 2;
    //c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    //c.anchor = GridBagConstraints.CENTER;
    add(current, c);
    JPanel speedPanel = new JPanel();
    // Speed desc
    JLabel lbl = new JLabel("Current Speed:");
    speedLabel = new JLabel("0 kb/s");
    speedPanel.add(lbl);
    speedPanel.add(speedLabel);
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 3;
    c.anchor = GridBagConstraints.LINE_START;
    add(speedPanel, c);
    
    buttonCancel = new JButton("Cancel");
    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 3;
    c.anchor = GridBagConstraints.LINE_END;
    add(buttonCancel, c);
    
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
      String noName =
          String.format("Downloading  (%d of %d)", (int) info.get("currentFile"),
              (int) info.get("overallFiles"));
      int sizeLeft = statusTextSize - noName.length();
      String name = (String) info.get("fileName");
      if (name.length() > sizeLeft) {
        name = ellipsize(name, sizeLeft);
      }
      status.setText(String.format("Downloading %s (%d of %d)", name,
          (int) info.get("currentFile"), (int) info.get("overallFiles")));
    } else if (evt.getPropertyName().equals("note")) {
      status.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals("speed")) {
      speedLabel.setText(String.format("%.2f KB/s", (float) evt.getNewValue()));
    } else if (evt.getPropertyName().equals("current_inter")) {
      current.setIndeterminate(true);
      current.setStringPainted(false);
      current.setValue(0);
    }
  }

}
