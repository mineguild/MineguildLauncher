package net.mineguild.Launcher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import lombok.Getter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.winreg.RuntimeStreamer;

import org.apache.commons.io.FileUtils;

public class OSUtils {

    public static enum OS {
        WINDOWS, UNIX, MACOSX, OTHER,
    }


    @Getter private static int numCores;

    static {
        numCores = Runtime.getRuntime().availableProcessors();
    }

    private static byte[] cachedMacAddress;
    private static byte[] hardwareID;

    private static byte[] genHardwareID() {
        switch (getCurrentOS()) {
            case WINDOWS:
                return genHardwareIDWINDOWS();
            case UNIX:
                return genHardwareIDUNIX();
            case MACOSX:
                return genHardwareIDMACOSX();
            default:
                return null;
        }
    }

    public static byte[] getMacAddress() {
        if (cachedMacAddress != null && cachedMacAddress.length >= 10) {
            return cachedMacAddress;
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces =
                NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface network = networkInterfaces.nextElement();
                byte[] mac = network.getHardwareAddress();
                if (mac != null && mac.length > 0 && !network.isLoopback() && !network.isVirtual()
                    && !network.isPointToPoint()) {
                    Logger.logDebug(
                        "Interface: " + network.getDisplayName() + " : " + network.getName());
                    cachedMacAddress = new byte[mac.length * 10];
                    for (int i = 0; i < cachedMacAddress.length; i++) {
                        cachedMacAddress[i] = mac[i - (Math.round(i / mac.length) * mac.length)];
                    }
                    return cachedMacAddress;
                }
            }
        } catch (SocketException e) {
            Logger.logWarn("Exception getting MAC address", e);
        }

        Logger.logWarn("Failed to get MAC address, using default logindata key");
        return new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    public static byte[] getHardwareID() {
        if (hardwareID == null) {
            hardwareID = genHardwareID();
        }
        return hardwareID;
    }

    public static long getOSTotalMemory() {
        return getOSMemory("getTotalPhysicalMemorySize", "Could not get RAM Value");
    }

    public static long getOSFreeMemory() {
        return getOSMemory("getFreePhysicalMemorySize", "Could not get free RAM Value");
    }

    public static boolean is64bitOS() {
        switch (getCurrentOS()) {
            case WINDOWS:
                return is64BitWindows();
            case MACOSX:
                return is64BitOSX();
            case UNIX:
                return is64BitPosix();
            default:
                return false;
        }
    }

    public static OS getCurrentOS() {
        String osString = System.getProperty("os.name").toLowerCase();
        if (osString.contains("win")) {
            return OS.WINDOWS;
        } else if (osString.contains("nix") || osString.contains("nux")) {
            return OS.UNIX;
        } else if (osString.contains("mac")) {
            return OS.MACOSX;
        } else {
            return OS.OTHER;
        }
    }

    /**
     * Used to check if Windows is 64-bit
     *
     * @return true if 64-bit Windows
     */
    public static boolean is64BitWindows() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        return (arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64")));
    }

    /**
     * Used to check if a posix OS is 64-bit
     *
     * @return true if 64-bit Posix OS
     */
    public static boolean is64BitPosix() {
        String line, result = "";
        try {
            Process command = Runtime.getRuntime().exec("uname -m");
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                result += (line + "\n");
            }
        } catch (Exception e) {
            Logger.logError("Posix bitness check failed", e);
        }
        // 32-bit Intel Linuxes, it returns i[3-6]86. For 64-bit Intel, it says x86_64
        return result.contains("_64");
    }

    /**
     * Used to check if OS X is 64-bit
     *
     * @return true if 64-bit OS X
     */

    public static boolean is64BitOSX() {
        String line, result = "";
        if (!(System.getProperty("os.version").startsWith("10.6") || System
            .getProperty("os.version").startsWith("10.5"))) {
            return true;// 10.7+ only shipped on hardware capable of using 64 bit java
        }
        try {
            Process command = Runtime.getRuntime().exec("/usr/sbin/sysctl -n hw.cpu64bit_capable");
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                result += (line + "\n");
            }
        } catch (Exception e) {
            Logger.logError("OS X bitness check failed", e);
        }
        return result.equals("1");
    }

    private static long getOSMemory(String methodName, String warning) {
        long ram = 0;

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method m;
        try {
            m = operatingSystemMXBean.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            Object value = m.invoke(operatingSystemMXBean);
            if (value != null) {
                ram = Long.valueOf(value.toString()) / 1024 / 1024;
            } else {
                Logger.logWarn(warning);
                ram = 1024;
            }
        } catch (Exception e) {
            Logger.logError("Error while getting OS memory info", e);
        }

        return ram;
    }



    @SuppressWarnings("unused") private static byte[] genHardwareIDUNIX() {
        String line;
        // TODO: will add command line option or advanced option later. Use old mac address method
        if (false) {
            try {
                line = FileUtils.readFileToString(new File("/etc/machine-id"));
            } catch (Exception e) {
                Logger.logDebug("failed", e);
                return new byte[] {};
            }
            return line.getBytes();
        } else {
            return new byte[] {};
        }
    }

    private static byte[] genHardwareIDMACOSX() {
        String line;
        try {
            Process command =
                Runtime.getRuntime().exec(new String[] {"system_profiler", "SPHardwareDataType"});
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                if (line.contains("Serial Number"))
                    // TODO: does that more checks?
                    return line.split(":")[1].trim().getBytes();
            }
            return new byte[] {};
        } catch (Exception e) {
            Logger.logDebug("failed", e);
            return new byte[] {};
        }
    }

    private static byte[] genHardwareIDWINDOWS() {
        String processOutput;
        try {
            processOutput =
                RuntimeStreamer.execute(new String[] {"wmic", "bios", "get", "serialnumber"});
      /*
       * wmic's output has special formatting: SerialNumber<SP><SP><SP><CR><CR><LF>
       * 00000000000000000<SP><CR><CR><LF><CR><CR><LF>
       * 
       * readLin()e uses <LF>, <CR> or <CR><LF> as line ending => we need to get third line from
       * RuntimeStreamers output
       */
            String line = processOutput.split("\n")[2].trim();
            // at least VM will report serial to be 0. Does real hardware do it?
            if (line.equals("0")) {
                return new byte[] {};
            } else {
                return line.trim().getBytes();
            }
        } catch (Exception e) {
            Logger.logDebug("failed", e);
            return new byte[] {};
        }
    }

    /**
     * Used to get the java delimiter for current OS
     *
     * @return string containing java delimiter for current OS
     */
    public static String getJavaDelimiter() {
        switch (getCurrentOS()) {
            case WINDOWS:
                return ";";
            case UNIX:
                return ":";
            case MACOSX:
                return ":";
            default:
                return ";";
        }
    }

    public static File getLocalDir() {
        File directory;
        switch (getCurrentOS()) {
            case WINDOWS:
                directory = new File(System.getenv("AppData") + "/mmp");
                break;
            default:
                directory = new File(System.getProperty("user.home") + "/.mmp");
        }
        if(directory.exists() && !directory.isDirectory()){
            directory.delete();
        }
        if(!directory.exists()){
            directory.mkdir();
        }
        return directory;
    }
    
    public static void cleanEnvVars (Map<String, String> environment) {
      environment.remove("_JAVA_OPTIONS");
      environment.remove("JAVA_TOOL_OPTIONS");
      environment.remove("JAVA_OPTIONS");
  }

}
