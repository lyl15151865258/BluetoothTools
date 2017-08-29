package cn.njmeter.bluetooth.fragment.heatmeter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.fragment.BaseFragment;

public class HeatMeterLcdFragment extends BaseFragment {

    @BindView(R.id.mGridView)
    GridView mGridView;

    private int[] imageRes = { R.mipmap.display, R.mipmap.clock,
            R.mipmap.setting_cv, R.mipmap.shidongliuliang,R.mipmap.flowa2, R.mipmap.staticvalue,
            R.mipmap.sleepvoltage, R.mipmap.flowout,R.mipmap.xielvxiuzheng, R.mipmap.batano,
            R.mipmap.setting_t1, R.mipmap.setting_t2,R.mipmap.setting_a1, R.mipmap.setting_a2,
            R.mipmap.setting_a3 };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_heatmeter_lcd_bluetooth, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        initData();

        String[] itemName = { this.getString(R.string.lcd_meterid), this.getString(R.string.lcd_time),this.getString(R.string.lcd_flowcoef),this.getString(R.string.lcd_flowrate) ,
                this.getString(R.string.lcd_total), this.getString(R.string.lcd_static),this.getString(R.string.lcd_sleeptimeandvol),this.getString(R.string.lcd_startandsize) ,
                this.getString(R.string.lcd_slopeandamend), this.getString(R.string.lcd_ver),this.getString(R.string.lcd_t1),this.getString(R.string.lcd_t2) ,
                this.getString(R.string.lcd_A1), this.getString(R.string.lcd_A2),this.getString(R.string.lcd_A3) };

        List<HashMap<String, Object>> data = new ArrayList<>();
        int length = itemName.length;
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("ItemImageView", imageRes[i]);
            map.put("ItemTextView", itemName[i]);
            data.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(),
                data, R.layout.lcd_item, new String[] { "ItemImageView",
                "ItemTextView" }, new int[] { R.id.ItemImageView,
                R.id.tv_item});
        mGridView.setAdapter(simpleAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String tx;
                switch (arg2) {
                    case 0:
                        tx="6820AAAAAAAAAAAAAA1A039A4F003416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 1:
                        tx="6820AAAAAAAAAAAAAA1A039A1F000416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 2:
                        tx="6820AAAAAAAAAAAAAA1A039A3F002416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 3:
                        tx="6820AAAAAAAAAAAAAA1A039AAF009416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 4:
                        tx="6820AAAAAAAAAAAAAA1A039A7F006416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 5:
                        tx="6820AAAAAAAAAAAAAA1A039A5F004416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 6:
                        tx="6820AAAAAAAAAAAAAA1A039AE300C816";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 7:
                        tx="6820AAAAAAAAAAAAAA1A039ACF00B416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 8:
                        tx="6820AAAAAAAAAAAAAA1A039AE200C716";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 9:
                        tx="6820AAAAAAAAAAAAAA1A039A6F005416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 10:
                        tx="6820AAAAAAAAAAAAAA1A039ADF00C416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 11:
                        tx="6820AAAAAAAAAAAAAA1A039AEF00D416";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 12:
                        tx="6820AAAAAAAAAAAAAA3E03901F001E16";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 13:
                        tx="6820AAAAAAAAAAAAAA3E03902F002E16";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    case 14:
                        tx="6820AAAAAAAAAAAAAA3E03903F003E16";
                        BluetoothToolsMainActivity.writeData(tx);
                        break;
                    default: break;
                }
            }
        });

        return view;
    }
}
