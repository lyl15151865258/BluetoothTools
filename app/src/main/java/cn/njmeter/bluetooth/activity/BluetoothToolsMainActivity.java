package cn.njmeter.bluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.njmeter.bluetooth.BaseActivity;
import cn.njmeter.bluetooth.MyActivityManager;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.bean.MBUS;
import cn.njmeter.bluetooth.bean.ParameterProtocol;
import cn.njmeter.bluetooth.bean.Protocol;
import cn.njmeter.bluetooth.bean.SSumHeat;
import cn.njmeter.bluetooth.bean.TcpUdpParam;
import cn.njmeter.bluetooth.constant.Constant;
import cn.njmeter.bluetooth.constant.ProductType;
import cn.njmeter.bluetooth.fragment.collector.GprsAddDeleteMeterFragment;
import cn.njmeter.bluetooth.fragment.collector.GprsNormalFragment;
import cn.njmeter.bluetooth.fragment.collector.GprsSetPressureFragment;
import cn.njmeter.bluetooth.fragment.collector.GprsSettingFragment;
import cn.njmeter.bluetooth.fragment.heatmeter.HeatMeterAdjustFragment;
import cn.njmeter.bluetooth.fragment.heatmeter.HeatMeterDataFragment;
import cn.njmeter.bluetooth.fragment.heatmeter.HeatMeterLcdFragment;
import cn.njmeter.bluetooth.fragment.heatmeter.HeatMeterParameterFragment;
import cn.njmeter.bluetooth.fragment.hydrant.HydrantAdjustFragment;
import cn.njmeter.bluetooth.fragment.hydrant.HydrantDataFragment;
import cn.njmeter.bluetooth.fragment.hydrant.HydrantGPRSFragment;
import cn.njmeter.bluetooth.fragment.hydrant.HydrantParameterFragment;
import cn.njmeter.bluetooth.fragment.valve.ValveSettingFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterAdjustFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterDataFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterLcdFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterParameterFragment;
import cn.njmeter.bluetooth.fragment.watermeter.WaterMeterSetPressureFragment;
import cn.njmeter.bluetooth.interfaces.OnMultiItemClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.BluetoothAnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.GPSUtils;
import cn.njmeter.bluetooth.utils.LogUtils;
import cn.njmeter.bluetooth.utils.ScreenTools;
import cn.njmeter.bluetooth.utils.SharedPreferencesUtils;
import cn.njmeter.bluetooth.utils.TimeUtils;
import cn.njmeter.bluetooth.widget.MyGridView;
import cn.njmeter.bluetooth.widget.dialog.ConnectBluetoothDialog;

public class BluetoothToolsMainActivity extends BaseActivity {

    private static Context context;
    private ImageView iv_search_bluetooth_device;
    private TextView tv_bluetoothStatus;
    private final static String SSP_UUID = "00001101-0000-1000-8000-00805F9B34FB";      //SPP服务UUID号
    private static InputStream inputStream;                                             //输入流，用来接收蓝牙数据
    public static String data = "";                                                     //显示用数据缓存
    private static BluetoothDevice bluetoothDevice = null;                              //蓝牙设备
    public static BluetoothSocket bluetoothSocket = null;                               //蓝牙通信socket
    private static boolean exit = false;                                                //切换不同蓝牙工具时避免创建多个接收数据线程
    private BluetoothAdapter bluetoothAdapter;                                          //本地蓝牙适配器
    private boolean bluetoothIsOpened = false;                                          //获取进入页面时蓝牙打开状态，退出页面时根据这个判断是否需要关闭蓝牙
    private static String tx;                                                           //发送的指令
    //蓝牙搜索相关
    private static ArrayAdapter<String> adapter1, adapter2;                             //已配对和未配对list的适配器
    private static ArrayList<String> deviceList_bonded = new ArrayList<>();             //已配对列表
    private static ArrayList<String> deviceList_found = new ArrayList<>();              //未配对列表
    private DeviceReceiver deviceReceiver = new DeviceReceiver();                       //蓝牙搜索广播
    private boolean isConnecting = false;                                               //标记是否正在连接过程中
    private boolean isCancelDiscovery = false;                                          //标记是否是取消搜索（不显示“没有搜索到*****”）
    //用于判断当前的Fragment是该Activity的第几个
    public static int currentFragment = 0;

    private MyHandler myHandler = new MyHandler(this);
    private ConnectBluetoothDialog connectBluetoothDialog;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_bluetooth_tools_main);
        LinearLayout ll_root = (LinearLayout) findViewById(R.id.ll_root);
        ll_root.setPadding(0, ScreenTools.getStatusBarHeight(this), 0, 0);
        iv_search_bluetooth_device = (ImageView) findViewById(R.id.iv_search_bluetooth_device);
        iv_search_bluetooth_device.setOnClickListener(onClickListener);
        tv_bluetoothStatus = (TextView) findViewById(R.id.tv_bluetoothStatus);
        MyGridView gridView_chooseType1 = (MyGridView) findViewById(R.id.gridView_chooseType1);
        MyGridView gridView_chooseType2 = (MyGridView) findViewById(R.id.gridView_chooseType2);
        int[] imageRes1 = {R.mipmap.icon_watermeter, R.mipmap.icon_heatmeter, R.mipmap.icon_gasmeter,
                R.mipmap.icon_hydrant, R.mipmap.icon_valve, R.mipmap.icon_gprs};
        String[] itemName1 = {"超声波水表", "超声热量表", "超声燃气表", "智能消火栓", "智能控制阀", "无线采集终端"};
        List<HashMap<String, Object>> data1 = new ArrayList<>();
        int length1 = itemName1.length;
        for (int i = 0; i < length1; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("ItemImageView", imageRes1[i]);
            map.put("ItemTextView", itemName1[i]);
            data1.add(map);
        }
        SimpleAdapter simpleAdapter1 = new SimpleAdapter(this, data1, R.layout.item_type_device,
                new String[]{"ItemImageView", "ItemTextView"}, new int[]{R.id.iv_icon, R.id.tv_item});
        gridView_chooseType1.setAdapter(simpleAdapter1);
        gridView_chooseType1.setOnItemClickListener(onMultiItemClickListener);

        int[] imageRes2 = {R.mipmap.icon_lora, R.drawable.ic_launcher, R.drawable.ic_launcher};
        String[] itemName2 = {"无线LoRa设定", "预留", "预留"};
        List<HashMap<String, Object>> data2 = new ArrayList<>();
        int length2 = itemName2.length;
        for (int i = 0; i < length2; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("ItemImageView", imageRes2[i]);
            map.put("ItemTextView", itemName2[i]);
            data2.add(map);
        }
        SimpleAdapter simpleAdapter2 = new SimpleAdapter(this, data2, R.layout.item_type_device,
                new String[]{"ItemImageView", "ItemTextView"}, new int[]{R.id.iv_icon, R.id.tv_item});
        gridView_chooseType2.setAdapter(simpleAdapter2);
        gridView_chooseType2.setOnItemClickListener(onMultiItemClickListener);
        initBluetooth();
        initBroadcastReceiver();
    }

    //初始化手机蓝牙
    private void initBluetooth() {
        try {
            //获取本地蓝牙适配器
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //如果打开本地蓝牙设备不成功，提示信息，结束程序
            if (bluetoothAdapter == null) {
                CommonUtils.showToast(context, "无法打开手机蓝牙");
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bluetoothIsOpened = bluetoothAdapter.isEnabled();
        // 设置设备可以被搜索
        new Thread() {
            public void run() {
                if (!bluetoothIsOpened) {
                    if (!bluetoothAdapter.enable()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.showToast(context, "您禁止了打开蓝牙");
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private void initBroadcastReceiver() {
        //注册蓝牙广播接收者
        IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(deviceReceiver, filterStart);
        context.registerReceiver(deviceReceiver, filterEnd);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_search_bluetooth_device:
                    try {
                        if (isConnecting) {
                            CommonUtils.showToast(context, "正在连接蓝牙设备，请稍后");
                        } else {
                            ConnectBluetoothDevice();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //连接蓝牙设备
    private void ConnectBluetoothDevice() {
        if (!bluetoothAdapter.isEnabled()) {
            new Thread() {
                public void run() {
                    if (!bluetoothAdapter.enable()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.showToast(context, "您禁止了打开蓝牙");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.showToast(context, "手机蓝牙已打开，请重新连接");
                            }
                        });
                    }
                }
            }.start();
        } else {
            if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                //如未连接设备则进行设备搜索
                showBluetoothList();
            } else {
                //提示是否断开蓝牙
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.app_notice).setIcon(android.R.drawable.ic_dialog_info).setNegativeButton(R.string.amenderr_setCancle, null);
                builder.setPositiveButton(R.string.amenderr_setOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disConnect();
                        iv_search_bluetooth_device.setImageResource(R.drawable.bluetooth_disconnected);
                        tv_bluetoothStatus.setText("未连接蓝牙工具");
                    }
                });
                builder.show();
            }
        }
    }

    //显示蓝牙设备列表
    private void showBluetoothList() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            isCancelDiscovery = true;
        } else {
            isCancelDiscovery = false;
        }
        bluetoothAdapter.startDiscovery();
        if (connectBluetoothDialog == null) {
            connectBluetoothDialog = new ConnectBluetoothDialog(context);
        }
        adapter1 = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, deviceList_bonded);
        ListView lv_bonded = connectBluetoothDialog.findViewById(R.id.lv_bonded);
        lv_bonded.setOnItemClickListener(onMultiItemClickListener);
        ListView lv_found = connectBluetoothDialog.findViewById(R.id.lv_found);
        lv_found.setOnItemClickListener(onMultiItemClickListener);
        adapter2 = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, deviceList_found);
        lv_bonded.setAdapter(adapter1);
        lv_found.setAdapter(adapter2);
        deviceList_bonded.clear();
        deviceList_found.clear();
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        connectBluetoothDialog.setCancelable(true);
        connectBluetoothDialog.show();
    }

    private OnMultiItemClickListener onMultiItemClickListener = new OnMultiItemClickListener() {
        @Override
        public void onMultiClick(AdapterView<?> parent, View view, int position, long id) {
            ArrayList<String> list;
            switch (parent.getId()) {
                case R.id.lv_bonded:
                    list = deviceList_bonded;
                    try {
                        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                            bluetoothAdapter.cancelDiscovery();
                        }
                        String msg = list.get(position);
                        String mac = msg.substring(msg.length() - 17);
                        connectBluetoothDialog.cancel();
                        connectDevice(mac);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.lv_found:
                    list = deviceList_found;
                    try {
                        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                            bluetoothAdapter.cancelDiscovery();
                        }
                        String msg = list.get(position);
                        String mac = msg.substring(msg.length() - 17);
                        connectBluetoothDialog.cancel();
                        connectDevice(mac);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.gridView_chooseType1:
                    switch (position) {
                        case 0:
                            //超声水表设置
                            openActivity(WaterMeterSettingActivity.class);
                            break;
                        case 1:
                            //超声热量表设置
                            openActivity(HeatMeterSettingActivity.class);
                            break;
                        case 3:
                            //智能消火栓设置
                            openActivity(HydrantSettingActivity.class);
                            break;
                        case 4:
                            //智能控制阀设置
                            openActivity(ValveSettingActivity.class);
                            break;
                        case 5:
                            //GPRS采集终端设置
                            openActivity(GPRSCollectorSettingActivity.class);
                            break;
                        default:
                            break;
                    }
                    break;
                case R.id.gridView_chooseType2:
                    switch (position) {
                        case 0:
                            //无线Lora设定
                            openActivity(LoRaSettingActivity.class);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //点击连接蓝牙设备
    private void connectDevice(final String mac) {
        //蓝牙连接会阻塞线程，开启子线程连接
        new Thread(new Runnable() {
            @Override
            public void run() {
                isConnecting = true;
                // 得到蓝牙设备句柄
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
                // 用服务号得到socket
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(SSP_UUID));
                } catch (IOException e) {
                    myHandler.sendMessage(myHandler.obtainMessage(-1));
                    isConnecting = false;
                }
                try {
                    bluetoothSocket.connect();
                    myHandler.sendMessage(myHandler.obtainMessage(0));
                } catch (IOException e) {
                    try {
                        myHandler.sendMessage(myHandler.obtainMessage(-1));
                        bluetoothSocket.close();
                        bluetoothSocket = null;
                    } catch (IOException ee) {
                        myHandler.sendMessage(myHandler.obtainMessage(-1));
                        isConnecting = false;
                    }
                    isConnecting = false;
                    return;
                }
                //打开接收线程
                try {
                    inputStream = bluetoothSocket.getInputStream();   //得到蓝牙数据输入流
                } catch (IOException e) {
                    myHandler.sendMessage(myHandler.obtainMessage(-2));
                    isConnecting = false;
                    return;
                }
                exit = false;
                isConnecting = false;
                createReadThread();
            }
        }).start();
    }

    //蓝牙搜索广播
    private class DeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d("bluetooth", "当前蓝牙状态：" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                LogUtils.d("bluetooth", "正在搜索蓝牙设备");
                //搜索到新设备
                BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //搜索没有配过对的蓝牙设备
                if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
                    LogUtils.d("bluetooth", "搜索到已配对的蓝牙设备");
                    if (!deviceList_found.contains(btd.getName() + '\n' + btd.getAddress())) {
                        deviceList_found.add(btd.getName() + '\n' + btd.getAddress());
                        try {
                            adapter2.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (btd.getBondState() != BluetoothDevice.BOND_NONE) {
                    LogUtils.d("bluetooth", "搜索到未配对的蓝牙设备");
                    if (!deviceList_bonded.contains(btd.getName() + '\n' + btd.getAddress())) {
                        deviceList_bonded.add(btd.getName() + '\n' + btd.getAddress());
                        try {
                            adapter1.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.d("bluetooth", "结束搜索蓝牙设备");
                //搜索结束
                if (deviceList_bonded.size() == 0 && !isCancelDiscovery) {
                    deviceList_bonded.add("没有搜索到已配对的蓝牙设备");
                    try {
                        adapter1.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (deviceList_found.size() == 0 && !isCancelDiscovery) {
                    deviceList_found.add("没有搜索到未配对的蓝牙设备");
                    try {
                        adapter2.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void writeData(final String tx) {
        if (!(bluetoothSocket != null && bluetoothSocket.isConnected())) {
            CommonUtils.showToast(context, "请先返回连接蓝牙工具");
            return;
        }
        LogUtils.d("bluetooth", "发送的指令为：" + tx);
        BluetoothToolsMainActivity.data = "";
        BluetoothToolsMainActivity.tx = tx;
        writeToBluetoothDevice();
    }

    //断开蓝牙连接
    public static void disConnect() {
        //关闭连接socket
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //关闭输入流
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bluetoothDevice != null) {
            bluetoothDevice = null;
        }
        //关闭接收数据的子线程
        exit = true;
    }

    //创建接收数据线程
    private void createReadThread() {
        new Thread() {
            public void run() {
                int num;
                byte[] buffer = new byte[1024];
                //接收线程
                while (!exit) {
                    try {
                        while (!exit) {
                            num = inputStream.read(buffer);         //读入数据
                            String s0 = "";
                            for (int i = 0; i < num; i++) {
                                int b = (int) buffer[i];
                                if (b < 0) b = 256 + b;
                                s0 = s0 + Integer.toHexString(b / 16) + Integer.toHexString(b % 16);
                            }
                            data += s0;   //写入接收缓存
                            if (inputStream.available() == 0)
                                break;  //短时间没有数据才跳出进行显示
                        }
                    } catch (IOException e) {
                        myHandler.sendMessage(myHandler.obtainMessage(1));
                        break;
                    }
                    //发送显示消息，进行显示刷新
                    myHandler.sendMessage(myHandler.obtainMessage(2));
                }
            }
        }.start();
    }

    //自定义的Handler，Handler类应该定义成静态类，否则可能导致内存泄露
    private static class MyHandler extends Handler {
        WeakReference<BluetoothToolsMainActivity> mActivity;

        MyHandler(BluetoothToolsMainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BluetoothToolsMainActivity theActivity = mActivity.get();
            AppCompatActivity currentActivity = (AppCompatActivity) MyActivityManager.getInstance().getCurrentActivity();
            switch (msg.what) {
                case -2:
                    CommonUtils.showToast(context, "接收数据失败");
                    break;
                case -1:
                    CommonUtils.showToast(context, "连接失败");
                    break;
                case 0:
                    theActivity.iv_search_bluetooth_device.setImageResource(R.drawable.bluetooth_connected);
                    theActivity.tv_bluetoothStatus.setText("已连接:" + bluetoothDevice.getName() + "(" + bluetoothDevice.getAddress() + ")");
                    CommonUtils.showToast(context, "连接" + bluetoothDevice.getName() + "成功");
                    if (currentActivity instanceof WaterMeterSettingActivity) {
                        WaterMeterSettingActivity waterMeterSettingActivity = (WaterMeterSettingActivity) currentActivity;
                        ((ImageView) waterMeterSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    } else if (currentActivity instanceof HeatMeterSettingActivity) {
                        HeatMeterSettingActivity heatMeterSettingActivity = (HeatMeterSettingActivity) currentActivity;
                        ((ImageView) heatMeterSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    } else if (currentActivity instanceof GPRSCollectorSettingActivity) {
                        GPRSCollectorSettingActivity gprsCollectorSettingActivity = (GPRSCollectorSettingActivity) currentActivity;
                        ((ImageView) gprsCollectorSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    } else if (currentActivity instanceof HydrantSettingActivity) {
                        HydrantSettingActivity hydrantSettingActivity = (HydrantSettingActivity) currentActivity;
                        ((ImageView) hydrantSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    } else if (currentActivity instanceof ValveSettingActivity) {
                        ValveSettingActivity valveSettingActivity = (ValveSettingActivity) currentActivity;
                        ((ImageView) valveSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    } else if (currentActivity instanceof LoRaSettingActivity) {
                        LoRaSettingActivity loRaSettingActivity = (LoRaSettingActivity) currentActivity;
                        ((ImageView) loRaSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_connected);
                    }
                    break;
                case 1:
                    disConnect();
                    theActivity.iv_search_bluetooth_device.setImageResource(R.drawable.bluetooth_disconnected);
                    theActivity.tv_bluetoothStatus.setText("未连接蓝牙工具");
                    CommonUtils.showToast(context, "蓝牙连接断开");
                    if (currentActivity instanceof WaterMeterSettingActivity) {
                        WaterMeterSettingActivity waterMeterSettingActivity = (WaterMeterSettingActivity) currentActivity;
                        ((ImageView) waterMeterSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    } else if (currentActivity instanceof HeatMeterSettingActivity) {
                        HeatMeterSettingActivity heatMeterSettingActivity = (HeatMeterSettingActivity) currentActivity;
                        ((ImageView) heatMeterSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    } else if (currentActivity instanceof GPRSCollectorSettingActivity) {
                        GPRSCollectorSettingActivity gprsCollectorSettingActivity = (GPRSCollectorSettingActivity) currentActivity;
                        ((ImageView) gprsCollectorSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    } else if (currentActivity instanceof HydrantSettingActivity) {
                        HydrantSettingActivity hydrantSettingActivity = (HydrantSettingActivity) currentActivity;
                        ((ImageView) hydrantSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    } else if (currentActivity instanceof ValveSettingActivity) {
                        ValveSettingActivity valveSettingActivity = (ValveSettingActivity) currentActivity;
                        ((ImageView) valveSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    } else if (currentActivity instanceof LoRaSettingActivity) {
                        LoRaSettingActivity loRaSettingActivity = (LoRaSettingActivity) currentActivity;
                        ((ImageView) loRaSettingActivity.findViewById(R.id.iv_search_bluetooth_device)).setImageResource(R.drawable.bluetooth_disconnected);
                    }
                    break;
                case 2:
                    Protocol protocol = new Protocol();
                    MBUS mbus = new MBUS();
                    SSumHeat ssumheat = new SSumHeat();
                    ParameterProtocol parameterprotocol = new ParameterProtocol();
                    String data = BluetoothToolsMainActivity.data.toUpperCase().replace(" ", "");
                    LogUtils.d("bluetooth", "接收到的数据为：" + data);
                    if (currentActivity instanceof WaterMeterSettingActivity) {
                        //水表操作页面
                        WaterMeterSettingActivity waterMeterSettingActivity = (WaterMeterSettingActivity) currentActivity;
                        Fragment subFragment = waterMeterSettingActivity.getSupportFragmentManager().getFragments().get(currentFragment);
                        if (subFragment instanceof WaterMeterDataFragment) {
                            //水表信息读取页面
                            WaterMeterDataFragment waterMeterDataFragment = (WaterMeterDataFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.WATER_METER_READ_METER_DATA:
                                    //水表读数据
                                    if (((CheckBox) waterMeterDataFragment.getView().findViewById(R.id.readdata_check)).isChecked()) {
                                        //单位标准化
                                        BluetoothAnalysisUtils.dataUnitToStandard(protocol);
                                    }
                                    ((TextView) waterMeterDataFragment.getView().findViewById(R.id.et_meterId)).setText(protocol.getMeterID());
                                    waterMeterDataFragment.addListView(protocol);
                                    CommonUtils.showToast(context, "读取水表数据成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof WaterMeterLcdFragment) {
                            //水表液晶屏显示页面
                            CommonUtils.showToast(context, "指令发送成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (subFragment instanceof WaterMeterParameterFragment) {
                            //水表参数设置页面
                            WaterMeterParameterFragment waterMeterParameterFragment = (WaterMeterParameterFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.WATER_METER_READ_METER_INTER_PARAMETER:
                                    ((TextView) waterMeterParameterFragment.getView().findViewById(R.id.et_meterId)).setText(parameterprotocol.getMeterid());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextslope)).setText(parameterprotocol.getSlope());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextstartf)).setText(parameterprotocol.getStartx());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextamendx)).setText(parameterprotocol.getAmendx());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextdiv1)).setText(parameterprotocol.getDivid1());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextdiv2)).setText(parameterprotocol.getDivid2());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextdiv3)).setText(parameterprotocol.getDivid3());
                                    ((EditText) waterMeterParameterFragment.getView().findViewById(R.id.EditTextsleeptime)).setText(parameterprotocol.getSleeptime());//003600
                                    if (parameterprotocol.getUnitStr().equals("003600")) {
                                        ((RadioButton) waterMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(true);
                                        ((RadioButton) waterMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(false);
                                    } else if (parameterprotocol.getUnitStr().equals("001000")) {
                                        ((RadioButton) waterMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(false);
                                        ((RadioButton) waterMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(true);
                                    }
                                    CommonUtils.showToast(context, "读取水表参数成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.WATER_METER_CORRECTION_TIME:
                                    if (data.contains("8403A015")) {
                                        CommonUtils.showToast(context, "校正时钟成功");
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                case ProductType.WATER_METER_OPEN_CLOSE_VALVE:
                                    if (data.contains("8404A0170055")) {
                                        CommonUtils.showToast(context, "开阀指令发送成功");
                                        BluetoothToolsMainActivity.data = "";
                                    } else if (data.contains("8404A0170099")) {
                                        CommonUtils.showToast(context, "关阀指令发送成功");
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof WaterMeterAdjustFragment) {
                            //水表系数调整页面
                            WaterMeterAdjustFragment waterMeterAdjustFragment = (WaterMeterAdjustFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                //读取水表内部参数
                                case ProductType.WATER_METER_READ_METER_INTER_PARAMETER:
                                    EditText et_meterId = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_meterId);
                                    EditText et_qn_1 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn_1);
                                    EditText et_qn2_1 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn2_1);
                                    EditText et_qn1_1 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn1_1);
                                    EditText et_qmin_1 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qmin_1);
                                    TextView tv_qn_2 = (TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_qn_2);
                                    TextView tv_qn2_2 = (TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_qn2_2);
                                    TextView tv_qn1_2 = (TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_qn1_2);
                                    TextView tv_qmin_2 = (TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_qmin_2);
                                    EditText et_qn_3 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn_3);
                                    EditText et_qn2_3 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn2_3);
                                    EditText et_qn1_3 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qn1_3);
                                    EditText et_qmin_3 = (EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_qmin_3);
                                    et_meterId.setText(parameterprotocol.getMeterid());
                                    double qnerr, qn2err, qn1err, qminerr, qnold, qn2old, qn1old, qminold, qnnew, qn2new, qn1new, qminnew = 0;
                                    try {
                                        qnerr = Double.valueOf(et_qn_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qnerr = 0;
                                    }
                                    try {
                                        qn2err = Double.valueOf(et_qn2_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn2err = 0;
                                    }
                                    try {
                                        qn1err = Double.valueOf(et_qn1_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn1err = 0;
                                    }
                                    try {
                                        qminerr = Double.valueOf(et_qmin_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qminerr = 0;
                                    }
                                    tv_qn_2.setText(parameterprotocol.getQnx());
                                    tv_qn2_2.setText(parameterprotocol.getQn2x());
                                    tv_qn1_2.setText(parameterprotocol.getQn1x());
                                    tv_qmin_2.setText(parameterprotocol.getQminx());
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("setQnParam", Context.MODE_PRIVATE);
                                    String nMeterid = parameterprotocol.getMeterid();

                                    Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                                    if (!map.keySet().contains(nMeterid + "qn_2") &&
                                            !map.keySet().contains(nMeterid + "qn1_2") &&
                                            !map.keySet().contains(nMeterid + "qn2_2") &&
                                            !map.keySet().contains(nMeterid + "qmin_2")) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(nMeterid + "qn_2", parameterprotocol.getQnx());
                                        editor.putString(nMeterid + "qn1_2", parameterprotocol.getQn1x());
                                        editor.putString(nMeterid + "qn2_2", parameterprotocol.getQn2x());
                                        editor.putString(nMeterid + "qmin_2", parameterprotocol.getQminx());
                                        editor.apply();//提交修改
                                    }

                                    try {
                                        qnold = Double.valueOf(tv_qn_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qnold = 0;
                                    }
                                    try {
                                        qn2old = Double.valueOf(tv_qn2_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn2old = 0;
                                    }
                                    try {
                                        qn1old = Double.valueOf(tv_qn1_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn1old = 0;
                                    }
                                    try {
                                        qminold = Double.valueOf(tv_qmin_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qminold = 0;
                                    }
                                    qnnew = qnold * (1 + qnerr * 0.01);
                                    qn2new = qn2old * (1 + qn2err * 0.01);
                                    qn1new = qn1old * (1 + qn1err * 0.01);
                                    qminnew = qminold * (1 + qminerr * 0.01);
                                    et_qn_3.setText(String.valueOf((int) qnnew));
                                    et_qn2_3.setText(String.valueOf((int) qn2new));
                                    et_qn1_3.setText(String.valueOf((int) qn1new));
                                    et_qmin_3.setText(String.valueOf((int) qminnew));
                                    CommonUtils.showToast(context, "读取流量系数成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.WATER_METER_READ_METER_DATA:
                                    //读取水表流量
                                    ((EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_meterId)).setText(data.substring(4, 12));
                                    WaterMeterAdjustFragment.flowUnit_positive = AnalysisUtils.getFlowMultiple(protocol.getTotalUnit());        //正向流量单位
                                    WaterMeterAdjustFragment.flowUnit_reserve = AnalysisUtils.getFlowMultiple(protocol.getOppositeTotalUnit()); //反向流量单位
                                    //单位标准化
                                    BluetoothAnalysisUtils.dataUnitToStandard(protocol);
                                    ((TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_flow_positive)).setText(String.valueOf(protocol.getTotal()));
                                    ((TextView) waterMeterAdjustFragment.getView().findViewById(R.id.tv_flow_reserve)).setText(String.valueOf(protocol.getOppositeTotal()));
                                    ((EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_consumption_positive)).setText("");
                                    ((EditText) waterMeterAdjustFragment.getView().findViewById(R.id.et_consumption_reverse)).setText("");
                                    CommonUtils.showToast(context, "读取流量成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof WaterMeterSetPressureFragment) {
                            WaterMeterSetPressureFragment waterMeterSetPressureFragment = (WaterMeterSetPressureFragment) subFragment;
                            View view = waterMeterSetPressureFragment.getView();
                            if (view != null) {
                                switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                    case ProductType.WATER_METER_SET_PRESSURE_SUCCESS:
                                        theActivity.showToast("标定成功");
                                        BluetoothToolsMainActivity.data = "";
                                        break;
                                    case ProductType.WATER_METER_SET_PRESSURE_FAIL:
                                        theActivity.showToast("标定失败");
                                        BluetoothToolsMainActivity.data = "";
                                        break;
                                    case ProductType.WATER_METER_READ_PRESSURE:
                                        //零点AD采样值
                                        int samplingValueZeroPoint = AnalysisUtils.HexS8ToInt(data.substring(26, 34));
                                        TextView tvSamplingValueZeroPoint = view.findViewById(R.id.tv_sampling_value_zero_point);
                                        tvSamplingValueZeroPoint.setText(String.valueOf(samplingValueZeroPoint));
                                        //常用点AD采样值
                                        int samplingValueCommonPoint = AnalysisUtils.HexS8ToInt(data.substring(42, 50));
                                        TextView tvSamplingValueCommonPoint = view.findViewById(R.id.tv_sampling_value_common_point);
                                        tvSamplingValueCommonPoint.setText(String.valueOf(samplingValueCommonPoint));
                                        //当前常用点
                                        float currentCommonPoint = AnalysisUtils.HexS8ToInt(data.substring(34, 42)) / 100000f;
                                        TextView tvCurrentCommonPoint = view.findViewById(R.id.tv_current_common_point);
                                        tvCurrentCommonPoint.setText("" + new DecimalFormat("##0.00").format(currentCommonPoint) + "bar");
                                        theActivity.showToast("读取压力标定成功");
                                        BluetoothToolsMainActivity.data = "";
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    } else if (currentActivity instanceof HeatMeterSettingActivity) {
                        //热表操作页面
                        HeatMeterSettingActivity heatMeterSettingActivity = (HeatMeterSettingActivity) currentActivity;
                        Fragment subFragment = heatMeterSettingActivity.getSupportFragmentManager().getFragments().get(currentFragment);
                        if (subFragment instanceof HeatMeterDataFragment) {
                            HeatMeterDataFragment heatMeterDataFragment = (HeatMeterDataFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.HEAT_METER_READ_METER_DATA: {
                                    //读表数据
                                    if (((CheckBox) heatMeterDataFragment.getView().findViewById(R.id.readdata_check)).isChecked()) {
                                        //单位标准化
                                        BluetoothAnalysisUtils.dataUnitToStandard(protocol);
                                    }
                                    ((TextView) heatMeterDataFragment.getView().findViewById(R.id.et_meterId)).setText(protocol.getMeterID());
                                    heatMeterDataFragment.addListView(protocol);
                                    heatMeterDataFragment.meterId = "FFFFFFFF";
                                    CommonUtils.showToast(context, "读取热表数据成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                }
                                case ProductType.HEAT_METER_READ_METER_INTER_PARAMETER: {
                                    //读内部参数，获取表号后再读表数据
                                    heatMeterDataFragment.meterId = parameterprotocol.getMeterid();
                                    (heatMeterDataFragment.getView().findViewById(R.id.btn_read_parameter)).performClick();
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                }
                            }
                        } else if (subFragment instanceof HeatMeterLcdFragment) {
                            CommonUtils.showToast(context, "指令发送成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (subFragment instanceof HeatMeterParameterFragment) {
                            HeatMeterParameterFragment heatMeterParameterFragment = (HeatMeterParameterFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.HEAT_METER_READ_METER_INTER_PARAMETER:
                                    ((TextView) heatMeterParameterFragment.getView().findViewById(R.id.et_meterId)).setText(parameterprotocol.getMeterid());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextslope)).setText(parameterprotocol.getSlope());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextstartf)).setText(parameterprotocol.getStartx());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextamendx)).setText(parameterprotocol.getAmendx());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextdiv1)).setText(parameterprotocol.getDivid1());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextdiv2)).setText(parameterprotocol.getDivid2());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextdiv3)).setText(parameterprotocol.getDivid3());
                                    ((EditText) heatMeterParameterFragment.getView().findViewById(R.id.EditTextsleeptime)).setText(parameterprotocol.getSleeptime());//003600
                                    if (parameterprotocol.getUnitStr().equals("003600")) {
                                        ((RadioButton) heatMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(true);
                                        ((RadioButton) heatMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(false);
                                    } else if (parameterprotocol.getUnitStr().equals("001000")) {
                                        ((RadioButton) heatMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(false);
                                        ((RadioButton) heatMeterParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(true);
                                    }
                                    CommonUtils.showToast(context, "读取热表参数成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof HeatMeterAdjustFragment) {
                            HeatMeterAdjustFragment heatMeterAdjustFragment = (HeatMeterAdjustFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case 1: {
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                }
                                case ProductType.HEAT_METER_READ_METER_INTER_PARAMETER: {
                                    EditText et_meterId = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_meterId);
                                    EditText et_qn_1 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn_1);
                                    EditText et_qn2_1 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn2_1);
                                    EditText et_qn1_1 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn1_1);
                                    EditText et_qmin_1 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qmin_1);
                                    TextView tv_qn_2 = (TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_qn_2);
                                    TextView tv_qn2_2 = (TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_qn2_2);
                                    TextView tv_qn1_2 = (TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_qn1_2);
                                    TextView tv_qmin_2 = (TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_qmin_2);
                                    EditText et_qn_3 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn_3);
                                    EditText et_qn2_3 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn2_3);
                                    EditText et_qn1_3 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qn1_3);
                                    EditText et_qmin_3 = (EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_qmin_3);
                                    et_meterId.setText(parameterprotocol.getMeterid());
                                    heatMeterAdjustFragment.meterId = parameterprotocol.getMeterid();
                                    double qnerr, qn2err, qn1err, qminerr, qnold, qn2old, qn1old, qminold, qnnew, qn2new, qn1new, qminnew = 0;
                                    try {
                                        qnerr = Double.valueOf(et_qn_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qnerr = 0;
                                    }
                                    try {
                                        qn2err = Double.valueOf(et_qn2_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn2err = 0;
                                    }
                                    try {
                                        qn1err = Double.valueOf(et_qn1_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn1err = 0;
                                    }
                                    try {
                                        qminerr = Double.valueOf(et_qmin_1.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qminerr = 0;
                                    }
                                    tv_qn_2.setText(parameterprotocol.getQnx());
                                    tv_qn2_2.setText(parameterprotocol.getQn2x());
                                    tv_qn1_2.setText(parameterprotocol.getQn1x());
                                    tv_qmin_2.setText(parameterprotocol.getQminx());
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("setQnParam", Context.MODE_PRIVATE);
                                    String nMeterid = parameterprotocol.getMeterid();

                                    Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                                    if (!map.keySet().contains(nMeterid + "qn_2") &&
                                            !map.keySet().contains(nMeterid + "qn1_2") &&
                                            !map.keySet().contains(nMeterid + "qn2_2") &&
                                            !map.keySet().contains(nMeterid + "qmin_2")) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(nMeterid + "qn_2", parameterprotocol.getQnx());
                                        editor.putString(nMeterid + "qn1_2", parameterprotocol.getQn1x());
                                        editor.putString(nMeterid + "qn2_2", parameterprotocol.getQn2x());
                                        editor.putString(nMeterid + "qmin_2", parameterprotocol.getQminx());
                                        editor.apply();//提交修改
                                    }

                                    try {
                                        qnold = Double.valueOf(tv_qn_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qnold = 0;
                                    }
                                    try {
                                        qn2old = Double.valueOf(tv_qn2_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn2old = 0;
                                    }
                                    try {
                                        qn1old = Double.valueOf(tv_qn1_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qn1old = 0;
                                    }
                                    try {
                                        qminold = Double.valueOf(tv_qmin_2.getText().toString().replace(" ", ""));
                                    } catch (Exception e) {
                                        qminold = 0;
                                    }
                                    qnnew = qnold * (1 + qnerr * 0.01);
                                    qn2new = qn2old * (1 + qn2err * 0.01);
                                    qn1new = qn1old * (1 + qn1err * 0.01);
                                    qminnew = qminold * (1 + qminerr * 0.01);
                                    et_qn_3.setText(String.valueOf((int) qnnew));
                                    et_qn2_3.setText(String.valueOf((int) qn2new));
                                    et_qn1_3.setText(String.valueOf((int) qn1new));
                                    et_qmin_3.setText(String.valueOf((int) qminnew));
                                    BluetoothToolsMainActivity.data = "";
                                    (heatMeterAdjustFragment.getView().findViewById(R.id.btn_read_flow)).performClick();
                                    break;
                                }
                                case ProductType.HEAT_METER_READ_METER_DATA: {
                                    //读取冷热量、流量
                                    ((EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_meterId)).setText(protocol.getMeterID());
                                    heatMeterAdjustFragment.meterId = "";
                                    CommonUtils.showToast(context, "数据读取成功");
                                    //读取原始单位
                                    HeatMeterAdjustFragment.coldUnit = AnalysisUtils.getHeatMultiple(protocol.getSumCoolUnit());
                                    HeatMeterAdjustFragment.heatUnit = AnalysisUtils.getHeatMultiple(protocol.getSumHeatUnit());
                                    HeatMeterAdjustFragment.flowUnit = AnalysisUtils.getFlowMultiple(protocol.getTotalUnit());
                                    LogUtils.d("unit", protocol.getSumCoolUnit() + "——" + protocol.getSumHeatUnit() + "——" + protocol.getTotalUnit());
                                    //单位标准化
                                    BluetoothAnalysisUtils.dataUnitToStandard(protocol);
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_quantity_cold)).setText(String.valueOf(protocol.getSumCool()));
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_quantity_heat)).setText(String.valueOf(protocol.getSumHeat()));
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_consumption_positive)).setText(String.valueOf(protocol.getTotal()));
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_unit_cold)).setText(protocol.getSumCoolUnit());
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_unit_heat)).setText(protocol.getSumHeatUnit());
                                    ((TextView) heatMeterAdjustFragment.getView().findViewById(R.id.tv_unit_positive)).setText(protocol.getTotalUnit());
                                    ((EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_quantity_heat)).setText("");
                                    ((EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_quantity_cold)).setText("");
                                    ((EditText) heatMeterAdjustFragment.getView().findViewById(R.id.et_consumption_positive)).setText("");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                }
                            }
                        }
                    } else if (currentActivity instanceof HydrantSettingActivity) {
                        HydrantSettingActivity hydrantSettingActivity = (HydrantSettingActivity) currentActivity;
                        Fragment subFragment = hydrantSettingActivity.getSupportFragmentManager().getFragments().get(currentFragment);
                        if (subFragment instanceof HydrantDataFragment) {
                            HydrantDataFragment hydrantDataFragment = (HydrantDataFragment) subFragment;
                            if (data == null || data.equals("")) {
                                return;
                            }
                            String meterId, state1, state2, state3, current_amount_unit;
                            EditText et_deviceId = ((EditText) hydrantDataFragment.getView().findViewById(R.id.et_deviceId));
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.HYDRANT_READ_WARNING_PARAMETER:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    String pressure_mutation = format(changeCode(data.substring(26, 30)), 0.01);          //压力突变阈值
                                    String pressure_low_limit = format(changeCode(data.substring(30, 34)), 0.01);         //压力下限阈值
                                    String leakage_flow_rate = format(changeCode(data.substring(34, 38)), 0.01);          //漏水流速
                                    String leaking_timeout = format(changeCode(data.substring(38, 42)), 1);               //漏水超时
                                    String lower_temperature_limit = format(changeCode(data.substring(42, 46)), 0.01);    //温度下限
                                    String vibration_variation = format(changeCode(data.substring(46, 50)), 1);           //震动变化量
                                    ((EditText) hydrantDataFragment.getView().findViewById(R.id.et_deviceId)).setText(meterId);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_pressure_mutation1)).setText(pressure_mutation);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_pressure_low_limit1)).setText(pressure_low_limit);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_leakage_flow_rate1)).setText(leakage_flow_rate);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_leaking_timeout1)).setText(leaking_timeout);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_lower_temperature_limit1)).setText(lower_temperature_limit);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_vibration_variation1)).setText(vibration_variation);
                                    SharedPreferencesUtils.getInstance().saveData("has_read_alarm_parameters", true);
                                    CommonUtils.showToast(context, "读取报警参数成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_WARNING_STATE:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    state1 = data.substring(26, 28);                                                  //截取报警状态位1
                                    state2 = data.substring(28, 30);                                                  //截取报警状态位2
                                    state3 = data.substring(30, 32);                                                  //截取报警状态位1
                                    showState1(hydrantDataFragment, state1);
                                    showState2(hydrantDataFragment, state2);
                                    showState3(hydrantDataFragment, state3);
                                    CommonUtils.showToast(context, "读取报警状态成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_METER_DATA:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    double voltage = new BigDecimal(Integer.parseInt(data.substring(26, 28), 16) * 2 * 0.01).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();//电压，保留1位小数
                                    String cumulative_flow = format(changeCode(data.substring(28, 36)), AnalysisUtils.getFlowMultiple(data.substring(36, 38)));                    //累计流量
                                    String reverse_cumulative_flow = format(changeCode(data.substring(38, 46)), AnalysisUtils.getFlowMultiple(data.substring(46, 48)));            //反向流量
                                    String instantaneous_flow = format(changeCode(data.substring(48, 56)), 0.01);                //瞬时流量
                                    String pressure_value = format(changeCode(data.substring(62, 68)), 0.01);                    //压力
                                    state1 = data.substring(82, 84);                                                      //截取报警状态位1
                                    state2 = data.substring(84, 86);                                                      //截取报警状态位2
                                    state3 = data.substring(86, 88);                                                      //截取报警状态位1
                                    String time = data.substring(80, 82) + data.substring(78, 80) + "-" + data.substring(76, 78) + "-" + data.substring(74, 76) + "     " + data.substring(72, 74) + ":" + data.substring(70, 72) + ":" + data.substring(68, 70);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_battery_voltage)).setText(getPrettyNumber(String.valueOf(voltage)));
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_cumulative_flow)).setText(cumulative_flow);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_reverse_cumulative_flow)).setText(reverse_cumulative_flow);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_instantaneous_flow)).setText(instantaneous_flow);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_pressure_value)).setText(pressure_value);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_time)).setText(time);
                                    showState1(hydrantDataFragment, state1);
                                    showState2(hydrantDataFragment, state2);
                                    showState3(hydrantDataFragment, state3);
                                    CommonUtils.showToast(context, "读取表数据成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_CURRENT_USE_DATA:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    String currenttime = data.substring(58, 60) + data.substring(56, 58) + "-" + data.substring(54, 56) + "-" + data.substring(52, 54) + "     " + data.substring(50, 52) + ":" + data.substring(48, 50) + ":" + data.substring(46, 48);
                                    String lock_device = data.substring(26, 28);
                                    String current_userID = data.substring(28, 36);
                                    String current_amount = changeCode(data.substring(36, 44));
                                    current_amount_unit = data.substring(44, 46);
                                    String current_useState = data.substring(60, 62);
                                    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(current_amount) * AnalysisUtils.getFlowMultiple(current_amount_unit));
                                    double water_consumption = bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();//保留三位小数
                                    if (lock_device.equals("00")) {
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_userID)).setText(hydrantDataFragment.getString(R.string.no_user));
                                    } else {
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_time2)).setText(currenttime);
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_userID)).setText(current_userID);
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_amount)).setText(String.format(hydrantDataFragment.getString(R.string.exampleConsumption), String.valueOf(water_consumption)));
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_lock_device)).setText(AnalysisUtils.getOpenValveMethod(lock_device));
                                        switch (current_useState) {
                                            case "00":
                                                ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_useState)).setText("阀门锁开启");
                                                break;
                                            case "01":
                                                ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_useState)).setText("正在用水");
                                                break;
                                            case "02":
                                                ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_useState)).setText("结束使用");
                                                break;
                                            default:
                                                break;
                                        }
                                        String alarm = data.substring(62, 64);
                                        int m = Integer.valueOf(alarm, 16);
                                        if ((m & 0x02) == 0x02) {
                                            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_alarm_not_close_valve)).setText("阀门未关紧");
                                        } else {
                                            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_alarm_not_close_valve)).setText("阀门已关紧");
                                        }
                                        if ((m & 0x04) == 0x04) {
                                            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_alarm_leakage_withoutUser)).setText("漏水中");
                                        } else {
                                            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_alarm_leakage_withoutUser)).setText("未漏水");
                                        }
                                    }
                                    CommonUtils.showToast(context, "读取当前使用数据成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_HISTORY_USE_DATA:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    int current_order_number = Integer.parseInt(changeCode(data.substring(26, 30)));
                                    int total_numbers = Integer.parseInt(changeCode(data.substring(30, 34)), 16);
                                    String user_appid = "00000000";
                                    if (!data.substring(36, 44).equals("FFFFFFFF")) {
                                        user_appid = changeCode(data.substring(36, 44));
                                    }
                                    String open_device = data.substring(34, 36);
                                    String open_time = data.substring(56, 58) + data.substring(54, 56) + "-" + data.substring(52, 54) + "-" + data.substring(50, 52) + "     " + data.substring(48, 50) + ":" + data.substring(46, 48) + ":" + data.substring(44, 46);
                                    String endtime = data.substring(80, 82) + data.substring(78, 80) + "-" + data.substring(76, 78) + "-" + data.substring(74, 76) + "     " + data.substring(72, 74) + ":" + data.substring(70, 72) + ":" + data.substring(68, 70);
                                    String water_consumption2 = data.substring(58, 66);
                                    String water_consumption_unit = data.substring(66, 68);
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_total_numbers1)).setText(String.valueOf(total_numbers));
                                    ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_total_numbers2)).setText(String.valueOf(total_numbers));
                                    if (total_numbers != 0) {
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_current_order_number)).setText(String.valueOf(current_order_number + 1));
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_user_appid)).setText(user_appid);
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_device)).setText(AnalysisUtils.getOpenValveMethod(open_device));
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_time)).setText(open_time);
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_endTime)).setText(endtime);
                                        BigDecimal bigDecimal2 = new BigDecimal((Double.valueOf(water_consumption2) * AnalysisUtils.getFlowMultiple(water_consumption_unit)));
                                        double water_consumption3 = bigDecimal2.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();//保留三位小数
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_water_consumption)).setText(String.format(hydrantDataFragment.getString(R.string.exampleConsumption), String.valueOf(water_consumption3)));
                                    }
                                    CommonUtils.showToast(context, "读取历史使用数据成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_WARNING_ENABLE:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    String enabled1 = data.substring(26, 28);                                                  //截取报警状态位1
                                    String enabled2 = data.substring(28, 30);                                                  //截取报警状态位2
                                    String enabled3 = data.substring(30, 32);                                                  //截取报警状态位1
                                    showEnabled1(hydrantDataFragment, enabled1);
                                    showEnabled2(hydrantDataFragment, enabled2);
                                    showEnabled3(hydrantDataFragment, enabled3);
                                    SharedPreferencesUtils.getInstance().saveData("has_read_alarm_enabled", true);
                                    CommonUtils.showToast(context, "读取报警使能成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_READ_GPS:
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    if (Integer.parseInt(data.substring(28, 36)) == 0) {
                                        CommonUtils.showToast(context, "没有定位记录，请刷新GPS信息");
                                        BluetoothToolsMainActivity.data = "";
                                    } else {
                                        convertHexToString(data.substring(30, 116));
                                        //GPS纬度
                                        String lat = convertHexToString(data.substring(28, 58));
                                        double lat2 = Double.parseDouble(lat.substring(0, 2)) + Double.parseDouble(lat.substring(2, 15)) / 60;
                                        //GPS经度
                                        String lng = convertHexToString(data.substring(58, 88));
                                        double lng2 = Double.parseDouble(lng.substring(0, 3)) + Double.parseDouble(lng.substring(3, 15)) / 60;
                                        GPSUtils.wgs2bd(lat2, lng2);
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_lng)).setText(String.valueOf(new DecimalFormat(".######").format(lng2)));
                                        ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_lat)).setText(String.valueOf(new DecimalFormat(".######").format(lat2)));
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                case ProductType.HYDRANT_OPEN_CLOSE_VALVE:
                                    //收到下位机发送的开关锁返回信息
                                    meterId = data.substring(4, 12);
                                    et_deviceId.setText(meterId);
                                    TextView tv_socket2 = (TextView) hydrantDataFragment.getView().findViewById(R.id.tv_socket2);
                                    refreshSocketText(TimeUtils.getCurrentTime() + "消火栓" + meterId + "返回开关阀门锁状态数据原文：" + data + "\n", tv_socket2);
                                    String device = AnalysisUtils.getOpenValveMethod(data.substring(26, 28));
                                    if (data.substring(36, 38).endsWith("00")) {
                                        refreshSocketText(TimeUtils.getCurrentTime() + "消火栓" + meterId + "阀门锁状态：开启，" + "开锁设备：" + device + "\n", tv_socket2);
                                        BluetoothToolsMainActivity.data = "";
                                    } else if (data.substring(36, 38).endsWith("01")) {
                                        refreshSocketText(TimeUtils.getCurrentTime() + "消火栓" + meterId + "阀门锁状态：关闭" + "\n", tv_socket2);
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof HydrantParameterFragment) {
                            HydrantParameterFragment hydrantParameterFragment = (HydrantParameterFragment) subFragment;
                            switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                case ProductType.HYDRANT_READ_METER_INTER_PARAMETER:
                                    ((TextView) hydrantParameterFragment.getView().findViewById(R.id.tv_meterId)).setText(parameterprotocol.getMeterid());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextslope)).setText(parameterprotocol.getSlope());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextstartf)).setText(parameterprotocol.getStartx());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextamendx)).setText(parameterprotocol.getAmendx());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextdiv1)).setText(parameterprotocol.getDivid1());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextdiv2)).setText(parameterprotocol.getDivid2());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextdiv3)).setText(parameterprotocol.getDivid3());
                                    ((EditText) hydrantParameterFragment.getView().findViewById(R.id.EditTextsleeptime)).setText(parameterprotocol.getSleeptime());//003600
                                    if (parameterprotocol.getUnitStr().equals("003600")) {
                                        ((RadioButton) hydrantParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(true);
                                        ((RadioButton) hydrantParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(false);
                                    } else if (parameterprotocol.getUnitStr().equals("001000")) {
                                        ((RadioButton) hydrantParameterFragment.getView().findViewById(R.id.RadioButtonunitm)).setChecked(false);
                                        ((RadioButton) hydrantParameterFragment.getView().findViewById(R.id.RadioButtonunitgal)).setChecked(true);
                                    }
                                    CommonUtils.showToast(context, "读取消火栓参数成功");
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                case ProductType.HYDRANT_CORRECTION_TIME:
                                    if (data.contains("8403A015")) {
                                        CommonUtils.showToast(context, "校正时钟成功");
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                case ProductType.HYDRANT_OPEN_CLOSE_VALVE:
                                    if (data.contains("8404A0170055")) {
                                        CommonUtils.showToast(context, "开阀指令发送成功");
                                        BluetoothToolsMainActivity.data = "";
                                    } else if (data.contains("8404A0170099")) {
                                        CommonUtils.showToast(context, "关阀指令发送成功");
                                        BluetoothToolsMainActivity.data = "";
                                    }
                                    break;
                                case ProductType.HYDRANT_CHANGE_TYPE:
                                    int type;
                                    switch (data.substring(26, 28)) {
                                        case "01":
                                            type = R.id.radioButton_typeI;
                                            break;
                                        case "02":
                                            type = R.id.radioButton_typeII;
                                            break;
                                        case "FF":
                                            type = R.id.radioButton_typeIII;
                                            break;
                                        default:
                                            type = -1;
                                    }
                                    if (type != -1) {
                                        ((RadioGroup) hydrantParameterFragment.getView().findViewById(R.id.radioGroup_type)).check(type);
                                        CommonUtils.showToast(context, "读取消火栓模式成功");
                                    } else {
                                        CommonUtils.showToast(context, "未读取到消火栓模式");
                                    }
                                    BluetoothToolsMainActivity.data = "";
                                    break;
                                default:
                                    break;
                            }
                        } else if (subFragment instanceof HydrantAdjustFragment) {
                            //消火栓系数调整页面
                            HydrantAdjustFragment hydrantAdjustFragment = (HydrantAdjustFragment) subFragment;
                            View view = hydrantAdjustFragment.getView();
                            if (view != null) {
                                if (data == null || "".equals(data)) {
                                    return;
                                } else {
                                    System.out.println("data:" + data);
                                    int start7B = data.indexOf("7B");
                                    if (start7B != -1) {
                                        String newData = data.substring(start7B + 2);
                                        System.out.println("newData:" + newData);
                                        int end7B = newData.lastIndexOf("7B");
                                        if (end7B != -1) {
                                            int length = AnalysisUtils.HexS2ToInt(newData.substring(4, 6));
                                            String aframe = newData.substring(0, end7B);
                                            if (aframe.length() == 2 * (length - 2)) {
                                                System.out.println("aframe:" + aframe);
                                                StringBuilder builder = new StringBuilder();
                                                builder.append(aframe);
                                                if (builder.indexOf("BB078054") == 46) {
                                                    theActivity.showToast("标定成功");
                                                    BluetoothToolsMainActivity.data = "";
                                                } else if (builder.indexOf("7B078054") == 46) {
                                                    theActivity.showToast("标定失败");
                                                    BluetoothToolsMainActivity.data = "";
                                                } else if (builder.indexOf("BB0E8055") == 46) {
                                                    //零点AD采样值
                                                    int samplingValueZeroPoint = AnalysisUtils.HexS8ToInt(builder.substring(54, 62));
                                                    TextView tvSamplingValueZeroPoint = view.findViewById(R.id.tv_sampling_value_zero_point);
                                                    tvSamplingValueZeroPoint.setText(String.valueOf(samplingValueZeroPoint));
                                                    //常用点AD采样值
                                                    int samplingValueCommonPoint = AnalysisUtils.HexS8ToInt(builder.substring(70, 78));
                                                    TextView tvSamplingValueCommonPoint = view.findViewById(R.id.tv_sampling_value_common_point);
                                                    tvSamplingValueCommonPoint.setText(String.valueOf(samplingValueCommonPoint));
                                                    //当前常用点
                                                    float currentCommonPoint = AnalysisUtils.HexS8ToInt(builder.substring(62, 70)) / 100000f;
                                                    TextView tvCurrentCommonPoint = view.findViewById(R.id.tv_current_common_point);
                                                    tvCurrentCommonPoint.setText("" + new DecimalFormat("##0.00").format(currentCommonPoint) + "bar");
                                                    theActivity.showToast("读取压力标定成功");
                                                    BluetoothToolsMainActivity.data = "";
                                                }
                                            }
                                        }
                                    }
                                }
                                switch (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol)) {
                                    //读取消火栓内部参数
                                    case ProductType.HYDRANT_READ_METER_INTER_PARAMETER:
                                        EditText et_meterId = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_meterId);
                                        EditText et_qn_1 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn_1);
                                        EditText et_qn2_1 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn2_1);
                                        EditText et_qn1_1 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn1_1);
                                        EditText et_qmin_1 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qmin_1);
                                        TextView tv_qn_2 = (TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_qn_2);
                                        TextView tv_qn2_2 = (TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_qn2_2);
                                        TextView tv_qn1_2 = (TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_qn1_2);
                                        TextView tv_qmin_2 = (TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_qmin_2);
                                        EditText et_qn_3 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn_3);
                                        EditText et_qn2_3 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn2_3);
                                        EditText et_qn1_3 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qn1_3);
                                        EditText et_qmin_3 = (EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_qmin_3);
                                        et_meterId.setText(parameterprotocol.getMeterid());
                                        double qnerr, qn2err, qn1err, qminerr, qnold, qn2old, qn1old, qminold, qnnew, qn2new, qn1new, qminnew = 0;
                                        try {
                                            qnerr = Double.valueOf(et_qn_1.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qnerr = 0;
                                        }
                                        try {
                                            qn2err = Double.valueOf(et_qn2_1.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qn2err = 0;
                                        }
                                        try {
                                            qn1err = Double.valueOf(et_qn1_1.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qn1err = 0;
                                        }
                                        try {
                                            qminerr = Double.valueOf(et_qmin_1.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qminerr = 0;
                                        }
                                        tv_qn_2.setText(parameterprotocol.getQnx());
                                        tv_qn2_2.setText(parameterprotocol.getQn2x());
                                        tv_qn1_2.setText(parameterprotocol.getQn1x());
                                        tv_qmin_2.setText(parameterprotocol.getQminx());
                                        SharedPreferences sharedPreferences = context.getSharedPreferences("setQnParam", Context.MODE_PRIVATE);
                                        String nMeterid = parameterprotocol.getMeterid();

                                        Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                                        if (!map.keySet().contains(nMeterid + "qn_2") &&
                                                !map.keySet().contains(nMeterid + "qn1_2") &&
                                                !map.keySet().contains(nMeterid + "qn2_2") &&
                                                !map.keySet().contains(nMeterid + "qmin_2")) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(nMeterid + "qn_2", parameterprotocol.getQnx());
                                            editor.putString(nMeterid + "qn1_2", parameterprotocol.getQn1x());
                                            editor.putString(nMeterid + "qn2_2", parameterprotocol.getQn2x());
                                            editor.putString(nMeterid + "qmin_2", parameterprotocol.getQminx());
                                            editor.apply();//提交修改
                                        }

                                        try {
                                            qnold = Double.valueOf(tv_qn_2.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qnold = 0;
                                        }
                                        try {
                                            qn2old = Double.valueOf(tv_qn2_2.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qn2old = 0;
                                        }
                                        try {
                                            qn1old = Double.valueOf(tv_qn1_2.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qn1old = 0;
                                        }
                                        try {
                                            qminold = Double.valueOf(tv_qmin_2.getText().toString().replace(" ", ""));
                                        } catch (Exception e) {
                                            qminold = 0;
                                        }
                                        qnnew = qnold * (1 + qnerr * 0.01);
                                        qn2new = qn2old * (1 + qn2err * 0.01);
                                        qn1new = qn1old * (1 + qn1err * 0.01);
                                        qminnew = qminold * (1 + qminerr * 0.01);
                                        et_qn_3.setText(String.valueOf((int) qnnew));
                                        et_qn2_3.setText(String.valueOf((int) qn2new));
                                        et_qn1_3.setText(String.valueOf((int) qn1new));
                                        et_qmin_3.setText(String.valueOf((int) qminnew));
                                        CommonUtils.showToast(context, "读取流量系数成功");
                                        BluetoothToolsMainActivity.data = "";
                                        break;
                                    case ProductType.HYDRANT_READ_METER_DATA:
                                        //读取消火栓流量
                                        ((EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_meterId)).setText(data.substring(4, 12));
                                        double flowUnit_positive = AnalysisUtils.getFlowMultiple(data.substring(36, 38));                           //正向流量单位
                                        double flowUnit_reserve = AnalysisUtils.getFlowMultiple(data.substring(46, 48));                            //反向流量单位
                                        HydrantAdjustFragment.flowUnit_positive = flowUnit_positive;
                                        HydrantAdjustFragment.flowUnit_reserve = flowUnit_reserve;
                                        String cumulative_flow = format(changeCode(data.substring(28, 36)), flowUnit_positive);                 //正向流量
                                        String reverse_cumulative_flow = format(changeCode(data.substring(38, 46)), flowUnit_reserve);          //反向流量
                                        ((TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_flow_positive)).setText(cumulative_flow);
                                        ((TextView) hydrantAdjustFragment.getView().findViewById(R.id.tv_flow_reserve)).setText(reverse_cumulative_flow);
                                        ((EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_consumption_positive)).setText("");
                                        ((EditText) hydrantAdjustFragment.getView().findViewById(R.id.et_consumption_reverse)).setText("");
                                        CommonUtils.showToast(context, "读取流量成功");
                                        BluetoothToolsMainActivity.data = "";
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else if (subFragment instanceof HydrantGPRSFragment) {
                            HydrantGPRSFragment hydrantGPRSFragment = (HydrantGPRSFragment) subFragment;
                            if (data == null || data.equals("")) {
                                return;
                            }
                            if (data.contains("4D4F4449465920495031202020204F4B")) {
                                (hydrantGPRSFragment.getView().findViewById(R.id.btnReadComm)).performClick();
                                CommonUtils.showToast(context, "修改网络参数参数成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("494D454920534554204F4B2020202020")) {
                                (hydrantGPRSFragment.getView().findViewById(R.id.btn_read_imei)).performClick();
                                CommonUtils.showToast(context, "修改IMEI号成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            System.out.println("data:" + data);
                            int start7B = data.indexOf("7B");
                            if (start7B != -1) {
                                String newdata = data.substring(start7B + 2);
                                System.out.println("newdata:" + newdata);
                                int end7B = newdata.lastIndexOf("7B");
                                if (end7B != -1) {
                                    int length = AnalysisUtils.HexS2ToInt(newdata.substring(4, 6));
                                    String aframe = newdata.substring(0, end7B);
                                    if (aframe.length() == 2 * (length - 2)) {
                                        System.out.println("aframe:" + aframe);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(aframe);
                                        if (builder.charAt(46) == '8'
                                                && builder.charAt(47) == '7'
                                                && builder.charAt(50) == 'C'
                                                && builder.charAt(51) == '1'
                                                && builder.charAt(52) == '4'
                                                && builder.charAt(53) == '2') {
                                            String imei = aframe.substring(4 + 2, 4 + 2 + 22);
                                            System.out.println(imei);
                                            StringBuilder sb = new StringBuilder();
                                            for (int i = 0; i < imei.length() / 2; i++) {
                                                sb.append((Integer.parseInt(imei.substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            System.out.println(sb.toString());
                                            TextView tv_imei = (TextView) hydrantGPRSFragment.getView().findViewById(R.id.tv_imei);
                                            tv_imei.setText(sb.toString());
                                            tv_imei.setTextColor(context.getResources().getColorStateList(R.color.darkgreen));
                                            CommonUtils.showToast(context, "读取IMEI号成功");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (aframe.contains("873EC11F")) {
                                            TcpUdpParam param = new TcpUdpParam();
                                            switch (aframe.substring(56, 66)) {
                                                case "2254435022":
                                                    //TCP连接
                                                    ((RadioButton) hydrantGPRSFragment.getView().findViewById(R.id.radioTcpServer)).setChecked(true);
                                                    ((RadioButton) hydrantGPRSFragment.getView().findViewById(R.id.radioUdpServer)).setChecked(false);
                                                    param.setMode("TCP");
                                                    break;
                                                case "2255445022":
                                                    //UDP连接
                                                    ((RadioButton) hydrantGPRSFragment.getView().findViewById(R.id.radioTcpServer)).setChecked(false);
                                                    ((RadioButton) hydrantGPRSFragment.getView().findViewById(R.id.radioUdpServer)).setChecked(true);
                                                    param.setMode("UDP");
                                                    break;
                                            }
                                            String strtcpip = aframe.substring(56);
                                            String[] arrtcpip = strtcpip.split("222C22");
                                            System.out.println(arrtcpip[1]);
                                            String[] arrip = arrtcpip[1].split("2E");
                                            StringBuilder sb = new StringBuilder();
                                            for (int i = 0; i < arrip[0].length() / 2; i++) {
                                                sb.append((Integer.parseInt(arrip[0].substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            param.setIp1(sb.toString());
                                            //SetGprsParamterActivity.editTextIP1.setText(sb.toString());
                                            sb = new StringBuilder();
                                            for (int i = 0; i < arrip[1].length() / 2; i++) {
                                                sb.append((Integer.parseInt(arrip[1].substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            param.setIp2(sb.toString());
                                            //SetGprsParamterActivity.editTextIP2.setText(sb.toString());
                                            sb = new StringBuilder();
                                            for (int i = 0; i < arrip[2].length() / 2; i++) {
                                                sb.append((Integer.parseInt(arrip[2].substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            param.setIp3(sb.toString());
                                            //SetGprsParamterActivity.editTextIP3.setText(sb.toString());
                                            sb = new StringBuilder();
                                            for (int i = 0; i < arrip[3].length() / 2; i++) {
                                                sb.append((Integer.parseInt(arrip[3].substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            param.setIp4(sb.toString());
                                            //SetGprsParamterActivity.editTextIP4.setText(sb.toString());
                                            String strport = arrtcpip[2];
                                            System.out.println(strport);
                                            int index = strport.indexOf("22");
                                            if (index % 2 == 1) {
                                                index++;
                                            }
                                            strport = strport.substring(0, index);
                                            System.out.println(strport);
                                            sb = new StringBuilder();
                                            for (int i = 0; i < strport.length() / 2; i++) {
                                                sb.append((Integer.parseInt(strport.substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            //System.out.println(sb.toString());
                                            param.setPort(sb.toString());
                                            //SetGprsParamterActivity.editTextPort.setText(sb.toString());
                                            //SetGprsParamterActivity.tvGprsParam.setText(param.toString());
                                            //SetGprsParamterActivity.tvGprsParam.setTextColor(SetGprsParamterActivity.colordarkgreen);
                                            TextView tvGprsParam = (TextView) hydrantGPRSFragment.getView().findViewById(R.id.tvGprsParam);
                                            tvGprsParam.setText(param.toString());
                                            tvGprsParam.setTextColor((ColorStateList) context.getResources().getColorStateList(R.color.darkgreen));
                                            CommonUtils.showToast(context, "读取网络配置成功");
                                            BluetoothToolsMainActivity.data = "";
                                        }
                                    }
                                }
                            }
                        }
                    } else if (currentActivity instanceof ValveSettingActivity) {
                        ValveSettingActivity valveSettingActivity = (ValveSettingActivity) currentActivity;
                        Fragment subFragment = valveSettingActivity.getSupportFragmentManager().getFragments().get(currentFragment);
                        if (subFragment instanceof ValveSettingFragment) {
                            ValveSettingFragment valveSettingFragment = (ValveSettingFragment) subFragment;
                            if (data == null || data.equals(""))
                                return;
                            if (BluetoothAnalysisUtils.analyzeData(data, protocol, ssumheat, mbus, parameterprotocol) == 1) {
                                ((TextView) valveSettingFragment.getView().findViewById(R.id.et_meterId)).setText(protocol.getMeterID());
                                if (protocol.getCloseTime() != null && protocol.getCloseTime().length() == 10) {
                                    ((EditText) valveSettingFragment.getView().findViewById(R.id.et_endDate)).setText(protocol.getCloseTime());
                                }
                                CommonUtils.showToast(context, "阀门状态：" + protocol.getValveStatus());
                                BluetoothToolsMainActivity.data = "";
                            }
                        }
                    } else if (currentActivity instanceof GPRSCollectorSettingActivity) {
                        GPRSCollectorSettingActivity gprsCollectorSettingActivity = (GPRSCollectorSettingActivity) currentActivity;
                        Fragment subfragment = gprsCollectorSettingActivity.getSupportFragmentManager().getFragments().get(currentFragment);
                        if (subfragment instanceof GprsNormalFragment) {
                            GprsNormalFragment gprsNormalFragment = (GprsNormalFragment) subfragment;
                            if (data == null || data.equals("")) {
                                return;
                            }
                            if (data.contains("03794500")) {
                                CommonUtils.showToast(context, "触发抄表成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("03794600")) {
                                CommonUtils.showToast(context, "触发上传成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("5345542044495354414E434520204F4B")) {
                                (gprsNormalFragment.getView().findViewById(R.id.btn_readCollectorParameters)).performClick();
                                CommonUtils.showToast(context, "修改采集器配置成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("44495354414E4345204552524F52")) {//DISTANCE ERROR
                                CommonUtils.showToast(context, "修改采集器配置失败");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("4D4F4449465920495031202020204F4B")) {
                                (gprsNormalFragment.getView().findViewById(R.id.btnReadComm)).performClick();
                                CommonUtils.showToast(context, "修改TCPUDP参数成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("534554204D20434C4F434B204F4B")) {
                                (gprsNormalFragment.getView().findViewById(R.id.btnReadDatetime)).performClick();
                                CommonUtils.showToast(context, "校正时钟成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            if (data.contains("494D4549204F4B20202020494D454920534554204F4B")) {
                                (gprsNormalFragment.getView().findViewById(R.id.btn_read_imei)).performClick();
                                CommonUtils.showToast(context, "修改IMEI成功");
                                BluetoothToolsMainActivity.data = "";
                                return;
                            }
                            System.out.println("data:" + data);
                            int start7B = data.indexOf("7B");
                            if (start7B != -1) {
                                String newdata = data.substring(start7B + 2);
                                System.out.println("newdata:" + newdata);
                                int end7B = newdata.lastIndexOf("7B");
                                if (end7B != -1) {
                                    int length = AnalysisUtils.HexS2ToInt(newdata.substring(4, 6));
                                    String aframe = newdata.substring(0, end7B);
                                    if (aframe.length() == 2 * (length - 2)) {
                                        System.out.println("aframe:" + aframe);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(aframe);
                                        if (builder.charAt(46) == '8'
                                                && builder.charAt(47) == '7'
                                                && builder.charAt(50) == 'C'
                                                && builder.charAt(51) == '1'
                                                && builder.charAt(52) == '4'
                                                && builder.charAt(53) == '2') {
                                            String imei = aframe.substring(4 + 2, 4 + 2 + 22);
                                            System.out.println(imei);
                                            StringBuilder sb = new StringBuilder();
                                            for (int i = 0; i < imei.length() / 2; i++) {
                                                sb.append((Integer.parseInt(imei.substring(i * 2, (i + 1) * 2))) - 30);
                                            }
                                            System.out.println(sb.toString());
                                            TextView tv_imei = (TextView) gprsNormalFragment.getView().findViewById(R.id.tv_imei);
                                            tv_imei.setText(sb.toString());
                                            tv_imei.setTextColor(context.getResources().getColorStateList(R.color.darkgreen));
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.charAt(46) == '8'
                                                && builder.charAt(47) == '4'
                                                && builder.charAt(50) == 'A'
                                                && builder.charAt(51) == '1'
                                                && builder.charAt(52) == '1'
                                                && builder.charAt(53) == '7') {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(aframe.substring(34 * 2, 34 * 2 + 2));
                                            sb.append(aframe.substring(33 * 2, 33 * 2 + 2));
                                            sb.append('-');
                                            sb.append(aframe.substring(32 * 2, 32 * 2 + 2));
                                            sb.append('-');
                                            sb.append(aframe.substring(31 * 2, 31 * 2 + 2));
                                            sb.append(' ');
                                            sb.append(aframe.substring(30 * 2, 30 * 2 + 2));
                                            sb.append(':');
                                            sb.append(aframe.substring(29 * 2, 29 * 2 + 2));
                                            sb.append(':');
                                            sb.append(aframe.substring(28 * 2, 28 * 2 + 2));
                                            TextView textViewDateTime = (TextView) gprsNormalFragment.getView().findViewById(R.id.textViewDateTime);
                                            textViewDateTime.setText(sb.toString());
                                            textViewDateTime.setTextColor(context.getResources().getColorStateList(R.color.darkgreen));
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.charAt(46) == '8'
                                                && builder.charAt(47) == '7'
                                                && builder.charAt(50) == 'C'
                                                && builder.charAt(51) == '1'
                                                && builder.charAt(52) == '1'
                                                && builder.charAt(53) == 'F') {
                                            int tcpindex = aframe.indexOf("2254435022");
                                            System.out.println(aframe);
                                            if (tcpindex != -1) {
                                                ((RadioButton) gprsNormalFragment.getView().findViewById(R.id.radioTcpServer)).setChecked(true);
                                                ((RadioButton) gprsNormalFragment.getView().findViewById(R.id.radioUdpServer)).setChecked(false);
                                                TcpUdpParam param = new TcpUdpParam();
                                                param.setMode("TCP");
                                                String strtcpip = aframe.substring(tcpindex);
                                                String[] arrtcpip = strtcpip.split("222C22");
                                                System.out.println(arrtcpip[1]);
                                                String[] arrip = arrtcpip[1].split("2E");
                                                StringBuilder sb = new StringBuilder();
                                                for (int i = 0; i < arrip[0].length() / 2; i++) {
                                                    sb.append((Integer.parseInt(arrip[0].substring(i * 2, (i + 1) * 2))) - 30);
                                                }
                                                param.setIp1(sb.toString());
                                                //SetGprsParamterActivity.editTextIP1.setText(sb.toString());
                                                sb = new StringBuilder();
                                                for (int i = 0; i < arrip[1].length() / 2; i++) {
                                                    sb.append((Integer.parseInt(arrip[1].substring(i * 2, (i + 1) * 2))) - 30);
                                                }
                                                param.setIp2(sb.toString());
                                                //SetGprsParamterActivity.editTextIP2.setText(sb.toString());
                                                sb = new StringBuilder();
                                                for (int i = 0; i < arrip[2].length() / 2; i++) {
                                                    sb.append((Integer.parseInt(arrip[2].substring(i * 2, (i + 1) * 2))) - 30);
                                                }
                                                param.setIp3(sb.toString());
                                                //SetGprsParamterActivity.editTextIP3.setText(sb.toString());
                                                sb = new StringBuilder();
                                                for (int i = 0; i < arrip[3].length() / 2; i++) {
                                                    sb.append((Integer.parseInt(arrip[3].substring(i * 2, (i + 1) * 2))) - 30);
                                                }
                                                param.setIp4(sb.toString());
                                                //SetGprsParamterActivity.editTextIP4.setText(sb.toString());
                                                String strport = arrtcpip[2];
                                                System.out.println(strport);
                                                int index = strport.indexOf("22");
                                                if (index % 2 == 1) {
                                                    index++;
                                                }
                                                strport = strport.substring(0, index);
                                                System.out.println(strport);
                                                sb = new StringBuilder();
                                                for (int i = 0; i < strport.length() / 2; i++) {
                                                    sb.append((Integer.parseInt(strport.substring(i * 2, (i + 1) * 2))) - 30);
                                                }
                                                //System.out.println(sb.toString());
                                                param.setPort(sb.toString());
                                                //SetGprsParamterActivity.editTextPort.setText(sb.toString());
                                                //SetGprsParamterActivity.tvGprsParam.setText(param.toString());
                                                //SetGprsParamterActivity.tvGprsParam.setTextColor(SetGprsParamterActivity.colordarkgreen);
                                                TextView tvGprsParam = (TextView) gprsNormalFragment.getView().findViewById(R.id.tvGprsParam);
                                                tvGprsParam.setText(param.toString());
                                                tvGprsParam.setTextColor((ColorStateList) context.getResources().getColorStateList(R.color.darkgreen));
                                                BluetoothToolsMainActivity.data = "";
                                            }
                                        } else if (builder.indexOf("950F867700") == 46) {
                                            Spinner spinnerupdatecycle = (Spinner) gprsNormalFragment.getView().findViewById(R.id.spinnerupdatecycle);
                                            Spinner spinnerupdatehour = (Spinner) gprsNormalFragment.getView().findViewById(R.id.spinnerupdatehour);
                                            Spinner spinnerpwrbattery = (Spinner) gprsNormalFragment.getView().findViewById(R.id.spinnerpwrbattery);
                                            Spinner spinnerpwrelectric = (Spinner) gprsNormalFragment.getView().findViewById(R.id.spinnerpwrelectric);
                                            int position = AnalysisUtils.HexS2ToInt(builder.substring(56, 58));//Integer.parseInt(builder.charAt(28))*16+Integer.parseInt(builder.charAt(29));
                                            for (int i = 0; i < GprsNormalFragment.hours1_int.length; i++) {
                                                if (GprsNormalFragment.hours1_int[i] == position) {
                                                    spinnerupdatecycle.setSelection(i, false);
                                                    break;
                                                }
                                            }
                                            position = AnalysisUtils.HexS2ToInt(builder.substring(58, 60));//Integer.parseInt(builder.charAt(28))*16+Integer.parseInt(builder.charAt(29));
                                            for (int i = 0; i < GprsNormalFragment.hours2_int.length; i++) {
                                                if (GprsNormalFragment.hours2_int[i] == position) {
                                                    spinnerupdatehour.setSelection(i, false);
                                                    break;
                                                }
                                            }
                                            position = AnalysisUtils.HexS2ToInt(builder.substring(60, 62));//Integer.parseInt(builder.charAt(28))*16+Integer.parseInt(builder.charAt(29));
                                            for (int i = 0; i < GprsNormalFragment.minutes1_int.length; i++) {
                                                if (GprsNormalFragment.minutes1_int[i] == position) {
                                                    spinnerpwrbattery.setSelection(i, false);
                                                    break;
                                                }
                                            }
                                            position = AnalysisUtils.HexS2ToInt(builder.substring(62, 64));//Integer.parseInt(builder.charAt(28))*16+Integer.parseInt(builder.charAt(29));
                                            for (int i = 0; i < GprsNormalFragment.minutes2_int.length; i++) {
                                                if (GprsNormalFragment.minutes2_int[i] == position) {
                                                    spinnerpwrelectric.setSelection(i, false);
                                                    break;
                                                }
                                            }
                                            CommonUtils.showToast(context, "收到抄表间隔参数");
                                            BluetoothToolsMainActivity.data = "";
                                        }
                                    }
                                }
                            }
                        } else if (subfragment instanceof GprsSettingFragment) {
                            GprsSettingFragment gprsSettingFragment = (GprsSettingFragment) subfragment;
                            if (data.contains("001111AB03794700")) {
                                CommonUtils.showToast(context, "指示灯点亮成功");
                                BluetoothToolsMainActivity.data = "";
                            } else if (data.contains("5345542044495354414E434520204F4B")) {
                                //解析为文本为：SET DISTANCE  OK
                                CommonUtils.showToast(context, "设置参数成功");
                                BluetoothToolsMainActivity.data = "";
                            }
                            System.out.println("data:" + data);
                            int start7B = data.indexOf("7B");
                            if (start7B != -1) {
                                String newdata = data.substring(start7B + 2);
                                System.out.println("newdata:" + newdata);
                                int end7B = newdata.lastIndexOf("7B");
                                if (end7B != -1) {
                                    int length = AnalysisUtils.HexS2ToInt(newdata.substring(4, 6));
                                    String aframe = newdata.substring(0, end7B);
                                    if (aframe.length() == 2 * (length - 2)) {
                                        System.out.println("aframe:" + aframe);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(aframe);
                                        if (builder.indexOf("950F887900") == 46) {
                                            //采集终端模式
                                            TextView get_collector_mode = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_collector_mode);
                                            if (builder.substring(66, 68).equals("01")) {
                                                get_collector_mode.setText("短连接");
                                            } else if (builder.substring(66, 68).equals("02")) {
                                                get_collector_mode.setText("长连接");
                                            }
                                            //分段模式使能
                                            TextView get_enabled_batch_mode = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_enabled_batch_mode);
                                            if (builder.substring(70, 72).equals("00")) {
                                                get_enabled_batch_mode.setText("不使能");
                                            } else if (builder.substring(70, 72).equals("01")) {
                                                get_enabled_batch_mode.setText("使能");
                                            }
                                            //上午起始时间点
                                            TextView get_startTime_morning = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_startTime_morning);
                                            get_startTime_morning.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(72, 74))) + "点");
                                            //上午结束时间点
                                            TextView get_endTime_morning = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_endTime_morning);
                                            get_endTime_morning.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(74, 76))) + "点");
                                            //下午起始时间点
                                            TextView get_startTime_afternoon = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_startTime_afternoon);
                                            get_startTime_afternoon.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(76, 78))) + "点");
                                            //下午结束时间点
                                            TextView get_endTime_afternoon = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_endTime_afternoon);
                                            get_endTime_afternoon.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(78, 80))) + "点");
                                            //短连接上传周期
                                            TextView get_cycle_upload_shortConnection = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_cycle_upload_shortConnection);
                                            get_cycle_upload_shortConnection.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(56, 58))) + "小时");
                                            //短连接时间点
                                            TextView get_time_shortConnection = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_time_shortConnection);
                                            get_time_shortConnection.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(58, 60))) + "点");
                                            //短连接抄表间隔
                                            TextView get_interval_readMeter_shortConnection = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_interval_readMeter_shortConnection);
                                            get_interval_readMeter_shortConnection.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(60, 62))) + "分钟");
                                            //外供电抄表间隔
                                            TextView get_interval_readMeter_external_power_supply = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_interval_readMeter_external_power_supply);
                                            get_interval_readMeter_external_power_supply.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(62, 64))) + "分钟");
                                            //电池供电常在线抄表间隔（长连接长模式）
                                            TextView get_interval_longMode_longConnection = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_interval_longMode_longConnection);
                                            get_interval_longMode_longConnection.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(64, 66))) + "分钟");
                                            //电池供电短在线状态间隔（长连接短模式）
                                            TextView get_interval_shortMode_longConnection = (TextView) gprsSettingFragment.getView().findViewById(R.id.get_interval_shortMode_longConnection);
                                            get_interval_shortMode_longConnection.setText("" + String.valueOf(AnalysisUtils.HexS2ToInt(builder.substring(68, 70))) + "分钟");
                                            CommonUtils.showToast(context, "读取参数成功");
                                            BluetoothToolsMainActivity.data = "";
                                        }
                                    }
                                }
                            }
                        } else if (subfragment instanceof GprsAddDeleteMeterFragment) {
                            GprsAddDeleteMeterFragment gprsAddDeleteMeterFragment = (GprsAddDeleteMeterFragment) subfragment;
                            System.out.println("data:" + data);
                            int start7B = data.indexOf("7B");
                            if (start7B != -1) {
                                String newdata = data.substring(start7B + 2);
                                System.out.println("newdata:" + newdata);
                                int end7B = newdata.lastIndexOf("7B");
                                if (end7B != -1) {
                                    int length = AnalysisUtils.HexS2ToInt(newdata.substring(4, 6));
                                    String aframe = newdata.substring(0, end7B);
                                    if (aframe.length() == 2 * (length - 2)) {
                                        System.out.println("aframe:" + aframe);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(aframe);
                                        if (builder.indexOf("BB077950") == 46) {
                                            String meterId = builder.substring(56, 64);
                                            CommonUtils.showToast(context, "表号" + meterId + "添加成功");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("7B077950") == 46) {
                                            String meterId = builder.substring(56, 64);
                                            CommonUtils.showToast(context, "表号" + meterId + "已存在");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("BB077951") == 46) {
                                            String meterId = builder.substring(56, 64);
                                            if (meterId.equals("FFFFFFFF")) {
                                                CommonUtils.showToast(context, "成功删除所有表号");
                                                BluetoothToolsMainActivity.data = "";
                                            } else {
                                                CommonUtils.showToast(context, "表号" + meterId + "删除成功");
                                                BluetoothToolsMainActivity.data = "";
                                            }
                                        } else if (builder.indexOf("7B077951") == 46) {
                                            String meterId = builder.substring(56, 64);
                                            CommonUtils.showToast(context, "表号" + meterId + "不存在，删除失败");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("BB077952") == 46) {
                                            int number = AnalysisUtils.HexS2ToInt(builder.substring(54, 56)) + 1;
                                            String meterId = builder.substring(56, 64);
                                            String text = "序号：" + (number < 10 ? "0" + number : number) + "    " + "表号：" + meterId + "    " + "状态：有效" + "\n";
                                            TextView tv_allMeter = (TextView) gprsAddDeleteMeterFragment.getView().findViewById(R.id.tv_allMeter);
                                            refreshSocketText(text, tv_allMeter);
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("7B077952") == 46) {
                                            int number = AnalysisUtils.HexS2ToInt(builder.substring(54, 56)) + 1;
                                            String meterId = builder.substring(56, 64);
                                            String text = "序号：" + (number < 10 ? "0" + number : number) + "    " + "表号：" + meterId + "    " + "状态：无效" + "\n";
                                            TextView tv_allMeter = (TextView) gprsAddDeleteMeterFragment.getView().findViewById(R.id.tv_allMeter);
                                            refreshSocketText(text, tv_allMeter);
                                            BluetoothToolsMainActivity.data = "";
                                        }
                                    }
                                }
                            }
                        } else if (subfragment instanceof GprsSetPressureFragment) {
                            GprsSetPressureFragment gprsSetPressureFragment = (GprsSetPressureFragment) subfragment;
                            System.out.println("data:" + data);
                            int start7B = data.indexOf("7B");
                            if (start7B != -1) {
                                String newdata = data.substring(start7B + 2);
                                System.out.println("newdata:" + newdata);
                                int end7B = newdata.lastIndexOf("7B");
                                if (end7B != -1) {
                                    int length = AnalysisUtils.HexS2ToInt(newdata.substring(4, 6));
                                    String aframe = newdata.substring(0, end7B);
                                    if (aframe.length() == 2 * (length - 2)) {
                                        System.out.println("aframe:" + aframe);
                                        StringBuilder builder = new StringBuilder();
                                        builder.append(aframe);
                                        if (builder.indexOf("BB078054") == 46) {
                                            CommonUtils.showToast(context, "标定成功");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("7B078054") == 46) {
                                            CommonUtils.showToast(context, "标定失败");
                                            BluetoothToolsMainActivity.data = "";
                                        } else if (builder.indexOf("BB0E8055") == 46) {
                                            //零点AD采样值
                                            int sampling_value_zero_point = AnalysisUtils.HexS2ToInt(builder.substring(54, 62));
                                            TextView tv_sampling_value_zero_point = (TextView) gprsSetPressureFragment.getView().findViewById(R.id.tv_sampling_value_zero_point);
                                            tv_sampling_value_zero_point.setText(String.valueOf(sampling_value_zero_point));
                                            //常用点AD采样值
                                            int sampling_value_common_point = AnalysisUtils.HexS2ToInt(builder.substring(70, 78));
                                            TextView tv_sampling_value_common_point = (TextView) gprsSetPressureFragment.getView().findViewById(R.id.tv_sampling_value_common_point);
                                            tv_sampling_value_common_point.setText(String.valueOf(sampling_value_common_point));
                                            //当前常用点
                                            float current_common_point = AnalysisUtils.HexS8ToInt(builder.substring(62, 70)) / 100000f;
                                            TextView tv_current_common_point = (TextView) gprsSetPressureFragment.getView().findViewById(R.id.tv_current_common_point);
                                            tv_current_common_point.setText("" + new DecimalFormat("##0.00").format(current_common_point) + "bar");
                                            CommonUtils.showToast(context, "读取压力标定成功");
                                            BluetoothToolsMainActivity.data = "";
                                        }
                                    }
                                }
                            }
                        }
                    } else if (currentActivity instanceof LoRaSettingActivity) {
                        LoRaSettingActivity loRaSettingActivity = (LoRaSettingActivity) currentActivity;
                        if (data == null || data.equals("")) {
                            return;
                        }
                        if (data.contains("880B901E")) {
                            try {
                                String wirelessState = data.substring(26, 28);
                                String power = data.substring(28, 30);
                                int frequency_point_1 = AnalysisUtils.HexS2ToInt(data.substring(30, 32));
                                int frequency_point_2 = AnalysisUtils.HexS2ToInt(data.substring(32, 34));
                                int frequency_point_3 = AnalysisUtils.HexS2ToInt(data.substring(34, 36));
                                String datum_point = data.substring(36, 44);
                                ((TextView) loRaSettingActivity.findViewById(R.id.tv_wireless_state)).setText(wirelessState.equals("00") ? "停用" : "启用");
                                ((TextView) loRaSettingActivity.findViewById(R.id.tv_power)).setText(power);
                                ((TextView) loRaSettingActivity.findViewById(R.id.tv_channel)).setText(frequency_point_1 + "-" + frequency_point_2 + "-" + frequency_point_3);
                                ((TextView) loRaSettingActivity.findViewById(R.id.tv_datum_point)).setText(datum_point);
                                CommonUtils.showToast(context, "读取参数成功");
                                BluetoothToolsMainActivity.data = "";
                            } catch (Exception e) {
                                e.printStackTrace();
                                CommonUtils.showToast(context, "读取参数失败");
                                BluetoothToolsMainActivity.data = "";
                            }
                        } else if (data.contains("6854111111110011118503501A011516")) {
                            CommonUtils.showToast(context, "启用无线成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (data.contains("6854111111110011118503501A001416")) {
                            CommonUtils.showToast(context, "停用无线成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (data.contains("6854111111110011118705701C")) {
                            CommonUtils.showToast(context, "修改信道成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (data.contains("6854111111110011118706801D")) {
                            CommonUtils.showToast(context, "修改频率基准点成功");
                            BluetoothToolsMainActivity.data = "";
                        } else if (data.contains("6854111111110011118603601B")) {
                            CommonUtils.showToast(context, "修改功率成功");
                            BluetoothToolsMainActivity.data = "";
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //16进制字符串转String
    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    //报警状态1
    private static void showState1(HydrantDataFragment hydrantDataFragment, String state1) {
        //换成16进制整型数
        int m = Integer.valueOf(state1, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_water_pressure)).setText(getRedText("水压低", "水压低"));
        } else if ((m & 0x01) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_water_pressure)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00000010运算
        if ((m & 0x02) == 0x02) {
            //保留报警位
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_water_pressure_mutation)).setText(getRedText("水压突变", "水压突变"));
        } else if ((m & 0x02) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_water_pressure_mutation)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_slant)).setText(getRedText("倾斜", "倾斜"));
        } else if ((m & 0x04) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_slant)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00001000运算
        if ((m & 0x08) == 0x08) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_vibration)).setText(getRedText("震动", "震动"));
        } else if ((m & 0x08) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_vibration)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00010000运算
        if ((m & 0x10) == 0x10) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_cover)).setText(getRedText("盖子被拆", "盖子被拆"));
        } else if ((m & 0x10) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_cover)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00100000运算
        if ((m & 0x20) == 0x20) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_battery)).setText(getRedText("电池被拆", "电池被拆"));
        } else if ((m & 0x20) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_open_battery)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制01000000运算
        if ((m & 0x40) == 0x40) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_temperature)).setText(getRedText("温度低", "温度低"));
        } else if ((m & 0x40) == 0x00) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_low_temperature)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制10000000运算
        if ((m & 0x80) == 0x80) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_valve_status)).setText(getGreenText("关", "关"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_valve_status)).setText(getRedText("开", "开"));
        }
    }

    //报警状态2
    private static void showState2(HydrantDataFragment hydrantDataFragment, String state2) {
        //转换成16进制整型数
        int m = Integer.valueOf(state2, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_temperature_measurement_short_circuit)).setText(getRedText("短路", "短路"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_temperature_measurement_short_circuit)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00000010运算
        if ((m & 0x02) == 0x02) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_temperature_measurement_open_circuit)).setText(getRedText("断路", "断路"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_temperature_measurement_open_circuit)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_no_water)).setText(getRedText("无水", "无水"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_no_water)).setText(getGreenText("有水", "有水"));
        }
        //与运算，和二进制00001000运算
        if ((m & 0x08) == 0x08) {
        } else {
        }
        //与运算，和二进制00010000运算
        if ((m & 0x10) == 0x10) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_outrange)).setText(getRedText("超量程", "超量程"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_outrange)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制00100000运算
        if ((m & 0x20) == 0x20) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_battery_low)).setText(getRedText("电压低", "电压低"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_battery_low)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制01000000运算
        if ((m & 0x40) == 0x40) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_water_leakage)).setText(getRedText("漏水", "漏水"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_water_leakage)).setText(getGreenText("正常", "正常"));
        }
        //与运算，和二进制10000000运算
        if ((m & 0x80) == 0x80) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_reverse)).setText(getRedText("异常", "异常"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_reverse)).setText(getGreenText("正常", "正常"));
        }
    }

    //报警状态3
    private static void showState3(HydrantDataFragment hydrantDataFragment, String state3) {
        //转换成16进制整型数
        int m = Integer.valueOf(state3, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_not_close_valve)).setText(getRedText("未关阀", "未关阀"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_not_close_valve)).setText(getGreenText("已关阀", "已关阀"));
        }
        if ((m & 0x02) == 0x02) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_not_close_tightly_valve)).setText(getRedText("未关紧", "未关紧"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_not_close_tightly_valve)).setText(getGreenText("已关紧", "已关紧"));
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser)).setImageResource(R.drawable.circle_error);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser2)).setImageResource(R.drawable.circle_error);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_water_leakage_noUser)).setText(getRedText("漏水", "漏水"));
        } else {
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser)).setImageResource(R.drawable.circle_i);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser2)).setImageResource(R.drawable.circle_i);
            ((TextView) hydrantDataFragment.getView().findViewById(R.id.tv_water_leakage_noUser)).setText(getGreenText("未漏水", "未漏水"));
        }
//        //与运算，和二进制00001000运算
//        if ((m & 0x08) == 0x08) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制00010000运算
//        if ((m & 0x10) == 0x10) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制00100000运算
//        if ((m & 0x20) == 0x20) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制01000000运算
//        if ((m & 0x40) == 0x40) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        } else {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制10000000运算
//        if ((m & 0x80) == 0x80) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        } else {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
    }


    //报警使能1
    private static void showEnabled1(HydrantDataFragment hydrantDataFragment, String state1) {
        //截取最后两位数值并转换成16进制整型数
        int m = Integer.valueOf(state1, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            SharedPreferencesUtils.getInstance().saveData("low_water_pressure_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x01) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("low_water_pressure_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00000010运算
        if ((m & 0x02) == 0x02) {
            SharedPreferencesUtils.getInstance().saveData("low_water_pressure_mutation_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x02) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("low_water_pressure_mutation_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_water_pressure_mutation_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            SharedPreferencesUtils.getInstance().saveData("slant_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x04) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("slant_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_slant_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00001000运算
        if ((m & 0x08) == 0x08) {
            SharedPreferencesUtils.getInstance().saveData("vibration_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x08) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("vibration_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_vibration_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00010000运算
        if ((m & 0x10) == 0x10) {
            SharedPreferencesUtils.getInstance().saveData("open_cover_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x10) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("open_cover_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_cover_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00100000运算
        if ((m & 0x20) == 0x20) {
            SharedPreferencesUtils.getInstance().saveData("open_battery_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x20) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("open_battery_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_open_battery_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制01000000运算
        if ((m & 0x40) == 0x40) {
            SharedPreferencesUtils.getInstance().saveData("low_temperature_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature_enabled)).setImageResource(R.drawable.circle_i);
        } else if ((m & 0x40) == 0x00) {
            SharedPreferencesUtils.getInstance().saveData("low_temperature_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_low_temperature_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制10000000运算
        if ((m & 0x80) == 0x80) {
            SharedPreferencesUtils.getInstance().saveData("valve_status_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("valve_status_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_valve_status_enabled)).setImageResource(R.drawable.circle_error);
        }
    }

    //报警使能2
    private static void showEnabled2(HydrantDataFragment hydrantDataFragment, String state2) {
        //截取最后两位数值并转换成16进制整型数
        int m = Integer.valueOf(state2, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            SharedPreferencesUtils.getInstance().saveData("temperature_measurement_short_circuit_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("temperature_measurement_short_circuit_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_short_circuit_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00000010运算
        if ((m & 0x02) == 0x02) {
            SharedPreferencesUtils.getInstance().saveData("temperature_measurement_open_circuit_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("temperature_measurement_open_circuit_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_temperature_measurement_open_circuit_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            SharedPreferencesUtils.getInstance().saveData("no_water_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("no_water_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_no_water_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00001000运算
        if ((m & 0x08) == 0x08) {
            SharedPreferencesUtils.getInstance().saveData("PCB_breakdown_enabled", true);
        } else {
            SharedPreferencesUtils.getInstance().saveData("PCB_breakdown_enabled", false);
        }
        //与运算，和二进制00010000运算
        if ((m & 0x10) == 0x10) {
            SharedPreferencesUtils.getInstance().saveData("outrange_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("outrange_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_outrange_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00100000运算
        if ((m & 0x20) == 0x20) {
            SharedPreferencesUtils.getInstance().saveData("battery_low_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("battery_low_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_battery_low_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制01000000运算
        if ((m & 0x40) == 0x40) {
            SharedPreferencesUtils.getInstance().saveData("water_leakage_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("water_leakage_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制10000000运算
        if ((m & 0x80) == 0x80) {
            SharedPreferencesUtils.getInstance().saveData("reverse_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("reverse_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_reverse_enabled)).setImageResource(R.drawable.circle_error);
        }
    }

    //报警状态3
    private static void showEnabled3(HydrantDataFragment hydrantDataFragment, String state3) {
        //截取最后两位数值并转换成16进制整型数
        int m = Integer.valueOf(state3, 16);
        //与运算，和二进制00000001运算
        if ((m & 0x01) == 0x01) {
            SharedPreferencesUtils.getInstance().saveData("not_close_valve_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("not_close_valve_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_valve_enabled)).setImageResource(R.drawable.circle_error);
        }
        if ((m & 0x02) == 0x02) {
            SharedPreferencesUtils.getInstance().saveData("not_close_tightly_valve_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("not_close_tightly_valve_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_not_close_tightly_valve_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制00000100运算
        if ((m & 0x04) == 0x04) {
            SharedPreferencesUtils.getInstance().saveData("water_leakage_noUser_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("water_leakage_noUser_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_water_leakage_noUser_enabled)).setImageResource(R.drawable.circle_error);
        }
//        //与运算，和二进制00001000运算
//        if ((m & 0x08) == 0x08) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制00010000运算
//        if ((m & 0x10) == 0x10) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
//        //与运算，和二进制00100000运算
//        if ((m & 0x20) == 0x20) {
//            refreshSocketText(getCurrentTime() + "消火栓" + meterId + "保留" + "\n");
//        }
        //与运算，和二进制01000000运算
        if ((m & 0x40) == 0x40) {
            SharedPreferencesUtils.getInstance().saveData("gprs_reconnected_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_gprs_reconnected_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("gprs_reconnected_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_gprs_reconnected_enabled)).setImageResource(R.drawable.circle_error);
        }
        //与运算，和二进制10000000运算
        if ((m & 0x80) == 0x80) {
            SharedPreferencesUtils.getInstance().saveData("getGPS_enabled", true);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_getGPS_enabled)).setImageResource(R.drawable.circle_i);
        } else {
            SharedPreferencesUtils.getInstance().saveData("getGPS_enabled", false);
            ((ImageView) hydrantDataFragment.getView().findViewById(R.id.iv_getGPS_enabled)).setImageResource(R.drawable.circle_error);
        }
    }

    //获取红色文字的方法
    private static SpannableStringBuilder getRedText(String string, String change) {
        int fstart = string.indexOf(change);
        int fend = fstart + change.length();
        SpannableStringBuilder style = new SpannableStringBuilder(string);
        style.setSpan(new ForegroundColorSpan(Color.RED), fstart, fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return style;
    }

    //获取绿色文字的方法
    private static SpannableStringBuilder getGreenText(String string, String change) {
        int fstart = string.indexOf(change);
        int fend = fstart + change.length();
        SpannableStringBuilder style = new SpannableStringBuilder(string);
        style.setSpan(new ForegroundColorSpan(Color.parseColor("#00A000")), fstart, fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return style;
    }

    //将转换后的string值根据参数转变成对应的值
    private static String format(String message, double unit) {
        //将字符串转成double类型，并乘以单位
        double value = Double.valueOf(message) * unit;
        BigDecimal bigDecimal = new BigDecimal(value);
        return getPrettyNumber(String.valueOf(bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
    }

    //去除double首尾无效的0（数值保持不变）
    public static String getPrettyNumber(String number) {
        return BigDecimal.valueOf(Double.parseDouble(number)).stripTrailingZeros().toPlainString();
    }

    //根据协议，将截取的数每两位截取再倒序拼接
    private static String changeCode(String message) {
        String result = "";
        //获取message的长度
        int length = message.length();
        for (int i = length - 2; i >= 0; i = i - 2) {
            result += message.substring(i, i + 2);
        }
        return result;
    }

    //刷新文本框(文字行数多余文本框高度时自动滚动，始终保证最后一行显示)
    private static void refreshSocketText(String msg, TextView textView) {
        textView.append(msg);
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    //发送到蓝牙工具
    private static void writeToBluetoothDevice() {
        try {
            OutputStream os = bluetoothSocket.getOutputStream();    //蓝牙连接输出流
            byte[] bos = new byte[tx.length() / 2];
            for (int i = 0; i < (tx.length() / 2); i++) {           //手机中换行为0a,将其改为0d 0a后再发送
                bos[i] = (byte) HexS2ToInt(tx.substring(2 * i, 2 * i + 2));
            }
            os.write(bos);
            //发送显示消息，进行显示刷新
            //myHandler.sendMessage(myHandler.obtainMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //发送数据功能函数
    static int HexS1ToInt(char ch) {
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException(String.valueOf(ch));
    }

    static int HexS2ToInt(String S) {
        int r;
        char a[] = S.toCharArray();
        r = HexS1ToInt(a[0]) * 16 + HexS1ToInt(a[1]);
        return r;
    }

    /**
     * 将EditText光标置于末尾
     *
     * @param charSequence EditText的内容
     */
    private static void setCharSequence(CharSequence charSequence) {
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceReceiver != null) {
            try {
                context.unregisterReceiver(deviceReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (bluetoothSocket != null) {
            //关闭连接socket
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //如果进入页面时蓝牙是未开启的，则退出本页面的时候关闭蓝牙
        if (!bluetoothIsOpened) {
            if (bluetoothAdapter != null) {
                try {
                    bluetoothAdapter.disable();  //关闭蓝牙服务
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (bluetoothDevice != null) {
            bluetoothDevice = null;
        }
    }

    //点击两次返回退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > Constant.EXIT_DOUBLE_CLICK_TIME) {
            CommonUtils.showToast(getApplicationContext(), getString(R.string.click_again_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
