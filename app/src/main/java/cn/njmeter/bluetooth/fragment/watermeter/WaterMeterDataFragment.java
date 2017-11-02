package cn.njmeter.bluetooth.fragment.watermeter;

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

public class WaterMeterDataFragment extends BaseFragment implements View.OnClickListener {

    private static ArrayList<MeterRecord> meterRecordArrayList;
    private Button btn_readMeter;
    private ListView listView;
    public Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_watermeter_data_bluetooth, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        btn_readMeter = (Button) view.findViewById(R.id.btn_read_parameter);
        listView = (ListView) view.findViewById(R.id.listViewreaddata);
        meterRecordArrayList = new ArrayList<>();
    }

    @Override
    public void initData() {
        btn_readMeter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.btn_read_parameter:
                //所有水表均可以直接通过FFFFFFFF读表
                String tx = readMeter("FFFFFFFF", "10", "001111");
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
                MeterRecord.MeterData meterData_productType = meterRecord.new MeterData();
                meterData_productType.setKey("产品类型");
                meterData_productType.setValue(protocol.getProductTypeTX());
                meterDataList.add(meterData_productType);

                MeterRecord.MeterData meterData_totalConsumption = meterRecord.new MeterData();
                meterData_totalConsumption.setKey("正向流量");
                meterData_totalConsumption.setValue(MathUtils.getOriginNumber(protocol.getTotal()) + " " + protocol.getTotalUnit());
                meterDataList.add(meterData_totalConsumption);

                MeterRecord.MeterData meterData_oppositeTotalConsumption = meterRecord.new MeterData();
                meterData_oppositeTotalConsumption.setKey("反向流量");
                meterData_oppositeTotalConsumption.setValue(MathUtils.getOriginNumber(protocol.getOppositeTotal()) + " " + protocol.getOppositeTotalUnit());
                meterDataList.add(meterData_oppositeTotalConsumption);

                MeterRecord.MeterData meterData_flowRate = meterRecord.new MeterData();
                meterData_flowRate.setKey("瞬时流速");
                meterData_flowRate.setValue(protocol.getFlowRate() + " " + protocol.getFlowRateUnit());
                meterDataList.add(meterData_flowRate);

                if (!protocol.getValveStatus().equals("-")) {
                    MeterRecord.MeterData meterData_valveStatus = meterRecord.new MeterData();
                    meterData_valveStatus.setKey("阀门状态");
                    meterData_valveStatus.setValue(protocol.getValveStatus());
                    meterDataList.add(meterData_valveStatus);
                }
                if (!protocol.getTimeInP().equals("-")) {
                    MeterRecord.MeterData meterData_meterTime = meterRecord.new MeterData();
                    meterData_meterTime.setKey("水表时间");
                    meterData_meterTime.setValue(protocol.getTimeInP());
                    meterDataList.add(meterData_meterTime);
                }
                if (!protocol.getStatus().equals("-")) {
                    MeterRecord.MeterData meterData_meterStatus = meterRecord.new MeterData();
                    meterData_meterStatus.setKey("水表状态");
                    meterData_meterStatus.setValue(protocol.getStatus());
                    meterDataList.add(meterData_meterStatus);
                }
            }
            if (protocol.getProductTypeTX().equals("20")) {
                CommonUtils.showToast(context, "设备类型错误，请进入热表页面查看");
                return;
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
