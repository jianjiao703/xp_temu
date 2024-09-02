package com.jianjiao.duoduo;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    public String TAG = "__尖叫__xp";
    Context sContext;
    String ACTIONR = "com.jianjiao.test.PDDGUANGBO";
    ClassLoader classLoader;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        classLoader = lpparam.classLoader;
        if (!"com.einnovation.temu".equals(lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod("com.baogong.activity.NewPageActivity", classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                //获取activity和context
                Activity activity = (Activity) param.thisObject;
                sContext = activity.getApplicationContext();
                sendIntent(1, "插件已加载:" + activity.getPackageName());
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        ClassLoader classLoader = lpparam.classLoader;
        Class<?> aClass = classLoader.loadClass("kl1.a");
        XposedHelpers.findAndHookMethod("com.google.gson.d", classLoader, "o", java.lang.String.class, aClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (param.args[1].toString().contains("com.baogong.app_goods_detail.entity.GoodsDetailEntity")) {
                    sendIntent(2, param.args[0].toString());
                }
                Log.d(TAG, "开始hook: " + param.args[1] + "|" + param.args[0]);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

    }


    public void sendIntent(int code, String data) {
        // 确定切片大小
        final int SLICE_SIZE = 1024 * 100;
        // 分割数据
        List<String> slices = new ArrayList<>();
        for (int i = 0; i < data.length(); i += SLICE_SIZE) {
            slices.add(data.substring(i, Math.min(i + SLICE_SIZE, data.length())));
        }
        Log.d(TAG, "开始发送数据: " + data.length() + "| " + slices.size() + "|" + code);
        // 发送每个切片
        for (int i = 0; i < slices.size(); i++) {
            String sliceData = slices.get(i);
            // 创建 Intent 对象
            Intent intent = new Intent();
            intent.setAction(ACTIONR);
            // 添加切片索引和数据
            intent.putExtra("code", code);
            intent.putExtra("index", i);
            intent.putExtra("total", slices.size());
            intent.putExtra("data", sliceData);
            Log.d(TAG, "发送: " + i + "|" + slices.size() + "|" + sliceData);
            // 发送广播
            try {
                if (sContext == null) {
                    // 获取 application context 并发送广播
                    Class<?> SearchBox = classLoader.loadClass("com.baidu.searchbox.SearchBox");
                    sContext = (Context) XposedHelpers.callMethod(SearchBox, "getAppContext");
                    sContext.sendBroadcast(intent);
                    XposedBridge.log("发送广播: 自行获取application " + sContext);
                } else {
                    // 使用现有的 Context 发送广播
                    sContext.sendBroadcast(intent);
                    XposedBridge.log("发送广播: 使用原context " + sContext);
                }
            } catch (Exception e) {
                XposedBridge.log("发送消息失败: " + e);
                Log.d(TAG, "发送消息失败: " + e);
            }
        }
    }
}













