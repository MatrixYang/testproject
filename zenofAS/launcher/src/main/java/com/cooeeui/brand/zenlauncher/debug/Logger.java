package com.cooeeui.brand.zenlauncher.debug;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Logger {

    static final String DUMP_TAG = "Debug.Logger.Dummp";

    public static final int ANDROID_LOG = 0;
    public static final int DISABLE_LOG = -1;

    public static int type = ANDROID_LOG;
    public static boolean DEBUG_DUMP_LOG = true;

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();
    static Date sDateStamp = new Date();
    static DateFormat sDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT);
    static long sRunStart = System.currentTimeMillis();

    public static void debug(String tag, String message) {
        if (type == ANDROID_LOG) {
            Log.d(tag, message);
        }
    }

    public static void info(String tag, String message) {
        if (type == ANDROID_LOG) {
            Log.i(tag, message);
        }
    }

    public static void error(String tag, String message) {
        if (type == ANDROID_LOG) {
            Log.e(tag, message);
        }
    }

    public static void warning(String tag, String message) {
        if (type == ANDROID_LOG) {
            Log.w(tag, message);
        }
    }

    public static void verbose(String tag, String message) {
        if (type == ANDROID_LOG) {
            Log.v(tag, message);
        }
    }

    public static void dumpDebugLogsToConsole() {
        if (DEBUG_DUMP_LOG) {
            synchronized (sDumpLogs) {
                Logger.debug(DUMP_TAG, "");
                Logger.debug(DUMP_TAG, "*********************");
                Logger.debug(DUMP_TAG, "Launcher debug logs: ");
                for (int i = 0; i < sDumpLogs.size(); i++) {
                    Logger.debug(DUMP_TAG, "  " + sDumpLogs.get(i));
                }
                Logger.debug(DUMP_TAG, "*********************");
                Logger.debug(DUMP_TAG, "");
            }
        }
    }

    public static void dump(PrintWriter writer) {
        synchronized (sDumpLogs) {
            writer.println(" ");
            writer.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                writer.println("  " + sDumpLogs.get(i));
            }
        }
    }

    public static void addDumpLog(String tag, String log, boolean debugLog) {
        if (debugLog) {
            Logger.debug(tag, log);
        }
        if (DEBUG_DUMP_LOG) {
            sDateStamp.setTime(System.currentTimeMillis());
            synchronized (sDumpLogs) {
                sDumpLogs.add(sDateFormat.format(sDateStamp) + ": " + tag
                              + ", " + log);
            }
        }
    }

    public void dumpLogsToLocalData(final Context context) {
        if (DEBUG_DUMP_LOG) {
            new Thread("DumpLogsToLocalData") {
                @Override
                public void run() {
                    boolean success = false;
                    sDateStamp.setTime(sRunStart);
                    String FILENAME = sDateStamp.getMonth() + "-"
                                      + sDateStamp.getDay() + "_" + sDateStamp.getHours()
                                      + "-" + sDateStamp.getMinutes() + "_"
                                      + sDateStamp.getSeconds() + ".txt";

                    FileOutputStream fos = null;
                    File outFile = null;
                    try {
                        outFile = new File(context.getFilesDir(), FILENAME);
                        outFile.createNewFile();
                        fos = new FileOutputStream(outFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fos != null) {
                        PrintWriter writer = new PrintWriter(fos);

                        writer.println(" ");
                        writer.println("Debug logs: ");
                        synchronized (sDumpLogs) {
                            for (int i = 0; i < sDumpLogs.size(); i++) {
                                writer.println("  " + sDumpLogs.get(i));
                            }
                        }
                        writer.close();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                            success = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!success) {
                        Logger.error(DUMP_TAG, "Dummp to local data failed!");
                    }
                }
            }.start();
        }
    }
}
