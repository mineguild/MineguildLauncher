package net.mineguild.Launcher.utils;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
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
        // Create a new trust manager that trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Activate the new trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        DownloadTask task = new DownloadTask(this, url_dest);
        task.addPropertyChangeListener(this);
        task.run();
    }

    public static void main(String[] args) {
        HashMap<String, File> test = new HashMap<String, File>();
        test.put("https://mineguild.net/uploadscript/uploads/AC2.mp4", new File("AC2.mp4"));
        test.put("https://mineguild.net/uploadscript/uploads/BAnzServiceDevSpace.zip", new File("BAnzServiceDevSpace.zip"));
        DownloadDialog dialog = new DownloadDialog(test, "Test");

        dialog.pack();
        dialog.setVisible(true);
        dialog.start();
        System.exit(0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            current.setValue(progress);
        } else if(evt.getPropertyName().equals("overall")){
            int progress = (Integer) evt.getNewValue();
            overall.setValue(progress);
        }
    }
}
