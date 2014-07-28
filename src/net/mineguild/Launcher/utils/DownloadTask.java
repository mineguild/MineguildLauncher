package net.mineguild.Launcher.utils;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadTask extends SwingWorker<Void, Void> {
    private static final int BUFFER_SIZE = 1024;
    private HashMap<String, File> url_file;
    private DownloadDialog gui;

    public DownloadTask(DownloadDialog gui, HashMap<String, File> url_file){
        this.gui = gui;
        this.url_file = url_file;
    }

    @Override
    protected Void doInBackground() throws Exception {
        List<HTTPDownloadUtil> utils = HTTPDownloadUtil.prepareHashMap(url_file);
        long totalSize = HTTPDownloadUtil.calcSize(utils);
        long overallRead = 0;
        int overallFiles = utils.size();
        int overallPercentCompleted = 0;
        int currentFile = 1;
        for(HTTPDownloadUtil util : utils){
            try {

                String saveFilePath = util.getFilePath();

                InputStream inputStream = util.getInputStream();
                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                byte[] buffer = new byte[BUFFER_SIZE];
                long totalBytesRead = 0;
                int percentCompleted;
                long fileSize = util.getContentLength();
                long lastTime = System.currentTimeMillis();
                float lastFileSize = 0;
                float speed = 0;
                int tryNum = 0;
                int bytesRead;

                while (!gui.canceled) {
                    try {
                        if (!((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) >= 0)) {
                            break;
                        }
                    } catch (IOException e){
                        tryNum++;
                        if (tryNum > 5) {
                            outputStream.close();
                            util.disconnect();
                            throw e;
                        }
                        continue;
                    }
                    if (System.currentTimeMillis() - lastTime > 1000) {
                        speed = ((totalBytesRead - lastFileSize) / BUFFER_SIZE) / (System.currentTimeMillis() - lastTime) * 1000;
                        lastTime = System.currentTimeMillis();
                        lastFileSize = totalBytesRead;
                    }
                    gui.status.setText(String.format("Downloading %s (%d of %d)", util.getFileName(), currentFile, overallFiles));
                    gui.speedLabel.setText(String.format("%.2f KB/s", speed));
                    gui.pack();


                    totalBytesRead += bytesRead;
                    overallRead += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);
                    percentCompleted = (int) ((totalBytesRead * 100) / fileSize);
                    setProgress(percentCompleted);
                    if (totalSize > 0) {
                        overallPercentCompleted = (int) ((overallRead * 100) / totalSize);
                    }
                    firePropertyChange("overall", null, overallPercentCompleted);
                }

                outputStream.close();

                util.disconnect();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui, "Error downloading file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                setProgress(0);
                cancel(true);
            }
            currentFile++;
        }

        return null;
    }
}
