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
        while (cis.read(buffer) >= 0) {
            //Reading
        }
        return cis.getChecksum().getValue();
    }

    public String getMD5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[1024];
            while (dis.read(buffer) >= 0) {
                //Reading
            }
        }
        byte[] hash = md.digest();
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return hexString.toString();
    }

}
