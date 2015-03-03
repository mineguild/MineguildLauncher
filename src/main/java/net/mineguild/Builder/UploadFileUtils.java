package net.mineguild.Builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.BuilderSettings.UploadSettings;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class UploadFileUtils {

  private static Map<String, ChannelSftp> channels;

  public static void uploadMineguild(String basePath, ModPack pack, UploadSettings set) throws JSchException,
      SftpException, IOException, InterruptedException {
    // String adress = JOptionPane.showInputDialog("Adress:");
    // String user = JOptionPane.showInputDialog("User:");
    // String password = JOptionPane.showInputDialog("Password:");
    String adress = set.getAddress();
    String user = set.getPassword();
    String password = set.getPassword();
    final String uploadPath = set.getFilePath();
    int port = 22;
    JSch connection = new JSch();
    String host = adress;
    if (adress.split(":").length == 2) {
      host = adress.split(":")[0];
      port = Integer.parseInt(adress.split(":")[1]);
    }

    Session s = connection.getSession(user, host, port);
    s.setPassword(password);
    s.setConfig("StrictHostKeyChecking", "no");
    s.connect();
    ChannelExec exec = (ChannelExec) s.openChannel("exec");
    exec.setCommand(String.format("cd %s && ls -d -1 */", uploadPath));
    exec.setErrStream(System.err);
    InputStream in = exec.getInputStream();
    exec.connect();
    String out = new String();
    byte[] tmp = new byte[1024];
    while (true) {
      while (in.available() > 0) {
        int i = in.read(tmp, 0, 1024);
        if (i < 0)
          break;
        out += new String(tmp, 0, i);
      }
      if (exec.isClosed()) {
        if (in.available() > 0)
          continue;
        System.out.println("exit-status: " + exec.getExitStatus());
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }
    exec.disconnect();
    s.disconnect();
    final Session ses = connection.getSession(user, host, port);
    ses.setPassword(password);
    ses.setConfig("StrictHostKeyChecking", "no");
    ses.connect();
    Channel channel = ses.openChannel("sftp");
    channel.connect();

    final ChannelSftp c = (ChannelSftp) channel;

    c.cd(uploadPath);

    placeUploadFiles(basePath, pack.getFiles());
    List<File> files =
        (List<File>) FileUtils.listFiles(new File("upload"), FileFilterUtils.trueFileFilter(),
            FileFilterUtils.trueFileFilter());
    final List<String> directories = Lists.newArrayList();
    if (!out.isEmpty()) {
      directories.addAll(Lists.newArrayList(out.split("\n")));
    }
    channels = Maps.newHashMap();
    Parallel.ForEach(files, new Parallel.Action<File>() {
      @Override
      public void doAction(File f) {
        if (!channels.containsKey(Thread.currentThread().getName())) {
          try {
            Channel channel = ses.openChannel("sftp");
            channel.connect();
            channels.put(Thread.currentThread().getName(), (ChannelSftp) channel);
            Logger.logDebug(String.format("Created new channel! (%s)", Thread.currentThread()
                .getName()));
          } catch (JSchException e1) {
            Logger.logError("Couldn't create new channel!");
          }
        }

        String directory = f.getParentFile().getName();
        if (!directories.contains(directory + "/")) {
          synchronized (c) {
            try {
              SftpATTRS attr = c.stat(directory);
              if (!attr.isDir()) {
                Logger.logDebug(String.format("'%s' is a file. Deleting and creating directory.",
                    directory));
                c.rm(directory);
                c.mkdir(directory);
              }
            } catch (SftpException e) {
              if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                Logger.logDebug(String.format("Directory '%s' doesn't exist, creating", directory));
                try {
                  c.mkdir(directory);
                } catch (SftpException e1) {
                  e1.printStackTrace();
                }
              } else {
                Logger.logError("Error occurred!", e);
              }
            }
          }
        }

        try {
          ChannelSftp c = channels.get(Thread.currentThread().getName());
          c.cd(uploadPath);
          String filePath = f.getParentFile().getName() + "/" + f.getName();
          boolean doUpload = false;
          try {
            SftpATTRS fileAttr = c.stat(filePath);
            if (fileAttr.getSize() != f.length()) {
              Logger.logInfo(String.format(
                  "File size doesn't match for '%s'. Remote %d | Local %d. Re-uploading.",
                  filePath, fileAttr.getSize(), f.length()));
              c.rm(filePath);
              doUpload = true;
            }
          } catch (SftpException ee) {
            ee.printStackTrace();
            if (ee.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
              doUpload = true;
            }
          }

          if (doUpload) {
            try {
              OutputStream stream = c.put(filePath);
              FileUtils.copyFile(f, stream);
              stream.close();
              Logger.logDebug(String.format("Uploaded %s", filePath));
            } catch (Exception e3) {
              Logger.logError("Error while uploading!", e3);
            }
          }
        } catch (Exception e3) {
          e3.printStackTrace();
        }

      }
    });
    
    Logger.logInfo("Uploading ModPack file");
    c.cd(set.getVersionsPath());
    JsonWriter.saveModpack(pack, c.put(pack.getHash()));
    ModpackBuilder.getVerRepo().getVersions().add(pack);
    Logger.logInfo("Uploading Repository");
    JsonWriter.saveRepository(ModpackBuilder.getRepo(), c.put(set.getRepoPath(), ChannelSftp.OVERWRITE));
    Logger.logInfo("Done!");

  }

  public static void placeUploadFiles(String basePath, Map<String, ModPackFile> files) {
    File uploadDir = new File("upload");
    uploadDir.mkdir();
    try {
      FileUtils.cleanDirectory(uploadDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Map.Entry<String, ModPackFile> entry : files.entrySet()) {
      File file = new File(basePath, entry.getKey());
      File newDirectory = new File(uploadDir, entry.getValue().getHash().substring(0, 2));
      File newFile = new File(newDirectory, entry.getValue().getHash());
      if (file.exists()) {
        try {
          FileUtils.copyFile(file, newFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void fromUploadFiles(Map<String, String> files) {
    File modpack = new File("modpack");
    File upload = new File("upload");
    modpack.mkdir();
    try {
      FileUtils.cleanDirectory(modpack);
    } catch (IOException ignored) {
    }
    for (Map.Entry<String, String> entry : files.entrySet()) {
      String hash = entry.getValue();
      File fileDir = new File(upload, hash.substring(0, 2));
      File file = new File(fileDir, hash);
      String path = entry.getKey();
      File filePath = new File(modpack, path);
      try {
        System.out.printf("Copying %s to %s\n", file.toString(), filePath.toString());
        FileUtils.copyFile(file, filePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
