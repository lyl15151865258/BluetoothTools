package cn.njmeter.bluetooth.fragment.hydrant;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.bean.TcpUdpParam;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.utils.AnalysisUtils;

public class HydrantGPRSFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.scrollView_root)
    ScrollView scrollView_root;

    @BindView(R.id.tv_imei)
    TextView tv_imei;
    @BindView(R.id.btn_set_imei)
    Button btnSetIMEI;
    @BindView(R.id.et_imei)
    EditText editTextIMEI;
    @BindView(R.id.btn_read_imei)
    Button btn_read_imei;
    @BindView(R.id.radioTcpServer)
    RadioButton radioTcpServer;
    @BindView(R.id.editTextIP1)
    EditText editTextIP1;
    @BindView(R.id.editTextIP2)
    EditText editTextIP2;
    @BindView(R.id.editTextIP3)
    EditText editTextIP3;
    @BindView(R.id.editTextIP4)
    EditText editTextIP4;
    @BindView(R.id.editTextPort)
    EditText editTextPort;
    @BindView(R.id.btnSetComm)
    Button btnSetComm;
    @BindView(R.id.btnReadComm)
    Button btnReadComm;
    @BindView(R.id.btnGprsDefault)
    Button btnGprsDefault;

    static boolean autoRefreshDateTime;

    boolean isSetParamLegal;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_hydrant_gprs_bluetooth, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        autoRefreshDateTime = false;
        isSetParamLegal = true;
        initView(view);
        initData();
        return view;
    }

    //获得软键盘高度并滚动ScrollView
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();
            //计算软键盘占有的高度  = 屏幕高度 - 视图可见高度
            int heightDifference = screenHeight - rect.bottom;
            //计算父（Activity）布局底部菜单栏的高度
            int height_menu = getActivity().findViewById(R.id.ll_menu).getHeight();
            //针对在父控件中的View的参数获取
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) scrollView_root.getLayoutParams();
            //设置ScrollView的marginBottom的值为软键盘占有的高度减去Activity的底部菜单栏高度
            //不减去Activity的底部菜单栏高度会导致软键盘弹出的时候，会在软键盘与输入框之间产生Activity底部菜单栏高度的空白
            //当软键盘弹出的时候，heightDifference - height_menu＞0，设置正常
            //当软键盘隐藏的时候，heightDifference - height_menu＜0，此时需要设置为0，否则ScrollView最下面一部分会被Activity的菜单栏遮挡
            layoutParams.setMargins(0, 0, 0, heightDifference - height_menu < 0 ? 0 : heightDifference - height_menu);
            scrollView_root.requestLayout();
        }
    };

    @Override
    public void initView(View view) {
        //监听软键盘，滚动ScrollView以适应输入框
        getActivity().getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        btnSetIMEI.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String newimei = editTextIMEI.getText().toString().replace(" ", "");
                if (newimei.length() == 11) {
                    int checksum = 557;
                    StringBuilder sb = new StringBuilder();
                    sb.append("7B89002B3030303030303030303030684811111111001111070EC14100");
                    for (int i = 0; i < newimei.length(); i++) {
                        sb.append('3');
                        sb.append(newimei.charAt(i));
                        checksum += 3 * 16 + Integer.parseInt("" + (newimei.charAt(i)));
                    }
                    String cs = Integer.toHexString(checksum);
                    sb.append(cs.substring(cs.length() - 2));
                    sb.append("167B");
                    //Toast.makeText(getApplicationContext(), tx, Toast.LENGTH_SHORT).show();
                    System.out.println(sb.toString());
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(sb.toString());
                } else {
                    Toast.makeText(context, "请输入11位IMEI号！", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_read_imei.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                tv_imei.setText("");
                String tx = "7B89002030303030303030303030306848111111110011110703C1420023167B";
                BluetoothToolsMainActivity.data = "";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        btnGprsDefault.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                editTextIP1.setText("58");
                editTextIP2.setText("240");
                editTextIP3.setText("47");
                editTextIP4.setText("50");
            }
        });
        btnSetComm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //7B89003D30303030303030303030306848111111110011110703C11E0122544350222C2235382E3234302E34372E3530222C2235303034220D2361167B
                StringBuilder sb = new StringBuilder();
                sb.append("7B89003D30303030303030303030306848111111110011110703C11E01");
                TcpUdpParam param = new TcpUdpParam();
                if (radioTcpServer.isChecked()) {
                    param.setMode("TCP");
                } else {
                    param.setMode("UDP");
                }
                param.setIp1(editTextIP1.getText().toString().replace(" ", ""));
                param.setIp2(editTextIP2.getText().toString().replace(" ", ""));
                param.setIp3(editTextIP3.getText().toString().replace(" ", ""));
                param.setIp4(editTextIP4.getText().toString().replace(" ", ""));
                param.setPort(editTextPort.getText().toString().replace(" ", ""));
                // BluetoothPrinterMainActivity.writeData(tx);
                if (param.isIsvalid()) {
                    sb.append(getHexStr(param));
                    sb.append("0D23");

                    String checkStr = sb.substring(sb.indexOf("68"));
                    int len = checkStr.length() - 20;
                    //StringBuffer sbf = new StringBuffer(checkStr);
                    // sb.replace(50, 52, len<17?("0"+Integer.toHexString(len)):(Integer.toHexString(len)));
                    checkStr = sb.substring(sb.indexOf("68"));
                    int checksum = getCheckSum(checkStr);
                    String cs = Integer.toHexString(checksum);
                    sb.append(cs.substring(cs.length() - 2));
                    sb.append("167B");
                    sb.replace(6, 8, sb.length() / 2 < 17 ? ("0" + Integer.toHexString(sb.length() / 2)) : (Integer.toHexString(sb.length() / 2)));
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(sb.toString());
                } else {
                    Toast.makeText(context, "IP或端口输入错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnReadComm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //tv_meterid.setText("");
                //BluetoothPrinterMainActivity.data = "";
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002130303030303030303030306848111111110011110703C12F000111167B";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

    }

    public String getHexStr(TcpUdpParam param) {
        String strtcp = param.toString();
        byte[] tcpbytes = strtcp.getBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tcpbytes.length; i++) {
            sb.append(Integer.toHexString(tcpbytes[i]));
        }
        System.out.println(sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    public int getCheckSum(String param) {
        StringBuilder sb = new StringBuilder();
        int res = 0;
        for (int i = 0; i < param.length() / 2; i++) {
            res += AnalysisUtils.HexS2ToInt(param.substring(i * 2, i * 2 + 2));
        }
        return res;
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
    public void onDestroyView() {
        super.onDestroyView();
        //移除软键盘弹出收回监听（解决隐藏状态栏以及导航栏导致和软件盘冲突的解决所添加的监听）
        if (onGlobalLayoutListener != null) {
            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }
}
