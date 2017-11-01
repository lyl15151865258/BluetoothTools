package cn.njmeter.bluetooth.fragment.collector;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.interfaces.OnMultiClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;

public class  GprsAddDeleteMeterFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.et_meterId)
    EditText et_meterId;
    @BindView(R.id.btn_addMeters)
    Button btn_addMeters;
    @BindView(R.id.btn_deleteMeters)
    Button btn_deleteMeters;
    @BindView(R.id.btn_deleteAllMeters)
    Button btn_deleteAllMeters;
    @BindView(R.id.btn_readAllMeters)
    Button btn_readAllMeters;
    @BindView(R.id.tv_allMeter)
    TextView tv_allMeter;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_gprs_add_delete_meter, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {

        tv_allMeter.setMovementMethod(ScrollingMovementMethod.getInstance());
        //设置TextView控件可以自由滚动，由于这个TextView嵌套在ScrollView中，所以在OnTouch事件中通知父控件ScrollView不要干扰
        tv_allMeter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //通知父控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //通知父控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });


        //添加表号
        btn_addMeters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                String meterId = et_meterId.getText().toString().trim();
                if (meterId.length() != 8) {
                    CommonUtils.showToast(context, "请输入正确的表号");
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900243030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B07795000");
                sb.append(meterId);
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //删除表号
        btn_deleteMeters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                String meterId = et_meterId.getText().toString().trim();
                if (TextUtils.isEmpty(meterId)) {
                    CommonUtils.showToast(context, "请输入表号后再操作");
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900243030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B07795100");
                sb.append(meterId);
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //删除所有表号
        btn_deleteAllMeters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                String meterId = "FFFFFFFF";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900243030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B07795100");
                sb.append(meterId);
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        //读取所有表号
        btn_readAllMeters.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("7B8900203030303030303030303030");
                StringBuilder sb = new StringBuilder();
                sb.append("6848111111110011113B03795200");
                int checksum = getCheckSum(sb.toString());
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");
                stringBuilder.append(sb.toString());
                BluetoothToolsMainActivity.data = "";
                String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

    }

    public int getCheckSum(String param) {
        StringBuilder sb = new StringBuilder();
        int res = 0;
        for (int i = 0; i < param.length() / 2; i++) {
            res += AnalysisUtils.HexS2ToInt(param.substring(i * 2, i * 2 + 2));
        }
        return res;
    }
}