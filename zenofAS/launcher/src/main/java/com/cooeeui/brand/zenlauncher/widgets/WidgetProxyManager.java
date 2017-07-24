package com.cooeeui.brand.zenlauncher.widgets;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/3/14.
 */
public class WidgetProxyManager implements IWidgetProxy{

    private Context context;
    private Class<?> wrapClass;
    private Constructor<?> wrapConstructor;
    private Method methodGetView;
    private Method methodOnPause;
    private Method methodOnResume;
    private Method methodOnDestroy;
    private Method methodOnCreate;
    private Object obj;
    private static WidgetProxyManager instance;

    public static WidgetProxyManager getInstance() {
        if (instance == null) {
            synchronized (WidgetProxyManager.class) {
                if (instance == null) {
                    instance = new WidgetProxyManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        try {
            methodOnCreate.invoke(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            methodOnDestroy.invoke(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        try {
            methodOnResume.invoke(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        try {
            methodOnPause.invoke(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(Integer id) {
        try {
            return (View) methodGetView.invoke(obj,id);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean loadInstance() {
        try {
            obj = wrapConstructor.newInstance(context);
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadClassType(String className) {
        try {
            wrapClass = Class
                .forName(className, true, context.getClassLoader());
            wrapConstructor = wrapClass.getDeclaredConstructor(Context.class);
            methodOnCreate = wrapClass.getDeclaredMethod("onCreate");
            methodOnDestroy = wrapClass.getDeclaredMethod("onDestroy");
            methodOnResume = wrapClass.getDeclaredMethod("onResume");
            methodOnPause = wrapClass.getDeclaredMethod("onPause");
            methodGetView = wrapClass.getDeclaredMethod("getView",Integer.class);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadContext(Context currentContext, String packageName) {
        if (currentContext.getPackageName().equals(packageName)){
            context = currentContext;
            return true;
        }
        try {
            context = currentContext.createPackageContext(packageName,
                                                          Context.CONTEXT_INCLUDE_CODE
                                                          | Context.CONTEXT_IGNORE_SECURITY);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadProxy(Context context, String packageName,
                             String className) {
        if (context == null || packageName == null || className == null) {
            return false;
        }
        if (loadContext(context, packageName) && loadClassType(className)
            && loadInstance()) {
            return true;
        }
        return false;
    }
}
