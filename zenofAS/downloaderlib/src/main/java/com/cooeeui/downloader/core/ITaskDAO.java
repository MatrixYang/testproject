package com.cooeeui.downloader.core;

interface ITaskDAO {

    void insertTaskInfo(DLInfo info);

    void deleteTaskInfo(String url);

    void updateTaskInfo(DLInfo info);

    DLInfo queryTaskInfo(String url);
}
