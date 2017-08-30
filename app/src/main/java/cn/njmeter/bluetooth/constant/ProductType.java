package cn.njmeter.bluetooth.constant;

/**
 * Created by LiYuliang on 2017/8/22 0022.
 * 产品类型以及操作指令代码
 */

public class ProductType {

    //产品类型代码

    public static final String WATER_METER = "10";                                              //智能水表
    public static final String HEAT_METER = "20";                                               //智能热量表
    public static final String M_BUS = "48";                                                    //M_Bus主机
    public static final String ON_OFF_CONTROLLER = "49";                                        //通断控制器
    public static final String CONTROL_VALVE = "50";                                            //智能控制阀
    public static final String LORA = "54";                                                     //LoRa无线模块
    public static final String PERMANENT_FLOW_VALVE = "56";                                     //智能恒流阀
    public static final String HYDRANT = "59";                                                  //智能消火栓

    //操作类型代码（用于指令解析时的switch-case）

    public static final int WATER_METER_READ_METER_DATA = 1001;                                 //读取水表数据
    public static final int WATER_METER_READ_METER_INTER_PARAMETER = 1002;                      //读取水表内部参数（流量系数等）
    public static final int WATER_METER_OPEN_CLOSE_VALVE = 1003;                                //水表开关阀
    public static final int WATER_METER_CORRECTION_TIME = 1004;                                 //水表校正时钟

    public static final int HEAT_METER_READ_METER_DATA = 2001;                                  //读取热表数据
    public static final int HEAT_METER_READ_METER_INTER_PARAMETER = 2002;                       //读取热表内部参数（流量系数等）
    public static final int HEAT_METER_OPEN_CLOSE_VALVE = 2003;                                 //热表开关阀
    public static final int HEAT_METER_CORRECTION_TIME = 2004;                                  //热表校正时钟

    public static final int HYDRANT_READ_WARNING_PARAMETER = 4901;                              //消火栓读取报警参数
    public static final int HYDRANT_READ_WARNING_STATE = 4902;                                  //消火栓读取报警状态
    public static final int HYDRANT_READ_METER_DATA = 4903;                                     //消火栓读取表数据
    public static final int HYDRANT_READ_GPS = 4904;                                            //消火栓读取GPS信息
    public static final int HYDRANT_READ_CURRENT_USE_DATA = 4905;                               //消火栓读取当前使用数据
    public static final int HYDRANT_READ_HISTORY_USE_DATA = 4906;                               //消火栓读取历史使用数据
    public static final int HYDRANT_READ_WARNING_ENABLE = 4907;                                 //消火栓读取报警使能


    public static final int HYDRANT_READ_METER_INTER_PARAMETER = 4911;                          //消火栓读取内部参数（流量系数等）
    public static final int HYDRANT_OPEN_CLOSE_VALVE = 4912;                                    //消火栓开关阀门锁
    public static final int HYDRANT_CORRECTION_TIME = 4913;                                     //消火栓校正时钟
    public static final int HYDRANT_CHANGE_TYPE = 4914;                                         //消火栓修改类型
}
