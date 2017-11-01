package cn.njmeter.bluetooth.constant;

/**
 * 接口返回值常量，区别于strings.xml中的值
 * xml中的String会随着语言变化而变化，这里的值是固定的
 * Created by LiYuliang on 2017/10/25.
 *
 * @author LiYuliang
 * @version 2017/10/25
 */

public class Constant {

    public static final String EMPTY = "";
    public static final String FAIL = "fail";
    public static final String NEW_LINE = "\n";
    public static final String POINT = ".";
    public static final String SUCCESS = "success";

    public static final int METER_ID_LENGTH = 8;
    public static final int HYDRANT_ID_LENGTH = 8;

    public static final int ACTIVITY_REQUEST_CODE_100 = 100;


    public static final int ACTIVITY_RESULT_CODE_100 = 100;
    public static final int ACTIVITY_RESULT_CODE_200 = 200;

    /**
     * 退出程序点击两次返回键的间隔时间
     */
    public static final int EXIT_DOUBLE_CLICK_TIME = 2000;
    /**
     * 距离达到1000m进行单位转换，变为1km
     */
    public static final int KILOMETER = 1000;
    /**
     * 网页加载完成进度
     */
    public static final int PROGRESS_WEBVIEW = 100;
}
