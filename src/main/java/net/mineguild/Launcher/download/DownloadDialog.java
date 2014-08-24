package net.mineguild.Launcher.download;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DownloadDialog extends JDialog implements PropertyChangeListener {
  private final static String NON_THIN = "[^iIl1\\.,']";
  private static Image icon = null;
  private final int statusTextSize = 40;
  public JLabel status;
  public JLabel speedLabel;
  Boolean canceled = false;
  private JPanel contentPane;
  private JButton buttonCancel;
  private JProgressBar overall;
  private JProgressBar current;
  private List<DownloadInfo> info;
  private long totalFilesSize = 0;
  private AssetDownloader task;

  public DownloadDialog(List<DownloadInfo> info, String title) {
    try {
      icon = ImageIO.read(DownloadDialog.class.getResourceAsStream("/icon.png"));
      setIconImage(icon);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    this.info = info;
    setTitle(title);
    setContentPane(contentPane);
    char[] statusChars = new char[statusTextSize];
    Arrays.fill(statusChars, 'x');
    status.setText(String.copyValueOf(statusChars));
    setResizable(false);
    pack();
    setMinimumSize(getSize());
    setResizable(true);
    status.setText("Working...");
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

  public DownloadDialog(List<DownloadInfo> info, String title, long totalFilesSize) {
    this(info, title);
    this.totalFilesSize = totalFilesSize;
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

  public void start() {
    task = new AssetDownloader(info, totalFilesSize);
    task.addPropertyChangeListener(this);
    task.run();
    try {
      boolean success = task.get();
      if (success) {
        JOptionPane.showMessageDialog(this, "All files were successfully downloaded!", "Success!",
            JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(this, "Files are missing!", "Error!",
            JOptionPane.ERROR_MESSAGE);
      }
    } catch (Exception e) {
      JOptionPane
          .showMessageDialog(this, "Files are missing!", "Error!", JOptionPane.ERROR_MESSAGE);
      // e.printStackTrace();
    }
    setVisible(false);
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

  {
    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
   * call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null,
        0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    panel2.add(buttonCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
        null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    status = new JLabel();
    status.setText("Working...");
    panel3.add(status, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    overall = new JProgressBar();
    overall.setStringPainted(true);
    overall.setToolTipText("Overall Download Progress");
    panel4.add(overall, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    current = new JProgressBar();
    current.setIndeterminate(false);
    current.setStringPainted(true);
    current.setToolTipText("Current File Download Progress");
    panel5.add(current, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel6 = new JPanel();
    panel6.setLayout(new BorderLayout(0, 0));
    contentPane.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
            | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label1 = new JLabel();
    label1.setText("Current Speed:  ");
    panel6.add(label1, BorderLayout.WEST);
    speedLabel = new JLabel();
    speedLabel.setText("0 kb/s");
    panel6.add(speedLabel, BorderLayout.CENTER);
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }
}
