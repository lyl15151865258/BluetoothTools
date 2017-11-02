package cn.njmeter.bluetooth.fragment.hydrant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.LogUtils;
import cn.njmeter.bluetooth.utils.SharedPreferencesUtils;
import cn.njmeter.bluetooth.widget.dialog.InputAlarmEnabledDialog;
import cn.njmeter.bluetooth.widget.dialog.InputMeterAlarmDialog;

/**
 * Created by Li Yuliang on 2017/3/17
 * 消火栓管理员操作页面
 */
public class HydrantDataFragment extends Fragment {

    private Context context;
    private EditText et_meterId;
    private Button btn_open, btn_close;
    private RelativeLayout rl_map, relativeLayout_map;
    private LinearLayout scrollView_read_alarmParameters, scrollView_read_alarmState, scrollView_alarmEnabled, scrollView_meter_data,
            current_use_state, history_use_data;
    private TextView tv_socket, tv_socket2, tv_pressure_mutation1, tv_pressure_low_limit1, tv_leakage_flow_rate1, tv_leaking_timeout1,
            tv_lower_temperature_limit1, tv_vibration_variation1;
    private TextView tv_pressure_mutation2, tv_pressure_low_limit2, tv_leakage_flow_rate2, tv_leaking_timeout2, tv_lower_temperature_limit2,
            tv_vibration_variation2;
    //报警状态页面
    private ImageView iv_not_close_valve, iv_low_water_pressure, iv_low_temperature, iv_low_water_pressure_mutation, iv_no_water, iv_slant,
            iv_vibration, iv_outRange, iv_open_cover, iv_battery_low, iv_open_battery, iv_water_leakage, iv_valve_status, iv_reverse,
            iv_temperature_measurement_short_circuit, iv_temperature_measurement_open_circuit, iv_not_close_tightly_valve, iv_water_leakage_noUser;
    //报警使能页面
    private ImageView iv_gprs_reconnected_enabled, iv_not_close_valve_enabled, iv_low_water_pressure_enabled, iv_low_temperature_enabled,
            iv_low_water_pressure_mutation_enabled, iv_no_water_enabled, iv_slant_enabled, iv_vibration_enabled, iv_outRange_enabled,
            iv_open_cover_enabled, iv_battery_low_enabled, iv_open_battery_enabled, iv_water_leakage_enabled, iv_valve_status_enabled,
            iv_reverse_enabled, iv_temperature_measurement_short_circuit_enabled, iv_temperature_measurement_open_circuit_enabled,
            iv_not_close_tightly_valve_enabled, iv_water_leakage_noUser_enabled, iv_getGPS_enabled;
    //读取表数据
    private TextView tv_battery_voltage, tv_cumulative_flow, tv_reverse_cumulative_flow, tv_instantaneous_flow, tv_pressure_value, tv_time;
    //读取表数据
    private TextView tv_low_water_pressure, tv_low_water_pressure_mutation, tv_slant, tv_vibration, tv_open_cover, tv_open_battery,
            tv_low_temperature, tv_valve_status, tv_temperature_measurement_short_circuit, tv_temperature_measurement_open_circuit,
            tv_no_water, tv_outRange, tv_battery_low, tv_water_leakage, tv_reverse, tv_not_close_valve, tv_not_close_tightly_valve, tv_water_leakage_noUser;
    //读取表数据
    private ImageView iv_not_close_valve2, iv_low_water_pressure2, iv_low_temperature2, iv_low_water_pressure_mutation2,
            iv_no_water2, iv_slant2, iv_vibration2, iv_outRange2, iv_open_cover2, iv_battery_low2, iv_open_battery2, iv_water_leakage2,
            iv_valve_status2, iv_reverse2, iv_temperature_measurement_short_circuit2, iv_temperature_measurement_open_circuit2,
            iv_not_close_tightly_valve2, iv_water_leakage_noUser2;
    //当前使用数据
    private TextView tv_open_time2, tv_lock_device, tv_current_userID, tv_current_amount, tv_current_useState, tv_alarm_not_close_valve,
            tv_alarm_leakage_withoutUser;
    //读取消火栓历史记录
    private TextView tv_total_numbers1, tv_current_order_number, tv_total_numbers2, tv_user_appId, tv_open_device, tv_open_time, tv_endTime,
            tv_water_consumption;
    private EditText et_current_order_number;
    //水表ID，8位数字
    private String meterId;
    //用于计算校验码
    private String number;
    //校验码
    private String checkCode;
    //最终用于发送指令的字符串
    private String command;

    private String userId_bluetooth = "12345678";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_hydrant_data_bluetooth, container, false);
        context = getContext();
        et_meterId = view.findViewById(R.id.et_deviceId);
        tv_socket2 = view.findViewById(R.id.tv_socket2);
        tv_socket2.setMovementMethod(ScrollingMovementMethod.getInstance());
        ImageView iv_scaleUp, iv_scaleDown;
        TextView tv_located = view.findViewById(R.id.tv_located);
        TextView tv_compass = view.findViewById(R.id.tv_compass);
        iv_scaleUp = view.findViewById(R.id.iv_scaleUp);
        iv_scaleDown = view.findViewById(R.id.iv_scaleDown);
        tv_located.setOnClickListener(onClickListener);
        tv_compass.setOnClickListener(onClickListener);
        iv_scaleUp.setOnClickListener(onClickListener);
        iv_scaleDown.setOnClickListener(onClickListener);
        rl_map = view.findViewById(R.id.rl_map);
        relativeLayout_map = view.findViewById(R.id.relativelayout_map);
        scrollView_read_alarmParameters = view.findViewById(R.id.scrollview_alarm_parameters);
        scrollView_read_alarmState = view.findViewById(R.id.scrollview_read_alarm_state);
        scrollView_alarmEnabled = view.findViewById(R.id.scrollView_alarmEnabled);
        scrollView_meter_data = view.findViewById(R.id.scrollView_meter_data);
        current_use_state = view.findViewById(R.id.current_use_state);
        history_use_data = view.findViewById(R.id.history_use_data);
        tv_pressure_mutation1 = view.findViewById(R.id.tv_pressure_mutation1);
        tv_pressure_low_limit1 = view.findViewById(R.id.tv_pressure_low_limit1);
        tv_leakage_flow_rate1 = view.findViewById(R.id.tv_leakage_flow_rate1);
        tv_leaking_timeout1 = view.findViewById(R.id.tv_leaking_timeout1);
        tv_lower_temperature_limit1 = view.findViewById(R.id.tv_lower_temperature_limit1);
        tv_vibration_variation1 = view.findViewById(R.id.tv_vibration_variation1);
        tv_pressure_mutation2 = view.findViewById(R.id.tv_pressure_mutation2);
        tv_pressure_low_limit2 = view.findViewById(R.id.tv_pressure_low_limit2);
        tv_leakage_flow_rate2 = view.findViewById(R.id.tv_leakage_flow_rate2);
        tv_leaking_timeout2 = view.findViewById(R.id.tv_leaking_timeout2);
        tv_lower_temperature_limit2 = view.findViewById(R.id.tv_lower_temperature_limit2);
        tv_vibration_variation2 = view.findViewById(R.id.tv_vibration_variation2);
        ImageView iv_deletemeterid = view.findViewById(R.id.iv_deleteDeviceId);
        iv_deletemeterid.setOnClickListener(onClickListener);
        //读取报警状态页面的图标
        iv_not_close_valve = view.findViewById(R.id.iv_not_close_valve);
        iv_low_water_pressure = view.findViewById(R.id.iv_low_water_pressure);
        iv_low_temperature = view.findViewById(R.id.iv_low_temperature);
        iv_low_water_pressure_mutation = view.findViewById(R.id.iv_low_water_pressure_mutation);
        iv_no_water = view.findViewById(R.id.iv_no_water);
        iv_slant = view.findViewById(R.id.iv_slant);
        iv_vibration = view.findViewById(R.id.iv_vibration);
        iv_outRange = view.findViewById(R.id.iv_outrange);
        iv_open_cover = view.findViewById(R.id.iv_open_cover);
        iv_battery_low = view.findViewById(R.id.iv_battery_low);
        iv_open_battery = view.findViewById(R.id.iv_open_battery);
        iv_water_leakage = view.findViewById(R.id.iv_water_leakage);
        iv_valve_status = view.findViewById(R.id.iv_valve_status);
        iv_reverse = view.findViewById(R.id.iv_reverse);
        iv_temperature_measurement_short_circuit = view.findViewById(R.id.iv_temperature_measurement_short_circuit);
        iv_temperature_measurement_open_circuit = view.findViewById(R.id.iv_temperature_measurement_open_circuit);
        iv_not_close_tightly_valve = view.findViewById(R.id.iv_not_close_tightly_valve);
        iv_water_leakage_noUser = view.findViewById(R.id.iv_water_leakage_noUser);
        //读取报警使能页面的图标
        iv_gprs_reconnected_enabled = view.findViewById(R.id.iv_gprs_reconnected_enabled);
        iv_not_close_valve_enabled = view.findViewById(R.id.iv_not_close_valve_enabled);
        iv_low_water_pressure_enabled = view.findViewById(R.id.iv_low_water_pressure_enabled);
        iv_low_temperature_enabled = view.findViewById(R.id.iv_low_temperature_enabled);
        iv_low_water_pressure_mutation_enabled = view.findViewById(R.id.iv_low_water_pressure_mutation_enabled);
        iv_no_water_enabled = view.findViewById(R.id.iv_no_water_enabled);
        iv_slant_enabled = view.findViewById(R.id.iv_slant_enabled);
        iv_vibration_enabled = view.findViewById(R.id.iv_vibration_enabled);
        iv_outRange_enabled = view.findViewById(R.id.iv_outrange_enabled);
        iv_open_cover_enabled = view.findViewById(R.id.iv_open_cover_enabled);
        iv_battery_low_enabled = view.findViewById(R.id.iv_battery_low_enabled);
        iv_open_battery_enabled = view.findViewById(R.id.iv_open_battery_enabled);
        iv_water_leakage_enabled = view.findViewById(R.id.iv_water_leakage_enabled);
        iv_valve_status_enabled = view.findViewById(R.id.iv_valve_status_enabled);
        iv_reverse_enabled = view.findViewById(R.id.iv_reverse_enabled);
        iv_temperature_measurement_short_circuit_enabled = view.findViewById(R.id.iv_temperature_measurement_short_circuit_enabled);
        iv_temperature_measurement_open_circuit_enabled = view.findViewById(R.id.iv_temperature_measurement_open_circuit_enabled);
        iv_not_close_tightly_valve_enabled = view.findViewById(R.id.iv_not_close_tightly_valve_enabled);
        iv_water_leakage_noUser_enabled = view.findViewById(R.id.iv_water_leakage_noUser_enabled);
        iv_getGPS_enabled = view.findViewById(R.id.iv_getGPS_enabled);
        //读取表数据页面的控件
        tv_battery_voltage = view.findViewById(R.id.tv_battery_voltage);
        tv_cumulative_flow = view.findViewById(R.id.tv_cumulative_flow);
        tv_reverse_cumulative_flow = view.findViewById(R.id.tv_reverse_cumulative_flow);
        tv_instantaneous_flow = view.findViewById(R.id.tv_instantaneous_flow);
        tv_pressure_value = view.findViewById(R.id.tv_pressure_value);
        tv_time = view.findViewById(R.id.tv_time);
        tv_low_water_pressure = view.findViewById(R.id.tv_low_water_pressure);
        tv_low_water_pressure_mutation = view.findViewById(R.id.tv_low_water_pressure_mutation);
        tv_slant = view.findViewById(R.id.tv_slant);
        tv_vibration = view.findViewById(R.id.tv_vibration);
        tv_open_cover = view.findViewById(R.id.tv_open_cover);
        tv_open_battery = view.findViewById(R.id.tv_open_battery);
        tv_low_temperature = view.findViewById(R.id.tv_low_temperature);
        tv_valve_status = view.findViewById(R.id.tv_valve_status);
        tv_temperature_measurement_short_circuit = view.findViewById(R.id.tv_temperature_measurement_short_circuit);
        tv_temperature_measurement_open_circuit = view.findViewById(R.id.tv_temperature_measurement_open_circuit);
        tv_no_water = view.findViewById(R.id.tv_no_water);
        tv_outRange = view.findViewById(R.id.tv_outrange);
        tv_battery_low = view.findViewById(R.id.tv_battery_low);
        tv_water_leakage = view.findViewById(R.id.tv_water_leakage);
        tv_reverse = view.findViewById(R.id.tv_reverse);
        tv_not_close_valve = view.findViewById(R.id.tv_not_close_valve);
        tv_not_close_tightly_valve = view.findViewById(R.id.tv_not_close_tightly_valve);
        tv_water_leakage_noUser = view.findViewById(R.id.tv_water_leakage_noUser);
        iv_not_close_valve2 = view.findViewById(R.id.iv_not_close_valve2);
        iv_low_water_pressure2 = view.findViewById(R.id.iv_low_water_pressure2);
        iv_low_temperature2 = view.findViewById(R.id.iv_low_temperature2);
        iv_low_water_pressure_mutation2 = view.findViewById(R.id.iv_low_water_pressure_mutation2);
        iv_no_water2 = view.findViewById(R.id.iv_no_water2);
        iv_slant2 = view.findViewById(R.id.iv_slant2);
        iv_vibration2 = view.findViewById(R.id.iv_vibration2);
        iv_outRange2 = view.findViewById(R.id.iv_outrange2);
        iv_open_cover2 = view.findViewById(R.id.iv_open_cover2);
        iv_battery_low2 = view.findViewById(R.id.iv_battery_low2);
        iv_open_battery2 = view.findViewById(R.id.iv_open_battery2);
        iv_water_leakage2 = view.findViewById(R.id.iv_water_leakage2);
        iv_valve_status2 = view.findViewById(R.id.iv_valve_status2);
        iv_reverse2 = view.findViewById(R.id.iv_reverse2);
        iv_temperature_measurement_short_circuit2 = view.findViewById(R.id.iv_temperature_measurement_short_circuit2);
        iv_temperature_measurement_open_circuit2 = view.findViewById(R.id.iv_temperature_measurement_open_circuit2);
        iv_not_close_tightly_valve2 = view.findViewById(R.id.iv_not_close_tightly_valve2);
        iv_water_leakage_noUser2 = view.findViewById(R.id.iv_water_leakage_noUser2);
        //读取当前使用数据的控件
        tv_open_time2 = view.findViewById(R.id.tv_open_time2);
        tv_lock_device = view.findViewById(R.id.tv_lock_device);
        tv_current_userID = view.findViewById(R.id.tv_current_userID);
        tv_current_amount = view.findViewById(R.id.tv_current_amount);
        tv_current_useState = view.findViewById(R.id.tv_current_useState);
        tv_alarm_not_close_valve = view.findViewById(R.id.tv_alarm_not_close_valve);
        tv_alarm_leakage_withoutUser = view.findViewById(R.id.tv_alarm_leakage_withoutUser);
        //读取历史使用记录
        tv_total_numbers1 = view.findViewById(R.id.tv_total_numbers1);
        tv_current_order_number = view.findViewById(R.id.tv_current_order_number);
        tv_total_numbers2 = view.findViewById(R.id.tv_total_numbers2);
        tv_user_appId = view.findViewById(R.id.tv_user_appid);
        tv_open_device = view.findViewById(R.id.tv_open_device);
        tv_open_time = view.findViewById(R.id.tv_open_time);
        tv_endTime = view.findViewById(R.id.tv_endTime);
        tv_water_consumption = view.findViewById(R.id.tv_water_consumption);
        et_current_order_number = view.findViewById(R.id.et_current_order_number);
        Button btn_search_history = view.findViewById(R.id.btn_search_history);
        btn_search_history.setOnClickListener(onClickListener);
        et_current_order_number.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!temp.toString().equals("") && !tv_total_numbers1.getText().toString().equals("n") && Integer.parseInt(temp.toString()) > Integer.parseInt(tv_total_numbers1.getText().toString())) {
                    CommonUtils.showToast(context, "查询序号不能大于总记录数");
                    et_current_order_number.setText("");
                }
            }
        });
        ImageView iv_scanCode = view.findViewById(R.id.iv_scanCode);
        iv_scanCode.setOnClickListener(onClickListener);
        //按钮
        Button btn_read_alarm_parameters, tv_set_alarm_parameters, btn_read_alarm_state, btn_read_meter_data,
                btn_read_alarm_enabled, btn_set_alarm_enabled, btn_readGPS, btn_refreshGPS, btn_read_usestate, btn_read_useinformation;
        btn_read_alarm_parameters = view.findViewById(R.id.btn_read_alarm_parameters);
        tv_set_alarm_parameters = view.findViewById(R.id.btn_set_alarm_parameters);
        btn_read_alarm_state = view.findViewById(R.id.btn_read_alarm_state);
        btn_read_meter_data = view.findViewById(R.id.btn_read_meter_data);
        btn_read_alarm_enabled = view.findViewById(R.id.btn_read_alarm_enabled);
        btn_set_alarm_enabled = view.findViewById(R.id.btn_set_alarm_enabled);
        btn_readGPS = view.findViewById(R.id.btn_readGPS);
        btn_refreshGPS = view.findViewById(R.id.btn_refreshGPS);
        btn_read_usestate = view.findViewById(R.id.btn_read_usestate);
        btn_read_useinformation = view.findViewById(R.id.btn_read_useinformation);
        btn_open = view.findViewById(R.id.btn_open);
        btn_close = view.findViewById(R.id.btn_close);
        tv_set_alarm_parameters.setOnClickListener(onClickListener);
        btn_read_alarm_parameters.setOnClickListener(onClickListener);
        btn_read_alarm_state.setOnClickListener(onClickListener);
        btn_read_meter_data.setOnClickListener(onClickListener);
        btn_read_alarm_enabled.setOnClickListener(onClickListener);
        btn_set_alarm_enabled.setOnClickListener(onClickListener);
        btn_readGPS.setOnClickListener(onClickListener);
        btn_refreshGPS.setOnClickListener(onClickListener);
        btn_read_usestate.setOnClickListener(onClickListener);
        btn_read_useinformation.setOnClickListener(onClickListener);
        btn_open.setOnClickListener(onClickListener);
        btn_close.setOnClickListener(onClickListener);
        et_meterId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                initAlarmData();
                initAlarmStateData();
                initAlarmEnabledData();
                initMeterData();
                initCurrentUseState();
                initHistoryUseData();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    //初始化报警参数数据
    private void initAlarmData() {
        //清空之前获取的数据
        tv_pressure_mutation1.setText("—");
        tv_pressure_low_limit1.setText("—");
        tv_leakage_flow_rate1.setText("—");
        tv_leaking_timeout1.setText("—");
        tv_lower_temperature_limit1.setText("—");
        tv_vibration_variation1.setText("—");
        //上次设置的值
        tv_pressure_mutation2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Mutation", "—"));
        tv_pressure_low_limit2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Low_Limit", "—"));
        tv_leakage_flow_rate2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leakage_Flow_Rate", "—"));
        tv_leaking_timeout2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leaking_Timeout", "—"));
        tv_lower_temperature_limit2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Lower_Temperature_Limit", "—"));
        tv_vibration_variation2.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Vibration_Variation", "—"));
        //已读取报警参数设为false
        SharedPreferencesUtils.getInstance().saveData("has_read_alarm_parameters", false);
    }

    //初始化报警状态报警标志
    private void initAlarmStateData() {
        iv_not_close_valve.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure.setImageResource(R.drawable.circle_wait);
        iv_low_temperature.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure_mutation.setImageResource(R.drawable.circle_wait);
        iv_no_water.setImageResource(R.drawable.circle_wait);
        iv_water_leakage_noUser.setImageResource(R.drawable.circle_wait);
        iv_slant.setImageResource(R.drawable.circle_wait);
        iv_vibration.setImageResource(R.drawable.circle_wait);
        iv_outRange.setImageResource(R.drawable.circle_wait);
        iv_open_cover.setImageResource(R.drawable.circle_wait);
        iv_battery_low.setImageResource(R.drawable.circle_wait);
        iv_open_battery.setImageResource(R.drawable.circle_wait);
        iv_water_leakage.setImageResource(R.drawable.circle_wait);
        iv_valve_status.setImageResource(R.drawable.circle_wait);
        iv_reverse.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_short_circuit.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_open_circuit.setImageResource(R.drawable.circle_wait);
        iv_not_close_tightly_valve.setImageResource(R.drawable.circle_wait);
    }

    //初始化报警使能报警标志
    private void initAlarmEnabledData() {
        iv_gprs_reconnected_enabled.setImageResource(R.drawable.circle_wait);
        iv_not_close_valve_enabled.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure_enabled.setImageResource(R.drawable.circle_wait);
        iv_low_temperature_enabled.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure_mutation_enabled.setImageResource(R.drawable.circle_wait);
        iv_water_leakage_noUser_enabled.setImageResource(R.drawable.circle_wait);
        iv_getGPS_enabled.setImageResource(R.drawable.circle_wait);
        iv_no_water_enabled.setImageResource(R.drawable.circle_wait);
        iv_slant_enabled.setImageResource(R.drawable.circle_wait);
        iv_vibration_enabled.setImageResource(R.drawable.circle_wait);
        iv_outRange_enabled.setImageResource(R.drawable.circle_wait);
        iv_open_cover_enabled.setImageResource(R.drawable.circle_wait);
        iv_battery_low_enabled.setImageResource(R.drawable.circle_wait);
        iv_open_battery_enabled.setImageResource(R.drawable.circle_wait);
        iv_water_leakage_enabled.setImageResource(R.drawable.circle_wait);
        iv_valve_status_enabled.setImageResource(R.drawable.circle_wait);
        iv_reverse_enabled.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_short_circuit_enabled.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_open_circuit_enabled.setImageResource(R.drawable.circle_wait);
        iv_not_close_tightly_valve_enabled.setImageResource(R.drawable.circle_wait);
        //将已读取报警使能设为false
        SharedPreferencesUtils.getInstance().saveData("has_read_alarm_enabled", false);
    }

    //初始化表数据
    private void initMeterData() {
        tv_battery_voltage.setText("—");
        tv_cumulative_flow.setText("—");
        tv_reverse_cumulative_flow.setText("—");
        tv_instantaneous_flow.setText("—");
        tv_pressure_value.setText("—");
        tv_time.setText("—");
        tv_low_water_pressure.setText("—");
        tv_low_water_pressure_mutation.setText("—");
        tv_slant.setText("—");
        tv_vibration.setText("—");
        tv_open_cover.setText("—");
        tv_open_battery.setText("—");
        tv_low_temperature.setText("—");
        tv_valve_status.setText("—");
        tv_temperature_measurement_short_circuit.setText("—");
        tv_temperature_measurement_open_circuit.setText("—");
        tv_no_water.setText("—");
        tv_outRange.setText("—");
        tv_battery_low.setText("—");
        tv_water_leakage.setText("—");
        tv_reverse.setText("—");
        tv_not_close_valve.setText("—");
        tv_not_close_tightly_valve.setText("—");
        tv_water_leakage_noUser.setText("—");
        iv_not_close_valve2.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure2.setImageResource(R.drawable.circle_wait);
        iv_low_temperature2.setImageResource(R.drawable.circle_wait);
        iv_low_water_pressure_mutation2.setImageResource(R.drawable.circle_wait);
        iv_no_water2.setImageResource(R.drawable.circle_wait);
        iv_water_leakage_noUser2.setImageResource(R.drawable.circle_wait);
        iv_slant2.setImageResource(R.drawable.circle_wait);
        iv_vibration2.setImageResource(R.drawable.circle_wait);
        iv_outRange2.setImageResource(R.drawable.circle_wait);
        iv_open_cover2.setImageResource(R.drawable.circle_wait);
        iv_battery_low2.setImageResource(R.drawable.circle_wait);
        iv_open_battery2.setImageResource(R.drawable.circle_wait);
        iv_water_leakage2.setImageResource(R.drawable.circle_wait);
        iv_valve_status2.setImageResource(R.drawable.circle_wait);
        iv_reverse2.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_short_circuit2.setImageResource(R.drawable.circle_wait);
        iv_temperature_measurement_open_circuit2.setImageResource(R.drawable.circle_wait);
        iv_not_close_tightly_valve2.setImageResource(R.drawable.circle_wait);
    }

    //初始化历史使用数据页面
    private void initHistoryUseData() {
        tv_total_numbers1.setText("n");
        tv_current_order_number.setText("—");
        tv_total_numbers2.setText("—");
        tv_user_appId.setText("—");
        tv_open_device.setText("—");
        tv_open_time.setText("—");
        tv_endTime.setText("—");
        tv_water_consumption.setText("—");
        et_current_order_number.setText("");
    }

    //初始化当前使用数据页面
    private void initCurrentUseState() {
        tv_open_time2.setText("—");
        tv_lock_device.setText("—");
        tv_current_userID.setText("—");
        tv_current_amount.setText("—");
        tv_current_useState.setText("—");
        tv_alarm_not_close_valve.setText("—");
        tv_alarm_leakage_withoutUser.setText("—");
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            et_meterId.clearFocus();
            switch (v.getId()) {
                case R.id.iv_deleteDeviceId:
                    et_meterId.setText("");
                    et_meterId.requestFocus();
                    break;
                case R.id.btn_read_alarm_parameters:
                    //读报警参数
                    initAlarmData();
                    scrollView_read_alarmParameters.setVisibility(View.VISIBLE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    readAlarm();
                    break;
                case R.id.btn_set_alarm_parameters:
                    //设置报警参数
                    scrollView_read_alarmParameters.setVisibility(View.VISIBLE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    showInputAlarmDialog();
                    break;
                case R.id.btn_read_alarm_state:
                    //读报警状态
                    initAlarmStateData();
                    readAlarmState();
                    scrollView_read_alarmState.setVisibility(View.VISIBLE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    break;
                case R.id.btn_read_meter_data:
                    //读取表数据
                    initMeterData();
                    readMeterData();
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.VISIBLE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    break;
                case R.id.btn_read_alarm_enabled:
                    //读取报警使能
                    initAlarmEnabledData();
                    scrollView_alarmEnabled.setVisibility(View.VISIBLE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    readAlarmEnabled();
                    break;
                case R.id.btn_set_alarm_enabled:
                    //设置报警使能
                    scrollView_alarmEnabled.setVisibility(View.VISIBLE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    showInputAlarmEnabledDialog();
                    break;
                case R.id.btn_readGPS:
                    //读取GPS信息
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.VISIBLE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    readGPS();
                    break;
                case R.id.btn_refreshGPS:
                    //刷新GPS信息
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.VISIBLE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    refreshGPS();
                    break;
                case R.id.btn_read_usestate:
                    //读取当前使用数据
                    initCurrentUseState();
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.VISIBLE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.GONE);
                    readCurrentUseState();
                    break;
                case R.id.btn_read_useinformation:
                    //读取历史使用记录
                    initHistoryUseData();
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.VISIBLE);
                    tv_socket2.setVisibility(View.GONE);
                    //默认查询最近一条
                    readUseInformation("0000");
                    break;
                case R.id.btn_search_history:
                    //读取历史具体某一条使用记录
                    String order = reData(String.valueOf(Integer.parseInt(et_current_order_number.getText().toString()) - 1), 1);
                    readUseInformation(order);
                    break;
                case R.id.btn_open:
                    //开启阀门锁
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.VISIBLE);
                    changeLock("F000");
                    break;
                case R.id.btn_close:
                    //关闭阀门锁
                    scrollView_alarmEnabled.setVisibility(View.GONE);
                    scrollView_read_alarmState.setVisibility(View.GONE);
                    scrollView_read_alarmParameters.setVisibility(View.GONE);
                    scrollView_meter_data.setVisibility(View.GONE);
                    relativeLayout_map.setVisibility(View.GONE);
                    current_use_state.setVisibility(View.GONE);
                    history_use_data.setVisibility(View.GONE);
                    tv_socket2.setVisibility(View.VISIBLE);
                    changeLock("0F00");
                    break;
                default:
                    break;
            }
        }
    };

    //开关锁方法
    private void changeLock(final String status) {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "AAAAAAAA";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112408901F" + userId_bluetooth + status;
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读当前使用状态
    private void readUseInformation(final String order) {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112504901F" + order;
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读当前使用状态
    private void readCurrentUseState() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112103901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //触发GPS信息
    private void refreshGPS() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112703901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读GPS信息
    private void readGPS() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112203901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读取表数据
    private void readMeterData() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011110103901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读取报警使能
    private void readAlarmEnabled() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112603901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //显示设置报警使能设置的对话框
    private void showInputAlarmEnabledDialog() {
        if (et_meterId.getText().toString().length() == 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.showToast(context, "请先扫描条形码或输入信息");
                }
            });
        } else if (et_meterId.getText().toString().length() != 8) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.showToast(context, "信息有误，请核对后再操作");
                }
            });
        } else if (!(boolean) SharedPreferencesUtils.getInstance().getData("has_read_alarm_enabled", false)) {
            CommonUtils.showToast(context, "请先读取报警使能");
        } else {
            final InputAlarmEnabledDialog inputAlarmEnabledDialog = new InputAlarmEnabledDialog(context);
            inputAlarmEnabledDialog.setCancelable(false);
            final CheckBox cb_low_water_pressure_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_low_water_pressure_enabled);
            final CheckBox cb_low_temperature_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_low_temperature_enabled);
            final CheckBox cb_low_water_pressure_mutation_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_water_pressure_mutation_enabled);
            final CheckBox cb_no_water_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_no_water_enabled);
            final CheckBox cb_slant_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_slant_enabled);
            final CheckBox cb_vibration_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_vibration_enabled);
            final CheckBox cb_outrange_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_outrange_enabled);
            final CheckBox cb_open_cover_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_open_cover_enabled);
            final CheckBox cb_battery_low_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_battery_low_enabled);
            final CheckBox cb_open_battery_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_open_battery_enabled);
            final CheckBox cb_water_leakage_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_water_leakage_enabled);
            final CheckBox cb_valve_status_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_valve_status_enabled);
            final CheckBox cb_reverse_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_reverse_enabled);
            final CheckBox cb_temperature_measurement_short_circuit_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_temperature_measurement_short_circuit_enabled);
            final CheckBox cb_temperature_measurement_open_circuit_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_temperature_measurement_open_circuit_enabled);
            final CheckBox cb_not_close_valve_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_not_close_valve_enabled);
            final CheckBox cb_not_close_tightly_valve_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_not_close_tightly_valve_enabled);
            final CheckBox cb_water_leakage_noUser_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_water_leakage_noUser_enabled);
            final CheckBox cb_getGPS_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_getGPS_enabled);
            final CheckBox cb_gprs_reconnected_enabled =  inputAlarmEnabledDialog.findViewById(R.id.cb_gprs_reconnected_enabled);
            cb_low_water_pressure_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("low_water_pressure_enabled", false));
            cb_low_temperature_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("low_temperature_enabled", false));
            cb_low_water_pressure_mutation_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("low_water_pressure_mutation_enabled", false));
            cb_no_water_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("no_water_enabled", false));
            cb_slant_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("slant_enabled", false));
            cb_vibration_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("vibration_enabled", false));
            cb_outrange_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("outrange_enabled", false));
            cb_open_cover_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("open_cover_enabled", false));
            cb_battery_low_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("battery_low_enabled", false));
            cb_open_battery_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("open_battery_enabled", false));
            cb_water_leakage_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("water_leakage_enabled", false));
            cb_valve_status_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("valve_status_enabled", false));
            cb_reverse_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("reverse_enabled", false));
            cb_temperature_measurement_short_circuit_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("temperature_measurement_short_circuit_enabled", false));
            cb_temperature_measurement_open_circuit_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("temperature_measurement_open_circuit_enabled", false));
            cb_not_close_valve_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("not_close_valve_enabled", false));
            cb_not_close_tightly_valve_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("not_close_tightly_valve_enabled", false));
            cb_water_leakage_noUser_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("water_leakage_noUser_enabled", false));
            cb_getGPS_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("getGPS_enabled", false));
            cb_gprs_reconnected_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("gprs_reconnected_enabled", false));
            final Button btn_useSave = inputAlarmEnabledDialog.findViewById(R.id.btn_useSave);
            btn_useSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cb_low_water_pressure_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_water_pressure_enabled", false));
                    cb_low_temperature_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_temperature_enabled", false));
                    cb_low_water_pressure_mutation_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_water_pressure_mutation_enabled", false));
                    cb_no_water_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_no_water_enabled", false));
                    cb_slant_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_slant_enabled", false));
                    cb_vibration_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_vibration_enabled", false));
                    cb_outrange_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_outrange_enabled", false));
                    cb_open_cover_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_open_cover_enabled", false));
                    cb_battery_low_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_battery_low_enabled", false));
                    cb_open_battery_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_open_battery_enabled", false));
                    cb_water_leakage_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_water_leakage_enabled", false));
                    cb_valve_status_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_valve_status_enabled", false));
                    cb_reverse_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_reverse_enabled", false));
                    cb_temperature_measurement_short_circuit_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_temperature_measurement_short_circuit_enabled", false));
                    cb_temperature_measurement_open_circuit_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_temperature_measurement_open_circuit_enabled", false));
                    cb_not_close_valve_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_not_close_valve_enabled", false));
                    cb_not_close_tightly_valve_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_not_close_tightly_valve_enabled", false));
                    cb_water_leakage_noUser_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_water_leakage_noUser_enabled", false));
                    cb_getGPS_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_getGPS_enabled", false));
                    cb_gprs_reconnected_enabled.setChecked((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_gprs_reconnected_enabled", false));
                }
            });
            inputAlarmEnabledDialog.setOnDialogClickListener(new InputAlarmEnabledDialog.OnDialogClickListener() {
                @Override
                public void onOKClick() {
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_low_water_pressure_enabled", cb_low_water_pressure_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_low_temperature_enabled", cb_low_temperature_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_low_water_pressure_mutation_enabled", cb_low_water_pressure_mutation_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_no_water_enabled", cb_no_water_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_slant_enabled", cb_slant_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_vibration_enabled", cb_vibration_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_outrange_enabled", cb_outrange_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_open_cover_enabled", cb_open_cover_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_battery_low_enabled", cb_battery_low_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_open_battery_enabled", cb_open_battery_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_water_leakage_enabled", cb_water_leakage_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_valve_status_enabled", cb_valve_status_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_reverse_enabled", cb_reverse_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_temperature_measurement_short_circuit_enabled", cb_temperature_measurement_short_circuit_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_temperature_measurement_open_circuit_enabled", cb_temperature_measurement_open_circuit_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_not_close_valve_enabled", cb_not_close_valve_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_not_close_tightly_valve_enabled", cb_not_close_tightly_valve_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_water_leakage_noUser_enabled", cb_water_leakage_noUser_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_getGPS_enabled", cb_getGPS_enabled.isChecked());
                    SharedPreferencesUtils.getInstance().saveData("LastSetting_gprs_reconnected_enabled", cb_gprs_reconnected_enabled.isChecked());
                    //清空之前的数据
                    initAlarmEnabledData();
                    //设置报警使能
                    setAlarmEnabled();
                }

                @Override
                public void onCancelClick() {

                }
            });
            inputAlarmEnabledDialog.show();
        }
    }

    private String boolean2String(boolean checked) {
        if (checked) {
            return "1";
        } else {
            return "0";
        }
    }

    // 显示设置报警参数设置的对话框
    private void showInputAlarmDialog() {
        if (et_meterId.getText().toString().length() == 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.showToast(context, "请先扫描条形码或输入信息");
                }
            });
        } else if (et_meterId.getText().toString().length() != 8) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.showToast(context, "信息有误，请核对后再操作");
                }
            });
        } else if (!(boolean) SharedPreferencesUtils.getInstance().getData("has_read_alarm_parameters", false)) {
            CommonUtils.showToast(context, "请先读取报警参数");
        } else {
            final InputMeterAlarmDialog inputMeterAlarmDialog = new InputMeterAlarmDialog(context);
            inputMeterAlarmDialog.setCancelable(false);
            final EditText et_pressure_mutation =  inputMeterAlarmDialog.findViewById(R.id.et_pressure_mutation);
            final EditText et_pressure_low_limit =  inputMeterAlarmDialog.findViewById(R.id.et_pressure_low_limit);
            final EditText et_leakage_flow_rate =  inputMeterAlarmDialog.findViewById(R.id.et_leakage_flow_rate);
            final EditText et_leaking_timeout =  inputMeterAlarmDialog.findViewById(R.id.et_leaking_timeout);
            final EditText et_lower_temperature_limit =  inputMeterAlarmDialog.findViewById(R.id.et_lower_temperature_limit);
            final EditText et_vibration_variation =  inputMeterAlarmDialog.findViewById(R.id.et_vibration_variation);
            et_pressure_mutation.setText(tv_pressure_mutation1.getText().toString());
            et_pressure_low_limit.setText(tv_pressure_low_limit1.getText().toString());
            et_leakage_flow_rate.setText(tv_leakage_flow_rate1.getText().toString());
            et_leaking_timeout.setText(tv_leaking_timeout1.getText().toString());
            et_lower_temperature_limit.setText(tv_lower_temperature_limit1.getText().toString());
            et_vibration_variation.setText(tv_vibration_variation1.getText().toString());
            et_pressure_mutation.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_pressure_mutation.setText("");
                    }
                    editStart = et_pressure_mutation.getSelectionStart();
                    editEnd = et_pressure_mutation.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_pressure_mutation.setText(s);
                        et_pressure_mutation.setSelection(tempSelection);
                    }
                    if (temp.toString().length() != 0 && Double.parseDouble(temp.toString()) > 10) {
                        CommonUtils.showToast(context, "压力突变阈值范围:0~10");
                        et_pressure_mutation.setText("");
                    }
                }
            });
            et_pressure_low_limit.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_pressure_low_limit.setText("");
                    }
                    editStart = et_pressure_low_limit.getSelectionStart();
                    editEnd = et_pressure_low_limit.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_pressure_low_limit.setText(s);
                        et_pressure_low_limit.setSelection(tempSelection);
                    }
                    if (temp.toString().length() != 0 && Double.parseDouble(temp.toString()) > 10) {
                        CommonUtils.showToast(context, "压力下限阈值范围:0~10");
                        et_pressure_low_limit.setText("");
                    }
                }
            });
            et_leakage_flow_rate.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_leakage_flow_rate.setText("");
                    }
                    editStart = et_leakage_flow_rate.getSelectionStart();
                    editEnd = et_leakage_flow_rate.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_leakage_flow_rate.setText(s);
                        et_leakage_flow_rate.setSelection(tempSelection);
                    }
                }
            });
            et_leaking_timeout.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_leaking_timeout.setText("");
                    }
                    editStart = et_leaking_timeout.getSelectionStart();
                    editEnd = et_leaking_timeout.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_leaking_timeout.setText(s);
                        et_leaking_timeout.setSelection(tempSelection);
                    }
                }
            });
            et_lower_temperature_limit.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_lower_temperature_limit.setText("");
                    }
                    editStart = et_lower_temperature_limit.getSelectionStart();
                    editEnd = et_lower_temperature_limit.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_lower_temperature_limit.setText(s);
                        et_lower_temperature_limit.setSelection(tempSelection);
                    }
                }
            });
            et_vibration_variation.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = s;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && s.toString().equals(".")) {
                        et_vibration_variation.setText("");
                    }
                    editStart = et_vibration_variation.getSelectionStart();
                    editEnd = et_vibration_variation.getSelectionEnd();
                    if (temp.toString().replace(".", "").length() > 4) {
                        CommonUtils.showToast(context, "最多输入4位数(不含小数点)！");
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et_vibration_variation.setText(s);
                        et_vibration_variation.setSelection(tempSelection);
                    }
                }
            });
            setCharSequence(et_pressure_mutation.getText());
            setCharSequence(et_pressure_low_limit.getText());
            setCharSequence(et_leakage_flow_rate.getText());
            setCharSequence(et_leaking_timeout.getText());
            setCharSequence(et_lower_temperature_limit.getText());
            setCharSequence(et_vibration_variation.getText());
            ImageView iv_delete_pressure_mutation =  inputMeterAlarmDialog.findViewById(R.id.iv_delete_pressure_mutation);
            ImageView iv_delete_pressure_low_limit =  inputMeterAlarmDialog.findViewById(R.id.iv_delete_pressure_low_limit);
            ImageView iv_delete_leakage_flow_rate =  inputMeterAlarmDialog.findViewById(R.id.iv_delete_leakage_flow_rate);
            ImageView iv_delete_leaking_timeout =  inputMeterAlarmDialog.findViewById(R.id.iv_delete_leaking_timeout);
            ImageView iv_delete_lower_temperature_limit =  inputMeterAlarmDialog.findViewById(R.id.iv_delete_lower_temperature_limit);
            ImageView iv_delete_vibration_variation =inputMeterAlarmDialog.findViewById(R.id.iv_delete_vibration_variation);
            Button btn_useSave = inputMeterAlarmDialog.findViewById(R.id.btn_useSave);
            iv_delete_pressure_mutation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_pressure_mutation.setText("");
                }
            });
            iv_delete_pressure_low_limit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_pressure_low_limit.setText("");
                }
            });
            iv_delete_leakage_flow_rate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_leakage_flow_rate.setText("");
                }
            });
            iv_delete_leaking_timeout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_leaking_timeout.setText("");
                }
            });
            iv_delete_lower_temperature_limit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_lower_temperature_limit.setText("");
                }
            });
            iv_delete_vibration_variation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_vibration_variation.setText("");
                }
            });
            inputMeterAlarmDialog.setOnDialogClickListener(new InputMeterAlarmDialog.OnDialogClickListener() {
                @Override
                public void onOKClick() {
                    if (et_pressure_mutation.getText().toString().equals("") ||
                            et_pressure_low_limit.getText().toString().equals("") ||
                            et_leakage_flow_rate.getText().toString().equals("") ||
                            et_leaking_timeout.getText().toString().equals("") ||
                            et_lower_temperature_limit.getText().toString().equals("") ||
                            et_vibration_variation.getText().toString().equals("")) {
                        CommonUtils.showToast(context, "数据填写不全，无法提交");
                    } else {
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Pressure_Mutation", et_pressure_mutation.getText().toString());
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Pressure_Low_Limit", et_pressure_low_limit.getText().toString());
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Leakage_Flow_Rate", et_leakage_flow_rate.getText().toString());
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Leaking_Timeout", et_leaking_timeout.getText().toString());
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Lower_Temperature_Limit", et_lower_temperature_limit.getText().toString());
                        SharedPreferencesUtils.getInstance().saveData("LastSetting_Vibration_Variation", et_vibration_variation.getText().toString());
                        //清空数据
                        initAlarmData();
                        //提交数据
                        setAlarm();
                    }
                }

                @Override
                public void onCancelClick() {
                }
            });
            btn_useSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Mutation", "").equals("")) {
                        et_pressure_mutation.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Mutation", ""));
                        et_pressure_low_limit.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Low_Limit", ""));
                        et_leakage_flow_rate.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leakage_Flow_Rate", ""));
                        et_leaking_timeout.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leaking_Timeout", ""));
                        et_lower_temperature_limit.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Lower_Temperature_Limit", ""));
                        et_vibration_variation.setText((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Vibration_Variation", ""));
                        setCharSequence(et_pressure_mutation.getText());
                        setCharSequence(et_pressure_low_limit.getText());
                        setCharSequence(et_leakage_flow_rate.getText());
                        setCharSequence(et_leaking_timeout.getText());
                        setCharSequence(et_lower_temperature_limit.getText());
                        setCharSequence(et_vibration_variation.getText());
                    } else {
                        CommonUtils.showToast(context, "没有历史设置记录，请手动输入");
                    }
                }
            });
            inputMeterAlarmDialog.show();
        }
    }

    //读取报警参数的方法
    private void readAlarm() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112303901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //读取报警状态的方法
    private void readAlarmState() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        number = "6859" + meterId + "0011112003901F00";
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //设置报警参数的方法
    private void setAlarm() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        //获取输入的参数值
        String pressure_mutation = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Mutation", "0000"), 100);
        String pressure_low_limit = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Pressure_Low_Limit", "0000"), 100);
        String leakage_flow_rate = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leakage_Flow_Rate", "0000"), 100);
        String leaking_timeout = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Leaking_Timeout", "0000"), 1);
        String lower_temperature_limit = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Lower_Temperature_Limit", "0000"), 100);
        String vibration_variation = reData((String) SharedPreferencesUtils.getInstance().getData("LastSetting_Vibration_Variation", "0000"), 1);
        number = "6859" + meterId + "001111230E902F" + pressure_mutation + pressure_low_limit + leakage_flow_rate + leaking_timeout + lower_temperature_limit + vibration_variation;
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //设置报警使能的方法
    private void setAlarmEnabled() {
        meterId = et_meterId.getText().toString();
        if (TextUtils.isEmpty(meterId)) {
            meterId = "FFFFFFFF";
        } else if (meterId.length() != 8) {
            CommonUtils.showToast(context, "请输入正确的消火栓编号");
            return;
        }
        //获取输入的参数值
        String low_water_pressure_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_water_pressure_enabled", false));
        String low_temperature_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_temperature_enabled", false));
        String low_water_pressure_mutation_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_low_water_pressure_mutation_enabled", false));
        String no_water_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_no_water_enabled", false));
        String slant_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_slant_enabled", false));
        String PCB_breakdown_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_PCB_breakdown_enabled", false));
        String vibration_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_vibration_enabled", false));
        String outrange_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_outrange_enabled", false));
        String open_cover_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_open_cover_enabled", false));
        String battery_low_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_battery_low_enabled", false));
        String open_battery_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_open_battery_enabled", false));
        String water_leakage_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_water_leakage_enabled", false));
        String valve_status_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_valve_status_enabled", false));
        String reverse_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_reverse_enabled", false));
        String temperature_measurement_short_circuit_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_temperature_measurement_short_circuit_enabled", false));
        String not_close_valve_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_not_close_valve_enabled", false));
        String temperature_measurement_open_circuit_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_temperature_measurement_open_circuit_enabled", false));
        String not_close_tightly_valve_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_not_close_tightly_valve_enabled", false));
        String water_leakage_noUser_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_water_leakage_noUser_enabled", false));
        String getGPS_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_getGPS_enabled", false));
        String gprs_reconnected_enabled = boolean2String((boolean) SharedPreferencesUtils.getInstance().getData("LastSetting_gprs_reconnected_enabled", false));
        String state1 = valve_status_enabled + low_temperature_enabled + open_battery_enabled + open_cover_enabled + vibration_enabled + slant_enabled + low_water_pressure_mutation_enabled + low_water_pressure_enabled;
        LogUtils.d("setAlarmEnabled", "状态报警1" + state1);
        String state2 = reverse_enabled + water_leakage_enabled + battery_low_enabled + outrange_enabled + PCB_breakdown_enabled + no_water_enabled + temperature_measurement_open_circuit_enabled + temperature_measurement_short_circuit_enabled;
        LogUtils.d("setAlarmEnabled", "状态报警1" + state2);
        String state3 = getGPS_enabled + gprs_reconnected_enabled + "000" + water_leakage_noUser_enabled + not_close_tightly_valve_enabled + not_close_valve_enabled;
        LogUtils.d("setAlarmEnabled", "状态报警1" + state3);
        number = "6859" + meterId + "0011112605902F" + binary2hex(state1) + binary2hex(state2) + binary2hex(state3);
        checkCode = AnalysisUtils.getCSSum(number, 0);
        command = number + checkCode + "16";
        BluetoothToolsMainActivity.writeData(command);
    }

    //将输入的信息转变成需要的16进制
    public static String reData(String str, int n) {
        double str0 = Double.valueOf(str) * n;
        int strx = (int) str0;
        String str1 = String.valueOf(strx);
        String str2 = addZeroForNum(str1, 4);//补全四位
        String str4 = str2.toUpperCase();//转大写
        return str4.substring(2, 4) + str4.substring(0, 2);
    }

    //位数不够补零
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(0, "0");//左补0
//		    	sb.insert(strLen,"0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200) {
            String scanResult = data.getStringExtra("text");
            //判断提取后的数字字符串是否为8位(表号）
            if (scanResult.length() == 8) {
                et_meterId.setText(scanResult);
                setCharSequence(et_meterId.getText());
                CommonUtils.showToast(context, "识别成功");
            } else {
                CommonUtils.showToast(context, "不是正确的条形码");
            }
        }
    }

    //将EditText光标置于末尾
    public void setCharSequence(CharSequence charSequence) {
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    public static String binary2hex(String binary) {
        int length = binary.length();
        int temp = length % 4;
        // 每四位2进制数字对应一位16进制数字
        // 补足4位
        if (temp != 0) {
            for (int i = 0; i < 4 - temp; i++) {
                binary = "0" + binary;
            }
        }
        // 重新计算长度
        length = binary.length();
        StringBuilder sb = new StringBuilder();
        // 每4个二进制数为一组进行计算
        for (int i = 0; i < length / 4; i++) {
            int num = 0;
            // 将4个二进制数转成整数
            for (int j = i * 4; j < i * 4 + 4; j++) {
                num <<= 1;// 左移
                num |= (binary.charAt(j) - '0');// 或运算
            }
            // 直接找到该整数对应的16进制，这里不用switch来做
            sb.append(hexStr[num]);
        }
        return sb.toString();
    }


    static String[] hexStr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

}
