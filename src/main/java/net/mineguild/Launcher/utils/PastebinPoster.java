package net.mineguild.Launcher.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.log.Logger;

public class PastebinPoster extends SwingWorker<Boolean, Void> {

  String postContent;


  public PastebinPoster(String postContent) {
    this.postContent = postContent;
  }

  @Override
  protected Boolean doInBackground() throws Exception {
    try {
      String body =
          "api_option=paste&api_dev_key=" + URLEncoder.encode(Constants.PASTEBIN_API_KEY, "utf-8")
              + "&api_paste_code=" + URLEncoder.encode(postContent, "utf-8")
              + "&api_paste_private=1" + "&api_paste_name=MineguildLauncher-Log";
      URL url = new URL(Constants.PASTEBIN_POST_URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setUseCaches(false);
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(body);
      writer.flush();

      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));

      String line = reader.readLine();


      writer.close();
      reader.close();

      Desktop d = Desktop.getDesktop();
      d.browse(new URI(line));

    } catch (Exception e) {
      Logger.logError("Error during Pastebin-Post!", e);
    }
    return false;
  }

}
