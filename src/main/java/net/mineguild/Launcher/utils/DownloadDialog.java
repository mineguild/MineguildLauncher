package net.mineguild.Launcher.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

public class DownloadDialog extends JDialog implements PropertyChangeListener {
    private JPanel contentPane;
    private JButton buttonCancel;
    public JLabel status;
    private JProgressBar overall;
    private JProgressBar current;
    public JLabel speedLabel;
    Boolean canceled = false;
    private HashMap<String, File> url_dest;

    public DownloadDialog(HashMap<String, File> url_dest, String title) {
        setContentPane(contentPane);

        this.url_dest = url_dest;
        setResizable(false);
        pack();
        setTitle(title);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(buttonCancel);
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canceled = true;
                setVisible(false);
            }
        });
    }

    public void start() {
        DownloadTask task = new DownloadTask(this, url_dest);
        task.addPropertyChangeListener(this);
        task.run();
    }

    public static void main(String[] args) {
        HashMap<String, File> test = new HashMap<>();
        test.put("https://mineguild.net/uploadscript/uploads/AC2.mp4", new File("AC2.mp4"));
        test.put("https://mineguild.net/uploadscript/uploads/BAnzServiceDevSpace.zip", new File("BAnzServiceDevSpace.zip"));
        DownloadDialog dialog = new DownloadDialog(test, "Test");
        dialog.setVisible(true);
        dialog.start();
        System.exit(0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            current.setValue(progress);
        } else if (evt.getPropertyName().equals("overall")) {
            int progress = (Integer) evt.getNewValue();
            overall.setValue(progress);
        } else if (evt.getPropertyName().equals("info")) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> info = (HashMap<String, Object>) evt.getNewValue();
            status.setText(String.format("Downloading %s (%d of %d)", (String) info.get("fileName"),
                    (int) info.get("currentFile"), (int) info.get("overallFiles")));
            pack();
        } else if (evt.getPropertyName().equals("speed")) {
            speedLabel.setText(String.format("%.2f KB/s", (float) evt.getNewValue()));
        }
    }
}
