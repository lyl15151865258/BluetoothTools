package cn.njmeter.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.njmeter.bluetooth.R;

public class CommonUtils {

    private static Toast toast = null;

    public static String getSystime() {
        String systime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE", Locale.CHINA);
        systime = dateFormat.format(new Date(System.currentTimeMillis()));
        return systime;
    }

    public static String getFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        StringBuffer sb = new StringBuffer();
        if (fileSize < 1024) {
            sb.append(fileSize);
            sb.append(" B");
        } else if (fileSize < 1048576) {
            sb.append(df.format((double) fileSize / 1024));
            sb.append(" K");
        } else if (fileSize < 1073741824) {
            sb.append(df.format((double) fileSize / 1048576));
            sb.append(" M");
        } else {
            sb.append(df.format((double) fileSize / 1073741824));
            sb.append(" G");
        }
        return sb.toString();
    }

    /**
     * 获取当前日期
     *
     * @return 20140716
     */
    public static String getDate() {
        Date date = new Date(System.currentTimeMillis());
        String strs = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            strs = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strs;
    }

    /**
     * 验证邮箱格式
     *
     * @param email email
     * @return
     */
    public static boolean verifyEmail(String email) {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)"
                + "|(([a-zA-Z0-9\\-]+\\.)+))" + "([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 验IP地址格式
     * 由于正则表达式判断不准确，这里采用String操作
     *
     * @param ip ip
     * @return
     */
    public static boolean verifyIp(String ip) {
        //注意转义字符的使用
        String[] string = ip.split("\\.");
        if (string.length == 4) {
            for (int i = 0; i < 4; i++) {
                //每一段数值应在0~255之间，而且长度不大于3
                if (!(Integer.parseInt(string[i]) >= 0 && Integer.parseInt(string[i]) <= 255 && string[i].length() < 4)) {
                    return false;
                }
                //循环判断，当判断到最后一个时仍正确，则返回true
                if (i == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * 验证密码格式
     *
     * @param password
     * @return
     */
    public static boolean verifyPassword(String password) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]{6,16}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    /**
     * 获取应用名称
     *
     * @param context Context实例
     * @return 返回String型名称
     */
    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    /**
     * 获取当前程序的版本名
     */
    public static String getVersionName(Context context) {
        try {
            //获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            //getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = null;
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            LogUtils.d("TAG", "版本号" + packInfo.versionCode);
            LogUtils.d("TAG", "版本名" + packInfo.versionName);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }

    /**
     * 获取当前程序的版本号
     */
    public static int getVersionCode(Context context) {
        try {
            //获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            //getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            LogUtils.d("TAG", "版本号" + packInfo.versionCode);
            LogUtils.d("TAG", "版本名" + packInfo.versionName);
            return packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //自定义的Toast，避免重复出现
    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = new Toast(context);
            //设置Toast显示位置，居中，向 X、Y轴偏移量均为0
//        toast.setGravity(Gravity.CENTER, 0, 0);
            //获取自定义视图
            View view = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            TextView tvMessage = (TextView) view.findViewById(R.id.tv_toast_text);
            //设置文本
            tvMessage.setText(msg);
            //设置视图
            toast.setView(view);
            //设置显示时长
            toast.setDuration(Toast.LENGTH_SHORT);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            TextView tvMessage = (TextView) view.findViewById(R.id.tv_toast_text);
            //设置文本
            tvMessage.setText(msg);
            //设置视图
            toast.setView(view);
            //设置显示时长
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void getShortToast(Context context, String hint) {
        if (toast == null) {
            toast = Toast.makeText(context, hint, Toast.LENGTH_SHORT);
        } else {
            toast.setText(hint);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void getLongToast(Context context, String hint) {
        if (toast == null) {
            toast = Toast.makeText(context, hint, Toast.LENGTH_LONG);
        } else {
            toast.setText(hint);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(final Context context, final String key, final String defaultValue) {
        return CommonUtils.getSharedPreferences(context).getString(key, defaultValue);
    }

    public static boolean putString(final Context context, final String key, final String pValue) {
        final SharedPreferences.Editor editor = CommonUtils.getSharedPreferences(context).edit();

        editor.putString(key, pValue);

        return editor.commit();
    }

    //获取设备唯一标识码
    public static String getDeviceId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append(getChannelName(context));
        try {
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (!TextUtils.isEmpty(imei)) {
                deviceId.append("imei");
                deviceId.append(imei);
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if (!TextUtils.isEmpty(sn)) {
                deviceId.append("sn");
                deviceId.append(sn);
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID();
            if (!TextUtils.isEmpty(uuid)) {
                deviceId.append("id");
                deviceId.append(uuid);
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID());
        }
        return deviceId.toString();
    }

    /**
     * 得到全局唯一UUID
     */
    private static String getUUID() {
        String uuid = (String) SharedPreferencesUtils.getInstance().getData("uuid", "");
        if (!TextUtils.isEmpty(uuid)) {
            return uuid;
        } else {
            uuid = UUID.randomUUID().toString();
            SharedPreferencesUtils.getInstance().saveData("uuid", uuid);
        }
        return uuid;
    }

    /**
     * 获取渠道名
     *
     * @param ctx Context对象
     * @return 如果没有获取成功，那么返回值为空
     */
    private static String getChannelName(Context ctx) {
        if (ctx == null) {
            return null;
        }
        String channelName = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = applicationInfo.metaData.getString("");
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return channelName;
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值, 或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resultData;
    }
}
