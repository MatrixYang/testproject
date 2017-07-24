package com.cooeeui.downloader.core;

public class DLThreadInfo {

    String id;
    String baseUrl;
    int start, end;
    public boolean isStop;

    DLThreadInfo(String id, String baseUrl, int start, int end) {
        this.id = id;
        this.baseUrl = baseUrl;
        this.start = start;
        this.end = end;
    }
}
