package com.cooeeui.basecore.utilities;

/**
 * Created by Steve on 2015/7/29.
 */

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

/**
 * 单卡、双卡双待手机检测类
 */
public final class TelephonyInfo {

  private static TelephonyInfo telephonyInfo;
  private String imsiSIM1;
  private String imsiSIM2;
  private boolean isSIM1Ready;
  private boolean isSIM2Ready;

  private TelephonyInfo() {
  }

    /*public static void setImsiSIM1(String imsiSIM1) {
        TelephonyInfo.imsiSIM1 = imsiSIM1;
    }*/

  public static TelephonyInfo getInstance(Context context) {

    if (telephonyInfo == null) {

      telephonyInfo = new TelephonyInfo();

      TelephonyManager
          telephonyManager =
          ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

      telephonyInfo.imsiSIM1 = telephonyManager.getDeviceId();
      telephonyInfo.imsiSIM2 = null;

      try {
        telephonyInfo.imsiSIM1 = getDeviceIdBySlot(context, "getDeviceIdGemini", 0);
        telephonyInfo.imsiSIM2 = getDeviceIdBySlot(context, "getDeviceIdGemini", 1);
      } catch (GeminiMethodNotFoundException e) {
        e.printStackTrace();

        try {
          telephonyInfo.imsiSIM1 = getDeviceIdBySlot(context, "getDeviceId", 0);
          telephonyInfo.imsiSIM2 = getDeviceIdBySlot(context, "getDeviceId", 1);
        } catch (GeminiMethodNotFoundException e1) {
          //Call here for next manufacturer's predicted method name if you wish
          e1.printStackTrace();
        }
      }

      telephonyInfo.isSIM1Ready =
          telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
      telephonyInfo.isSIM2Ready = false;

      try {
        telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimStateGemini", 0);
        telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimStateGemini", 1);
      } catch (GeminiMethodNotFoundException e) {

        e.printStackTrace();

        try {
          telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimState", 0);
          telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimState", 1);
        } catch (GeminiMethodNotFoundException e1) {
          //Call here for next manufacturer's predicted method name if you wish
          e1.printStackTrace();
        }
      }
    }

    return telephonyInfo;
  }

    /*public static void setImsiSIM2(String imsiSIM2) {
        TelephonyInfo.imsiSIM2 = imsiSIM2;
    }*/

  private static String getDeviceIdBySlot(Context context, String predictedMethodName, int slotID)
      throws GeminiMethodNotFoundException {

    String imsi = null;

    TelephonyManager
        telephony =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    try {

      Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

      Class<?>[] parameter = new Class[1];
      parameter[0] = int.class;
      Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

      Object[] obParameter = new Object[1];
      obParameter[0] = slotID;
      Object ob_phone = getSimID.invoke(telephony, obParameter);

      if (ob_phone != null) {
        imsi = ob_phone.toString();

      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new GeminiMethodNotFoundException(predictedMethodName);
    }

    return imsi;
  }

    /*public static void setSIM1Ready(boolean isSIM1Ready) {
        TelephonyInfo.isSIM1Ready = isSIM1Ready;
    }*/

  private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID)
      throws GeminiMethodNotFoundException {

    boolean isReady = false;

    TelephonyManager
        telephony =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    try {

      Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

      Class<?>[] parameter = new Class[1];
      parameter[0] = int.class;
      Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

      Object[] obParameter = new Object[1];
      obParameter[0] = slotID;
      Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

      if (ob_phone != null) {
        int simState = Integer.parseInt(ob_phone.toString());
        if (simState == TelephonyManager.SIM_STATE_READY) {
          isReady = true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new GeminiMethodNotFoundException(predictedMethodName);
    }

    return isReady;
  }

    /*public static void setSIM2Ready(boolean isSIM2Ready) {
        TelephonyInfo.isSIM2Ready = isSIM2Ready;
    }*/

  public static void printTelephonyManagerMethodNamesForThisDevice(Context context) {

    TelephonyManager
        telephony =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    Class<?> telephonyClass;
    try {
      telephonyClass = Class.forName(telephony.getClass().getName());
      Method[] methods = telephonyClass.getMethods();
      for (int idx = 0; idx < methods.length; idx++) {

        System.out
            .println("\n" + methods[idx] + " declared by " + methods[idx].getDeclaringClass());
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  //获取第一张卡的IMEI
  public String getImsiSIM1() {
    return imsiSIM1;
  }
  //获取第二张卡的IMEI
  public String getImsiSIM2() {
    return imsiSIM2;
  }

  //判断第一个SIM卡是否准备就绪
  public boolean isSIM1Ready() {
    return isSIM1Ready;
  }
  //判断第二个SIM卡是否准备就绪
  public boolean isSIM2Ready() {
    return isSIM2Ready;
  }

  //判断是否为双卡
  public boolean isDualSIM() {
    return imsiSIM2 != null;
  }

  private static class GeminiMethodNotFoundException extends Exception {

    private static final long serialVersionUID = -996812356902545308L;

    public GeminiMethodNotFoundException(String info) {
      super(info);
    }
  }
}