package com.sms.server.util;

import java.util.zip.Deflater;

public class DetectionUtil {

	private static final int JAVA_VERSION = javaVersion0();
    private static final boolean IS_WINDOWS;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        IS_WINDOWS = os.contains("win");
    }

    /**
     * Return <code>true</code> if the JVM is running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    public static int javaVersion() {
        return JAVA_VERSION;
    }

    private static int javaVersion0() {
        // Android
        try {
            Class.forName("android.app.Application", false, ClassLoader.getSystemClassLoader());
            return 6;
        } catch (Exception e) {
            // Ignore
        }

        try {
            Deflater.class.getDeclaredField("SYNC_FLUSH");
            return 7;
        } catch (Exception e) {
            // Ignore
        }

        return 6;
    }

    private DetectionUtil() {
        // only static method supported
    }
}
