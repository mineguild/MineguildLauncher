package net.mineguild.Launcher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class ChecksumUtil {

    public long getChecksum(File file) throws Exception {
        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), new Adler32());
        byte[] buffer = new byte[128];
        int bytesRead;
        do {
            bytesRead = cis.read(buffer);
        } while (bytesRead >= 0);
        return cis.getChecksum().getValue();
    }

    public String getMD5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[1024];
            int bytesRead;
            do {
                bytesRead = dis.read(buffer);
            } while (bytesRead >= 0);
        }
        byte[] hash = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            if ((0xff & aHash) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & aHash)));
            } else {
                hexString.append(Integer.toHexString(0xFF & aHash));
            }
        }
        return hexString.toString();
    }

}
