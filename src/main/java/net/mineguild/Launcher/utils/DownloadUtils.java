package net.mineguild.Launcher.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.download.AssetDownloader;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.log.Logger;

import org.apache.commons.io.IOUtils;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;

@SuppressWarnings("unused")
public class DownloadUtils {

  public static String fileHash(File file, String type) throws IOException {
    if (!file.exists()) {
      return "";
    }
    if (type.equalsIgnoreCase("md5")) {
      return ChecksumUtil.getMD5(file);
    }
    if (type.equalsIgnoreCase("sha1")) {
      return ChecksumUtil.getSHA(file);
    }
    URL fileUrl = file.toURI().toURL();
    MessageDigest dgest = null;
    try {
      dgest = MessageDigest.getInstance(type);
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
    InputStream str = fileUrl.openStream();
    byte[] buffer = new byte[65536];
    int readLen;
    while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
      dgest.update(buffer, 0, readLen);
    }
    str.close();
    Formatter fmt = new Formatter();
    for (byte b : dgest.digest()) {
      fmt.format("%02X", b);
    }
    String result = fmt.toString();
    fmt.close();
    return result;
  }

  public static void ssl_hack() {
    // Create a new trust manager that trust all certificates
    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

      public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
    }};

    // Activate the new trust manager
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    } catch (Exception ignored) {
    }
  }

  public static long getTotalSize(List<DownloadInfo> downloads) {
    try {
      Logger.logDebug("Starting size getting.");
      double start = System.currentTimeMillis();
      Collection<Long> size =
          new Parallel.ForEach<DownloadInfo, Long>(downloads).apply(
              new Parallel.F<DownloadInfo, Long>() {
                @Override
                public Long apply(DownloadInfo e) {
                  try {
                    if (e.size > 0) {
                      return e.size;
                    } else {
                      URLConnection con = e.url.openConnection();
                      return con.getContentLengthLong();
                    }
                  } catch (IOException e1) {
                    Logger.logError("Couldn't get size of file", e1);
                    return 0l;
                  }
                }

              }).values();
      Logger.logDebug(String.format("Finished size getting after %.2f seconds.",
          (System.currentTimeMillis() - start) / 1000));
      long sizeNumber = 0;
      for (Long l : size) {
        sizeNumber += l;
      }
      return sizeNumber;
    } catch (Exception e) {
      Logger.logError("Couldn't execute parallel task.", e);
      return 0l;
    }
  }

  public static synchronized void setTotalSize(final List<DownloadInfo> downloads,
      final AssetDownloader instance) {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        instance.setTotalSize(getTotalSize(downloads));
      }
    });
    t.start();
  }


}
