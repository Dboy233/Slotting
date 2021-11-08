package com.dboy.slotting.example;

import android.util.Log;

/**
 * 用来编译查看字节码
 */
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class EventDemo {

    private String globalString = "StringValue";

    private int globalInt = 1;

    /**
     * 配置文件中的字段name=globalLong于这个不对应，所以在插入的时候会忽略fuckLong
     */
    private long fuckLong = 1L;

    private double globalDouble = 1.1;

    private float globalFloat = 1.2f;

    private char globalChar = '?';

    private byte globalByte = 1;

    private boolean globalBoolean = false;

    public void event() {
        String localString = "我是之前的代码，测试会不会影响其他操作";
        Log.d("DBoy", localString);
    }

    public void eventLocal() {
        int localInt = 100;
        String localString = "局部变量的影响";
        Log.d("Dboy", "局部变量插入测试-" + localString);
    }

    public String eventReturn(boolean check, boolean isThrow) {
        if (isThrow) {
            String localThrow = "异常";
            throw new NullPointerException("throwReturn");
        }
        if (check) {
            String localTrue = "true";
            return "eventReturn true";
        } else {
            String localFalse = "false";
            return "eventReturn false";
        }
    }

    public void eventMap() {
        String localString = "我是之前的代码，测试会不会影响其他操作";
        Log.d("DBoy", localString);
    }

    public String eventMapReturn(boolean check, boolean isThrow) {
        if (isThrow) {
            boolean localThrow = false;
            Log.d("DJC", "eventMapReturn: " + localThrow);
            throw new NullPointerException("throwReturn");
        }
        if (check) {
            boolean localTrue = true;
            return "eventMapReturn true";
        } else {
            String localFalse = "false";
            return "eventMapReturn false";
        }
    }
}
