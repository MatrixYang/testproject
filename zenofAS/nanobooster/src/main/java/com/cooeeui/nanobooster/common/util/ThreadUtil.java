package com.cooeeui.nanobooster.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 可变数量的线程池，采用Executors的静态方法 newCachedThreadPool 来创建 ExecutorService，该线程池的大小是不定的
 * ，当执行任务时，会先选取缓存中的空闲线程来执行，如果没有空闲线程，则创建一个新的线程，而如果空闲线程的空闲状态超过60秒，则线程池删除该线程。
 */
public class ThreadUtil {

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public static void execute(Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }
}
