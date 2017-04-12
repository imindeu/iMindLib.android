package eu.imind.imindlib.videoplayer;

import android.util.Log;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;

public class Utils {

    private static final String LOG_TAG = "VideoPlayer";
    private static final int LOG_MAX_LENGTH = 500;

    private Utils() { }

    public static void warn(String message) {
        warn(message, null);
    }

    public static void warn(Throwable t) {
        warn(null, t);
    }

    public static void warn(String message, Throwable t) {
        log(WARN, message, t);
    }

    public static void debug(String message) {
        debug(message, null);
    }

    public static void debug(Throwable t) {
        debug(null, t);
    }

    public static void debug(String message, Throwable t) {
        log(DEBUG, message, t);
    }

    public static void log(int level, String message, Throwable t) {
        level = level == WARN ? WARN : DEBUG;
        message = message == null ? "" : message;
        message = t == null ? message : message + '\n' + Log.getStackTraceString(t);

        String[] lines = message.split("\n");
        for (int i = 0; i < lines.length; i++) {
            Log.println(level, LOG_TAG,
                    lines[i].substring(0, Math.min(lines[i].length(), LOG_MAX_LENGTH)));
        }
    }

}
