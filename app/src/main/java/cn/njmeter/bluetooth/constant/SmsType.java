package cn.njmeter.bluetooth.constant;

/**
 * Created by LiYuliang on 2017/8/18 0018.
 * 短信验证码的配置
 */

public class SmsType {

    //短信类型
    public static final String REGISTER = "1";                            //注册获取验证码
    public static final String RETRIEVE_PASSWORD = "2";                   //找回密码的验证码
    public static final String CHANGE_PASSWORD = "3";                     //修改密码的验证码
    public static final String CHANGE_PHONENUMBER = "4";                  //修改手机号的验证码

    //获取短信模板（模板在容联云通讯平台配置）
    public static String getTemplateCode(String smsType) {
        int templateCode;
        switch (smsType) {
            case REGISTER:
                templateCode = 1;
                break;
            case RETRIEVE_PASSWORD:
                templateCode = 1;
                break;
            case CHANGE_PASSWORD:
                templateCode = 1;
                break;
            case CHANGE_PHONENUMBER:
                templateCode = 1;
                break;
            default:
                templateCode = 0;
                break;
        }
        return String.valueOf(templateCode);
    }

    //获取短信验证码长度
    public static int getSmsLength(String smsType) {
        int smsLength;
        switch (smsType) {
            case REGISTER:
                smsLength = 6;
                break;
            case RETRIEVE_PASSWORD:
                smsLength = 6;
                break;
            case CHANGE_PASSWORD:
                smsLength = 6;
                break;
            case CHANGE_PHONENUMBER:
                smsLength = 6;
                break;
            default:
                smsLength = 0;
                break;
        }
        return smsLength;
    }

    //获取短信验填写时间（分钟）
    public static String getSmsTime(String smsType) {
        int smsTime;
        switch (smsType) {
            case REGISTER:
                smsTime = 5;
                break;
            case RETRIEVE_PASSWORD:
                smsTime = 5;
                break;
            case CHANGE_PASSWORD:
                smsTime = 2;
                break;
            default:
                smsTime = 0;
                break;
        }
        return String.valueOf(smsTime);
    }
}
