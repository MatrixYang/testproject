package com.cooeeui.downloader.core;

import com.cooeeui.downloader.core.interfaces.IDListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载实体类 Download entity.
 */
public class DLInfo {

    public int totalBytes;
    public int currentBytes;
    public String fileName;
    public String dirPath;
    public String baseUrl;
    public String realUrl;

    public int redirect;
    public boolean hasListener;
    public boolean isResume;
    public boolean isStop;
    String mimeType;
    String eTag;
    String disposition;
    String location;
    public List<DLHeader> requestHeaders;
    public final List<DLThreadInfo> threads;
    public IDListener listener;
    File file;

    public DLInfo() {
        threads = new ArrayList<>();
    }

    synchronized void addDLThread(DLThreadInfo info) {
        threads.add(info);
    }

    synchronized void removeDLThread(DLThreadInfo info) {
        threads.remove(info);
    }
}
