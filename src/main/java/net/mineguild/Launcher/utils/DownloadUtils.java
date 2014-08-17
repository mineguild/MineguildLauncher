package net.mineguild.Launcher.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

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


}
