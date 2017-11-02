package cn.njmeter.bluetooth.fragment.collector;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.interfaces.OnMultiClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;

public class GprsSettingFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.btn_readCollectorParameters)
    Button btn_readCollectorParameters;
    @BindView(R.id.btn_setCollectorParameters)
    Button btn_setCollectorParameters;
    @BindView(R.id.btn_turnOn_light)
    Button btn_turnOn_light;
    @BindView(R.id.btn_initParameters)
    Button btn_initParameters;

    @BindView(R.id.set_spinner_mode)
    Spinner set_spinner_mode;
    @BindView(R.id.set_enabled_batch_mode)
    Spinner set_enabled_batch_mode;
    @BindView(R.id.set_startTime_morning)
    Spinner set_startTime_morning;
    @BindView(R.id.set_endTime_morning)
    Spinner set_endTime_morning;
    @BindView(R.id.set_startTime_afternoon)
    Spinner set_startTime_afternoon;
    @BindView(R.id.set_endTime_afternoon)
    Spinner set_endTime_afternoon;
    @BindView(R.id.set_cycle_upload_shortConnection)
    Spinner set_cycle_upload_shortConnection;
    @BindView(R.id.set_time_shortConnection)
    Spinner set_time_shortConnection;
    @BindView(R.id.set_interval_readMeter_shortConnection)
    Spinner set_interval_readMeter_shortConnection;
    @BindView(R.id.set_interval_readMeter_external_power_supply)
    Spinner set_interval_readMeter_external_power_supply;
    @BindView(R.id.set_interval_longMode_longConnection)
    Spinner set_interval_longMode_longConnection;
    @BindView(R.id.set_interval_shortMode_longConnection)
    Spinner get_interval_shortMode_longConnection;

    private String[] hours1_string = {"2分钟", "3分钟", "4分钟", "6分钟", "8分钟", "12分钟"};
    public static int[] hours1_int = {2, 3, 4, 6, 8, 12};

    private String[] hours2_string = {"0点", "1点", "2点", "3点", "4点", "5点", "6点", "7点", "8点", "9点", "10点",
            "11点", "12点", "13点", "14点", "15点", "16点", "17点", "18点", "19点", "20点", "21点", "22点", "23点"};
    public static int[] hours2_int = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};

    private String[] minutes1_string = {"10分钟", "15分钟", "20分钟", "30分钟", "60分钟"};
    public static int[] minutes1_int = {10, 15, 20, 30, 60};

    private String[] minutes2_string = {"2分钟", "3分钟", "4分钟", "5分钟", "6分钟", "7分钟", "8分钟", "9分钟", "10分钟"};
    public static int[] minutes2_int = {2, 3, 4, 5, 6, 7, 8, 9, 10};

    private String[] minutes4_string = {"20分钟", "30分钟", "60分钟"};
    public static int[] minutes4_int = {20, 30, 60};

    private String[] minutes5_string = {"1分钟", "2分钟", "3分钟", "4分钟", "5分钟", "6分钟", "7分钟", "8分钟",
            "9分钟", "10分钟", "11分钟", "12分钟", "13分钟", "14分钟", "15分钟"};
    public static int[] minutes5_int = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private String[] collector_mode_string = {"短连接", "长连接"};
    public static int[] collector_mode_int = {1, 2};

    private String[] enabled_batch_mode_string = {"不使能", "使能"};
    public static int[] enabled_batch_mode_int = {0, 1};

    private ArrayAdapter<String> adapter_collector_mode;
    private ArrayAdapter<String> adapter_enabled_batch_mode;
    private ArrayAdapter<String> adapter_startTime_morning;
    private ArrayAdapter<String> adapter_endTime_morning;
    private ArrayAdapter<String> adapter_startTime_afternoon;
    private ArrayAdapter<String> adapter_endTime_afternoon;
    private ArrayAdapter<String> adapter_cycle_upload_shortConnection;
    private ArrayAdapter<String> adapter_time_shortConnection;
    private ArrayAdapter<String> adapter_interval_readMeter_shortConnection;
    private ArrayAdapter<String> adapter_interval_readMeter_external_power_supply;
    private ArrayAdapter<String> adapter_interval_longMode_longConnection;
    private ArrayAdapter<String> adapter_interval_shortMode_longConnection;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_gprs_setting, container, false);
        ButterKnife.bind(this, view);
        context = getContext();

        adapter_collector_mode = new ArrayAdapter<>(context, R.layout.spinner_layout_head, collector_mode_string);
        adapter_enabled_batch_mode = new ArrayAdapter<>(context, R.layout.spinner_layout_head, enabled_batch_mode_string);
        adapter_startTime_morning = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_endTime_morning = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_startTime_afternoon = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_endTime_afternoon = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_cycle_upload_shortConnection = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours1_string);
        adapter_time_shortConnection = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_interval_readMeter_shortConnection = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes4_string);
        adapter_interval_readMeter_external_power_supply = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes2_string);
        adapter_interval_longMode_longConnection = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes5_string);
        adapter_interval_shortMode_longConnection = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes1_string);

        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        setAdapter();
        //采集终端参数配置
        //读取参数
        btn_readCollectorParameters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                BluetoothToolsMainActivity.data = "";
                String tx = "7B890021303030303030303030303068481111111100111115048879004474167B";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //设置参数
        btn_setCollectorParameters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                //检查时间点设置是否有误
                if (!(set_startTime_morning.getSelectedItemPosition() < set_endTime_morning.getSelectedItemPosition()
                        && set_endTime_morning.getSelectedItemPosition() <= set_startTime_afternoon.getSelectedItemPosition()
                        && set_startTime_afternoon.getSelectedItemPosition() < set_endTime_afternoon.getSelectedItemPosition())) {
                    CommonUtils.showToast(context, "时间点设置有误，请检查后操作");
                    return;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B89002C3030303030303030303030");

                StringBuilder sb = new StringBuilder();
                sb.append("684811111111001111150F877800");
                //短连接上传周期
                int cycle_upload_shortConnection = set_cycle_upload_shortConnection.getSelectedItemPosition();
                if (hours1_int[cycle_upload_shortConnection] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours1_int[cycle_upload_shortConnection]));

                //短连接时间点
                int time_shortConnection = set_time_shortConnection.getSelectedItemPosition();
                if (hours2_int[time_shortConnection] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours2_int[time_shortConnection]));

                //短连接抄表间隔
                int interval_readMeter_shortConnection = set_interval_readMeter_shortConnection.getSelectedItemPosition();
                if (minutes4_int[interval_readMeter_shortConnection] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(minutes4_int[interval_readMeter_shortConnection]));

                //外供电抄表间隔
                int interval_readMeter_external_power_supply = set_interval_readMeter_external_power_supply.getSelectedItemPosition();
                if (minutes2_int[interval_readMeter_external_power_supply] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(minutes2_int[interval_readMeter_external_power_supply]));

                //电池供电常在线抄表间隔（长连接长模式）
                int interval_longMode_longConnection = set_interval_longMode_longConnection.getSelectedItemPosition();
                if (minutes5_int[interval_longMode_longConnection] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(minutes5_int[interval_longMode_longConnection]));

                //采集终端模式
                int spinner_mode = set_spinner_mode.getSelectedItemPosition();
                if (collector_mode_int[spinner_mode] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(collector_mode_int[spinner_mode]));

                //电池供电短在线状态间隔（长连接短模式）
                int interval_shortMode_longConnection = get_interval_shortMode_longConnection.getSelectedItemPosition();
                if (minutes1_int[interval_shortMode_longConnection] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(minutes1_int[interval_shortMode_longConnection]));

                //分段使能
                int enabled_batch_mode = set_enabled_batch_mode.getSelectedItemPosition();
                if (enabled_batch_mode_int[enabled_batch_mode] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(enabled_batch_mode_int[enabled_batch_mode]));

                //上午起始时间点
                int startTime_morning = set_startTime_morning.getSelectedItemPosition();
                if (hours2_int[startTime_morning] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours2_int[startTime_morning]));

                //上午结束时间点
                int endTime_morning = set_endTime_morning.getSelectedItemPosition();
                if (hours2_int[endTime_morning] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours2_int[endTime_morning]));

                //下午起始时间点
                int startTime_afternoon = set_startTime_afternoon.getSelectedItemPosition();
                if (hours2_int[startTime_afternoon] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours2_int[startTime_afternoon]));

                //下午结束时间点
                int endTime_afternoon = set_endTime_afternoon.getSelectedItemPosition();
                if (hours2_int[endTime_afternoon] < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(hours2_int[endTime_afternoon]));

                String cs = AnalysisUtils.getCSSum(sb.toString(), 0);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //采集器亮灯
        btn_turnOn_light.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002030303030303030303030306848111111110011112B0379470004167B";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //初始化配置
        btn_initParameters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                setAdapter();
            }
        });
    }

    //初始化所有spinner的数据
    public void setAdapter() {
        set_spinner_mode.setAdapter(adapter_collector_mode);
        set_spinner_mode.setSelection(0, false);
        set_enabled_batch_mode.setAdapter(adapter_enabled_batch_mode);
        set_enabled_batch_mode.setSelection(0, false);
        set_startTime_morning.setAdapter(adapter_startTime_morning);
        set_startTime_morning.setSelection(6, false);
        set_endTime_morning.setAdapter(adapter_endTime_morning);
        set_endTime_morning.setSelection(12, false);
        set_startTime_afternoon.setAdapter(adapter_startTime_afternoon);
        set_startTime_afternoon.setSelection(12, false);
        set_endTime_afternoon.setAdapter(adapter_endTime_afternoon);
        set_endTime_afternoon.setSelection(18, false);
        set_cycle_upload_shortConnection.setAdapter(adapter_cycle_upload_shortConnection);
        set_cycle_upload_shortConnection.setSelection(5, false);
        set_time_shortConnection.setAdapter(adapter_time_shortConnection);
        set_time_shortConnection.setSelection(6, false);
        set_interval_readMeter_shortConnection.setAdapter(adapter_interval_readMeter_shortConnection);
        set_interval_readMeter_shortConnection.setSelection(0, false);
        set_interval_readMeter_external_power_supply.setAdapter(adapter_interval_readMeter_external_power_supply);
        set_interval_readMeter_external_power_supply.setSelection(0, false);
        set_interval_longMode_longConnection.setAdapter(adapter_interval_longMode_longConnection);
        set_interval_longMode_longConnection.setSelection(1, false);
        get_interval_shortMode_longConnection.setAdapter(adapter_interval_shortMode_longConnection);
        get_interval_shortMode_longConnection.setSelection(0, false);

        set_startTime_morning.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= set_endTime_morning.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "上午起始时间点必须小于上午结束时间点");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_endTime_morning.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= set_startTime_morning.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "上午结束时间点必须大于上午开始时间点");
                }
                if (position > set_startTime_afternoon.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "上午结束时间点不能大于下午起始时间点");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_startTime_afternoon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < set_endTime_morning.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "下午起始时间点不能小于上午结束时间点");
                }
                if (position >= set_endTime_afternoon.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "下午起始时间点必须小于下午结束时间点");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_endTime_afternoon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= set_startTime_afternoon.getSelectedItemPosition()) {
                    CommonUtils.showToast(context, "下午结束时间点必须大于下午结束时间点");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
