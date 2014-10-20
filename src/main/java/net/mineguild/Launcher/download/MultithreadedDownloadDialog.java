package net.mineguild.Launcher.download;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import net.mineguild.Launcher.log.Logger;

import com.google.common.collect.Maps;

@SuppressWarnings("serial")
public class MultithreadedDownloadDialog extends JDialog implements PropertyChangeListener {

  public JLabel status;
  public JLabel speedLabel;
  Boolean canceled = false;
  private JButton buttonCancel;
  private JProgressBar overall;
  private List<DownloadInfo> info;
  private long totalFilesSize = 0;
  private AssetDownloader task;
  private JPanel mainPanel;
  private JScrollPane scrollPane;
  private JPanel progressPanel;
  private Map<String, JProgressBar> progressBarMap = Maps.newHashMap();

  {
    initGUI();
  }

  /**
   * @wbp.parser.constructor
   */
  public MultithreadedDownloadDialog(Frame parent, List<DownloadInfo> info, String title) {
    super(parent);
    setTitle(title);
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    this.info = info;
    pack();
    System.out.println(getSize().getWidth());
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
      public void windowClosing(WindowEvent e) {
        // super.windowClosed(e);
        canceled = true;
        System.out.println("Window closed");
        task.cancel(true);
      }
    });
  }

  public MultithreadedDownloadDialog(Frame parent, List<DownloadInfo> info, String title,
      long totalSize) {
    this(parent, info, title);
    this.totalFilesSize = totalSize;
  }

  public MultithreadedDownloadDialog(List<DownloadInfo> dlinfo, String title, Frame parent) {
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
    } catch (CancellationException e) {
      Logger.logError("Download was cancelled!", e);

    } catch (InterruptedException e) {
      Logger.logError("Download was interrupted!", e);
    } catch (ExecutionException e) {
      Logger.logError("Execution of Download failed!", e);
    } finally {
      dispose();
    }
    return false;
  }


  public void initGUI() {
    getContentPane().setLayout(new MigLayout("", "[284px,grow]", "[100px][100px,grow]"));
    mainPanel = new JPanel();
    getContentPane().add(mainPanel, "cell 0 0,grow");
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
    buttonPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(buttonPanel, "cell 0 1,grow");
    buttonCancel = new JButton("Cancel");
    buttonPanel.add(buttonCancel, BorderLayout.SOUTH);

    scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setAutoscrolls(true);
    progressPanel = new JPanel(new GridBagLayout());
    progressPanel.setBorder(null);
    // progressPanel.setPreferredSize(new Dimension(200, 200));
    scrollPane.setPreferredSize(new Dimension(150, 50));
    scrollPane.setViewportView(progressPanel);
    buttonPanel.add(scrollPane, BorderLayout.CENTER);

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("overall")) {
      int progress = (Integer) evt.getNewValue();
      if (overall.isIndeterminate()) {
        overall.setIndeterminate(false);
        overall.setStringPainted(true);
      }
      overall.setValue(progress);
    } else if (evt.getPropertyName().equals("info")) {
      @SuppressWarnings("unchecked")
      HashMap<String, Object> info = (HashMap<String, Object>) evt.getNewValue();
      status.setText(String.format("Downloading file %d of %d", (Integer) info.get("currentFile"),
          (Integer) info.get("overallFiles")));
    } else if (evt.getPropertyName().equals("note")) {
      status.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals("speed")) {
      speedLabel.setText((String) evt.getNewValue());
    } else if (evt.getPropertyName().equals("indProgress")) {
      Object[] data = (Object[]) evt.getNewValue();
      String threadId = (String) data[0];
      int progress = (Integer) data[1];
      progressBarMap.get(threadId).setValue(progress);
    } else if (evt.getPropertyName().equals("addIndProgress")) {
      Object[] data = (Object[]) evt.getNewValue();
      String threadId = (String) data[0];
      String[] splitName = ((String) data[1]).split("/");
      JProgressBar bar = new JProgressBar();
      bar.setStringPainted(true);
      bar.setString(splitName[splitName.length - 1]);
      progressBarMap.put(threadId, bar);
      drawProgessBars();
    } else if (evt.getPropertyName().equals("removeIndProgress")) {
      String threadId = (String) evt.getNewValue();
      progressBarMap.remove(threadId);
      drawProgessBars();
    } else if (evt.getPropertyName().equals("overallIndeterminate")) {
      overall.setIndeterminate(true);
      overall.setStringPainted(false);
    }
  }

  void drawProgessBars() {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    progressPanel.removeAll();
    for (JProgressBar progressBar : progressBarMap.values()) {
      c.gridy++;
      progressPanel.add(progressBar, c);
    }
    revalidate();
    repaint();
  }

}
