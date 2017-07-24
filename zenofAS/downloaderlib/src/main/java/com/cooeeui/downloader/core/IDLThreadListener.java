package com.cooeeui.downloader.core;

interface IDLThreadListener {

    void onProgress(int progress);

    void onStop(DLThreadInfo threadInfo);

    void onFinish(DLThreadInfo threadInfo);
}
