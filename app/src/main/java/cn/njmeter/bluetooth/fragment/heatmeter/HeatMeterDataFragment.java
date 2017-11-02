package cn.njmeter.bluetooth.fragment.heatmeter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.adapter.MeterDataAdapter;
import cn.njmeter.bluetooth.bean.MeterRecord;
import cn.njmeter.bluetooth.bean.Protocol;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.utils.BluetoothAnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.MathUtils;

public class HeatMeterDataFragment extends BaseFragment implements View.OnClickListener {

    private static ArrayList<MeterRecord> meterRecordArrayList;
    private Button btn_readmeter;
    private ListView listView;
    public Context context;
    public String meterId = "FFFFFFFF";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_heatmeter_data_bluetooth, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        btn_readmeter = (Button) view.findViewById(R.id.btn_read_parameter);
        listView = (ListView) view.findViewById(R.id.listViewreaddata);
        meterRecordArrayList = new ArrayList<>();
    }

    @Override
    public void initData() {
        btn_readmeter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.btn_read_parameter:
                //MTH-6系列超声波热量表不能直接通过FFFFFFFF读表，需先通过读取内部参数获得表号，然后带表号读表
                //除了MTH-6系列超声波热量表，其余表具可以直接通过FFFFFFFF读表，而不需要先读取表号
                String tx;
                if (meterId.equals("FFFFFFFF")) {
                    //读取内部参数获取表号
                    tx = "6820AAAAAAAAAAAAAA1A039A2F001416";
                } else {
                    //带着获取到的表号读表
                    tx = readMeter(meterId, "20", "001111");
                }
                BluetoothToolsMainActivity.data = "";
                BluetoothToolsMainActivity.writeData(tx);
                break;
            default:
                break;
        }
    }

    public String readMeter(String meterid, String producttypetx, String factorycode) {
        String r;
        BluetoothAnalysisUtils bluetoothAnalysisUtils = new BluetoothAnalysisUtils();
        String cs = bluetoothAnalysisUtils.getChecksum("68" + producttypetx + meterid + factorycode + "0103901F00", 0);
        r = "68" + producttypetx + meterid + factorycode + "0103901F00" + cs + "16";
        return r;
    }

    @SuppressLint("SimpleDateFormat")
    public void addListView(Protocol protocol) {
        List<MeterRecord.MeterData> meterDataList = new ArrayList<>();
        MeterRecord meterRecord = new MeterRecord();
        if (!protocol.getMeterID().equals("")) {
            if (protocol.getProductTypeTX().equals("10")) {
                CommonUtils.showToast(context, "设备类型错误，请进入水表页面查看");
                return;
            }
            if (protocol.getProductTypeTX().equals("20")) {
                MeterRecord.MeterData meterData_productType = meterRecord.new MeterData();
                meterData_productType.setKey("产品类型");
                meterData_productType.setValue(protocol.getProductTypeTX());
                meterDataList.add(meterData_productType);

                if (!protocol.getMBUSAddress().equals("-")) {
                    MeterRecord.MeterData meterData_mbusAddress = meterRecord.new MeterData();
                    meterData_mbusAddress.setKey("M-BUS主机地址");
                    meterData_mbusAddress.setValue(protocol.getMBUSAddress());
                    meterDataList.add(meterData_mbusAddress);
                }
                MeterRecord.MeterData meterData_coolConsumption = meterRecord.new MeterData();
                meterData_coolConsumption.setKey("累计冷量");
                meterData_coolConsumption.setValue(MathUtils.getOriginNumber(protocol.getSumCool()) + " " + protocol.getSumCoolUnit());
                meterDataList.add(meterData_coolConsumption);

                MeterRecord.MeterData meterData_heatConsumption = meterRecord.new MeterData();
                meterData_heatConsumption.setKey("累计热量");
                meterData_heatConsumption.setValue(MathUtils.getOriginNumber(protocol.getSumHeat()) + " " + protocol.getSumHeatUnit());
                meterDataList.add(meterData_heatConsumption);

                MeterRecord.MeterData meterData_totalConsumption = meterRecord.new MeterData();
                meterData_totalConsumption.setKey("累计流量");
                meterData_totalConsumption.setValue(MathUtils.getOriginNumber(protocol.getTotal()) + " " + protocol.getTotalUnit());
                meterDataList.add(meterData_totalConsumption);

                MeterRecord.MeterData meterData_power = meterRecord.new MeterData();
                meterData_power.setKey("制热功率");
                meterData_power.setValue(protocol.getPower() + " " + protocol.getPowerUnit());
                meterDataList.add(meterData_power);

                MeterRecord.MeterData meterData_flowRate = meterRecord.new MeterData();
                meterData_flowRate.setKey("瞬时流速");
                meterData_flowRate.setValue(protocol.getFlowRate() + " " + protocol.getFlowRateUnit());
                meterDataList.add(meterData_flowRate);

                if (protocol.getSumOpenValveM() != 0) {
                    MeterRecord.MeterData meterData_totalOpenValveTime = meterRecord.new MeterData();
                    meterData_totalOpenValveTime.setKey("累计开阀");
                    meterData_totalOpenValveTime.setValue(protocol.getSumOpenValveM() + " " + "Min");
                    meterDataList.add(meterData_totalOpenValveTime);
                }
                if (!protocol.getCloseTime().equals("-")) {
                    MeterRecord.MeterData meterData_closeTime = meterRecord.new MeterData();
                    meterData_closeTime.setKey("截止日期");
                    meterData_closeTime.setValue(protocol.getCloseTime());
                    meterDataList.add(meterData_closeTime);
                }
                if (!protocol.getLosePowerTime().equals("-")) {
                    MeterRecord.MeterData meterData_losePowerTime = meterRecord.new MeterData();
                    meterData_losePowerTime.setKey("断电时间");
                    meterData_losePowerTime.setValue(protocol.getLosePowerTime() + " " + "Min");
                    meterDataList.add(meterData_losePowerTime);
                }
                if (!protocol.getLoseConTime().equals("-")) {
                    MeterRecord.MeterData meterData_disconnectedTime = meterRecord.new MeterData();
                    meterData_disconnectedTime.setKey("无通讯时间");
                    meterData_disconnectedTime.setValue(protocol.getLoseConTime() + " " + "h");
                    meterDataList.add(meterData_disconnectedTime);
                }
                if (protocol.getInsideT() != 0) {
                    MeterRecord.MeterData meterData_insideTemperature = meterRecord.new MeterData();
                    meterData_insideTemperature.setKey("室内温度");
                    meterData_insideTemperature.setValue(protocol.getInsideT() + " " + "℃");
                    meterDataList.add(meterData_insideTemperature);
                }
                if (!protocol.getInsideTSet().equals("-")) {
                    MeterRecord.MeterData meterData_insideTemperatureSet = meterRecord.new MeterData();
                    meterData_insideTemperatureSet.setKey("设定室温");
                    meterData_insideTemperatureSet.setValue(protocol.getInsideTSet() + " " + "℃");
                    meterDataList.add(meterData_insideTemperatureSet);
                }
                if (!protocol.getValveStatus().equals("-")) {
                    MeterRecord.MeterData meterData_valveStatus = meterRecord.new MeterData();
                    meterData_valveStatus.setKey("阀门状态");
                    meterData_valveStatus.setValue(protocol.getValveStatus());
                    meterDataList.add(meterData_valveStatus);
                }
                if (protocol.getT1InP() != 0) {
                    MeterRecord.MeterData meterData_intoTemperature = meterRecord.new MeterData();
                    meterData_intoTemperature.setKey("进水温度");
                    meterData_intoTemperature.setValue(protocol.getT1InP() + " " + "℃");
                    meterDataList.add(meterData_intoTemperature);
                }
                if (protocol.getT2InP() != 0) {
                    MeterRecord.MeterData meterData_outTemperature = meterRecord.new MeterData();
                    meterData_outTemperature.setKey("出水温度");
                    meterData_outTemperature.setValue(protocol.getT2InP() + " " + "℃");
                    meterDataList.add(meterData_outTemperature);
                }
                if (protocol.getT1InP() != 0) {
                    MeterRecord.MeterData meterData_workTime = meterRecord.new MeterData();
                    meterData_workTime.setKey("工作时间");
                    meterData_workTime.setValue(protocol.getWorkTimeInP() + " " + "h");
                    meterDataList.add(meterData_workTime);
                }
                if (!protocol.getTimeInP().equals("-")) {
                    MeterRecord.MeterData meterData_meterTime = meterRecord.new MeterData();
                    meterData_meterTime.setKey("热表时间");
                    meterData_meterTime.setValue(protocol.getTimeInP());
                    meterDataList.add(meterData_meterTime);
                }
                if (!protocol.getVol().equals("-")) {
                    MeterRecord.MeterData meterData_batteryVoltage = meterRecord.new MeterData();
                    meterData_batteryVoltage.setKey("电池电压");
                    meterData_batteryVoltage.setValue(protocol.getVol() + " " + "V");
                    meterDataList.add(meterData_batteryVoltage);
                }
                if (!protocol.getStatus().equals("-")) {
                    MeterRecord.MeterData meterData_meterStatus = meterRecord.new MeterData();
                    meterData_meterStatus.setKey("热表状态");
                    meterData_meterStatus.setValue(protocol.getStatus());
                    meterDataList.add(meterData_meterStatus);
                }
            }
            if (protocol.getProductTypeTX().equals("49")) {
                CommonUtils.showToast(context, "设备类型错误，请进入阀门页面查看");
                return;
            }
            //在第一位插入数据，使新读的数据显示在最上面
            meterRecordArrayList.add(0, new MeterRecord(protocol.getMeterID(), (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)).format(new Date()), meterDataList));
        }
        MeterDataAdapter adapter = new MeterDataAdapter(context, meterRecordArrayList);
        listView.setAdapter(adapter);
    }
}
