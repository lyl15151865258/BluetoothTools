package cn.njmeter.bluetooth.fragment.collector;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.bean.TcpUdpParam;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.MathUtils;
import cn.njmeter.bluetooth.utils.RegexUtils;

public class GprsNormalFragment extends BaseFragment implements View.OnClickListener {

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
    @BindView(R.id.tvGprsParam)
    TextView tvGprsParam;
    @BindView(R.id.radioTcpServer)
    RadioButton radioTcpServer;
    @BindView(R.id.radioUdpServer)
    RadioButton radioUdpServer;
    @BindView(R.id.et_ip)
    EditText et_ip;
    @BindView(R.id.editTextPort)
    EditText editTextPort;
    @BindView(R.id.btnSetComm)
    Button btnSetComm;
    @BindView(R.id.btnReadComm)
    Button btnReadComm;
    @BindView(R.id.btnGprsDefault)
    Button btnGprsDefault;
    @BindView(R.id.btnDomainNameDefault)
    Button btnDomainNameDefault;


    @BindView(R.id.spinnerupdatecycle)
    Spinner spinnerupdatecycle;
    @BindView(R.id.spinnerupdatehour)
    Spinner spinnerupdatehour;
    @BindView(R.id.spinnerpwrbattery)
    Spinner spinnerpwrbattery;
    @BindView(R.id.spinnerpwrelectric)
    Spinner spinnerpwrelectric;
    @BindView(R.id.btn_read_meter)
    Button btnUserReading;
    @BindView(R.id.btn_commit_data)
    Button btnUserUpdate;
    @BindView(R.id.btnReadReadingParam)
    Button btnReadReadingParam;
    @BindView(R.id.btnSetReadingParam)
    Button btnSetReadingParam;


    @BindView(R.id.btnReadDatetime)
    Button btnReadDatetime;

    @BindView(R.id.textViewDateTime)
    TextView textViewDateTime;

    @BindView(R.id.Buttonadjusttime)
    Button Buttonadjusttime;
    @BindView(R.id.CheckBoxsyn)
    CheckBox CheckBoxsyn;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private String[] hours1_string = {"2小时", "3小时", "4小时", "6小时", "8小时", "12小时"};
    public static int[] hours1_int = {2, 3, 4, 6, 8, 12};

    private String[] hours2_string = {"0点", "1点", "2点", "3点", "4点", "5点", "6点", "7点", "8点", "9点", "10点",
            "11点", "12点", "13点", "14点", "15点", "16点", "17点", "18点", "19点", "20点", "21点", "22点", "23点"};
    public static int[] hours2_int = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};

    private String[] minutes1_string = {"10分钟", "15分钟", "20分钟", "30分钟", "60分钟"};
    public static int[] minutes1_int = {10, 15, 20, 30, 60};

    private String[] minutes2_string = {"2分钟", "3分钟", "4分钟", "5分钟", "6分钟", "7分钟", "8分钟", "9分钟", "10分钟"};
    public static int[] minutes2_int = {2, 3, 4, 5, 6, 7, 8, 9, 10};

    private ArrayAdapter<String> adapter_updateCycle;
    private ArrayAdapter<String> adapter_updateHour;
    private ArrayAdapter<String> adapter_pwrBattery;
    private ArrayAdapter<String> adapter_pwrElectric;


    //private static String strDeviceTypeCode = "20";
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private int mSecond;
    private static final int SHOW_DATAPICK = 0;
    private static final int DATE_DIALOG_ID = 1;
    private static final int SHOW_TIMEPICK = 2;
    private static final int TIME_DIALOG_ID = 3;
    private static final int SHOW_CLOSEDATAPICK = 4;
    private static final int CLOSEDATE_DIALOG_ID = 5;
    private Timer datetimetimer = new Timer();
    static ColorStateList colorgray;
    static ColorStateList colordarkgreen;
    static boolean autoRefreshDateTime;

    Resources resource;

    boolean isSetParamLegal;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_gprs_normal_layout, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
        autoRefreshDateTime = false;
        isSetParamLegal = true;
        resource = context.getResources();
        colorgray = resource.getColorStateList(R.color.gray);
        colordarkgreen = resource.getColorStateList(R.color.darkgreen);
        adapter_updateCycle = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours1_string);
        adapter_updateHour = new ArrayAdapter<>(context, R.layout.spinner_layout_head, hours2_string);
        adapter_pwrBattery = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes1_string);
        adapter_pwrElectric = new ArrayAdapter<>(context, R.layout.spinner_layout_head, minutes2_string);

        //FrameLayout generalActionBar = (FrameLayout) view.findViewById(R.id.general_actionbar);
        //TextView tvTitle = (TextView) generalActionBar.findViewById(R.id.tv_explore_scan);
        //tvTitle.setText(R.string.main_tab_name_gprs);
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
                tv_imei.setTextColor(colorgray);
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
                    System.out.println(sb.toString());
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(sb.toString());
                } else {
                    showToast("请输入11位IMEI号");
                }

            }
        });
        btn_read_imei.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                tv_imei.setTextColor(colorgray);
                tv_imei.setText("");
                String tx = "7B89002030303030303030303030306848111111110011110703C1420023167B";
                BluetoothToolsMainActivity.data = "";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        btnGprsDefault.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                et_ip.setText("58.240.47.50");
            }
        });
        btnDomainNameDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                et_ip.setText("www.metter.cn");
            }
        });
        btnSetComm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String ip = et_ip.getText().toString();
                String port = editTextPort.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    CommonUtils.showToast(context, "请输入IP地址或域名");
                    return;
                }
                if (TextUtils.isEmpty(port)) {
                    CommonUtils.showToast(context, "请输入端口号");
                    return;
                }
                tvGprsParam.setTextColor(colorgray);
                //7B89003D30303030303030303030306848111111110011110703C11E0122544350222C2235382E3234302E34372E3530222C2235303034220D2361167B
                StringBuilder sb = new StringBuilder();
                sb.append("7B89003D30303030303030303030306848111111110011110703C11E01");
                StringBuilder param = new StringBuilder();
                if (radioTcpServer.isChecked()) {
                    param.append("\"TCP\"");
                } else {
                    param.append("\"UDP\"");
                }
                param.append(",");
                param.append("\"");
                param.append(ip);
                param.append("\",\"");
                param.append(port);
                param.append("\"");
                if (!RegexUtils.checkPort(port)) {
                    CommonUtils.showToast(context, "端口号输入错误");
                    return;
                }
                if (RegexUtils.checkIpAddress(ip) || RegexUtils.checkDomainName(ip)) {
                    sb.append(MathUtils.getHexStr(param.toString()));
                    sb.append("0D23");
                    String checkStr = sb.substring(sb.indexOf("68"));
                    int checksum = getCheckSum(checkStr);
                    String cs = Integer.toHexString(checksum);
                    sb.append(cs.substring(cs.length() - 2));
                    sb.append("167B");
                    sb.replace(6, 8, sb.length() / 2 < 17 ? ("0" + Integer.toHexString(sb.length() / 2)) : (Integer.toHexString(sb.length() / 2)));
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(sb.toString());
                } else {
                    CommonUtils.showToast(context, "IP地址或域名输入错误");
                }
            }
        });
        btnReadComm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //tv_meterid.setText("");
                //BluetoothPrinterMainActivity.data = "";
                tvGprsParam.setTextColor(colorgray);
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002130303030303030303030306848111111110011110703C12F000111167B";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        btnReadDatetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                textViewDateTime.setTextColor(colorgray);
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002030303030303030303030306848111111110011110403A11700D5167B";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });
        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = SHOW_DATAPICK;
                dateandtimeHandler.sendMessage(msg);
            }
        });
        tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = SHOW_TIMEPICK;
                dateandtimeHandler.sendMessage(msg);
            }
        });
        Buttonadjusttime.setOnClickListener(new View.OnClickListener() {
            // @Override
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                String x, rx;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdf1 = new SimpleDateFormat("ssmmHHddMMyyyy");
                x = tv_date.getText().toString() + " " + tv_time.getText().toString();
                try {
                    Date xd = sdf.parse(x);
                    rx = sdf1.format(xd);
                    rx = rx.substring(0, 10) + rx.substring(12, 14) + rx.substring(10, 12);
                } catch (Exception e) {
                    CommonUtils.showToast(context, "请正确输入当前时间");
                    return;
                }
                int checksum = 473;
                StringBuilder sb = new StringBuilder();
                sb.append("7B8900273030303030303030303030684811111111001111040AA01500");
                sb.append(rx);
                //int temp=
                checksum += Integer.parseInt(rx.substring(0, 1)) * 16 + Integer.parseInt(rx.substring(1, 2));
                checksum += Integer.parseInt(rx.substring(2, 3)) * 16 + Integer.parseInt(rx.substring(3, 4));
                checksum += Integer.parseInt(rx.substring(4, 5)) * 16 + Integer.parseInt(rx.substring(5, 6));
                checksum += Integer.parseInt(rx.substring(6, 7)) * 16 + Integer.parseInt(rx.substring(7, 8));
                checksum += Integer.parseInt(rx.substring(8, 9)) * 16 + Integer.parseInt(rx.substring(9, 10));
                checksum += Integer.parseInt(rx.substring(10, 11)) * 16 + Integer.parseInt(rx.substring(11, 12));
                checksum += Integer.parseInt(rx.substring(12, 13)) * 16 + Integer.parseInt(rx.substring(13, 14));
                String cs = Integer.toHexString(checksum);
                sb.append(cs.substring(cs.length() - 2));
                sb.append("167B");

                BluetoothToolsMainActivity.data = "";
                BluetoothToolsMainActivity.writeData(sb.toString());
            }
        });

        spinnerupdatecycle.setAdapter(adapter_updateCycle);
        spinnerupdatecycle.setSelection(5, false);
        spinnerupdatehour.setAdapter(adapter_updateHour);
        spinnerupdatehour.setSelection(6, false);
        spinnerpwrbattery.setAdapter(adapter_pwrBattery);
        spinnerpwrbattery.setSelection(2, false);
        spinnerpwrelectric.setAdapter(adapter_pwrElectric);
        spinnerpwrelectric.setSelection(0, false);

        spinnerupdatecycle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                         @Override
                                                         public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                                             switch (arg2) {
                                                                 case 4:
                                                                     if (spinnerpwrbattery.getSelectedItemPosition() == 0) {
                                                                         isSetParamLegal = false;
                                                                         CommonUtils.showToast(context, "您选择的抄表周期与上传周期冲突");
                                                                     } else {
                                                                         isSetParamLegal = true;
                                                                     }
                                                                     break;
                                                                 case 5:
                                                                     if (spinnerpwrbattery.getSelectedItemPosition() == 0
                                                                             || spinnerpwrbattery.getSelectedItemPosition() == 1) {
                                                                         isSetParamLegal = false;
                                                                         CommonUtils.showToast(context, "您选择的抄表周期与上传周期冲突");
                                                                     } else {
                                                                         isSetParamLegal = true;
                                                                     }
                                                                     break;
                                                                 default:
                                                                     isSetParamLegal = true;
                                                                     break;
                                                             }
                                                         }

                                                         @Override
                                                         public void onNothingSelected(AdapterView<?> parent) {

                                                         }
                                                     }
        );
        spinnerpwrbattery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                switch (arg2) {
                    case 0:
                        if (spinnerupdatecycle.getSelectedItemPosition() >= 4) {
                            isSetParamLegal = false;
                            CommonUtils.showToast(context, "您选择的抄表周期与上传周期冲突");
                        } else {
                            isSetParamLegal = true;
                        }
                        break;
                    case 1:
                        if (spinnerupdatecycle.getSelectedItemPosition() >= 5) {
                            isSetParamLegal = false;
                            CommonUtils.showToast(context, "您选择的抄表周期与上传周期冲突");
                        } else {
                            isSetParamLegal = true;
                        }
                        break;
                    default:
                        isSetParamLegal = true;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        btnUserReading.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002030303030303030303030306848111111110011112B0379450002167B";
                BluetoothToolsMainActivity.writeData(tx);

            }
        });
        btnUserUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BluetoothToolsMainActivity.data = "";
                String tx = "7B89002030303030303030303030306848111111110011112B0379460003167B";
                BluetoothToolsMainActivity.writeData(tx);

            }
        });
        btnReadReadingParam.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BluetoothToolsMainActivity.data = "";
                String tx = "7B890021303030303030303030303068481111111100111115048677004470167B";
                BluetoothToolsMainActivity.writeData(tx);

            }
        });
        btnSetReadingParam.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isSetParamLegal) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("7B89002C3030303030303030303030");

                    StringBuilder sb = new StringBuilder();
                    sb.append("684811111111001111150F857600");
                    int updatecycle = spinnerupdatecycle.getSelectedItemPosition();
                    if (hours1_int[updatecycle] < 16) {
                        sb.append("0");
                    }
                    sb.append(Integer.toHexString(hours1_int[updatecycle]));
                    int updatehour = spinnerupdatehour.getSelectedItemPosition();
                    if (hours2_int[updatehour] < 16) {
                        sb.append("0");
                    }
                    sb.append(Integer.toHexString(hours2_int[updatehour]));
                    int pwrbattery = spinnerpwrbattery.getSelectedItemPosition();
                    if (minutes1_int[pwrbattery] < 16) {
                        sb.append("0");
                    }
                    sb.append(Integer.toHexString(minutes1_int[pwrbattery]));
                    int pwrelectric = spinnerpwrelectric.getSelectedItemPosition();
                    if (minutes2_int[pwrelectric] < 16) {
                        sb.append("0");
                    }
                    sb.append(Integer.toHexString(minutes2_int[pwrelectric]));
                    sb.append("0000000000000000");
                    int checksum = getCheckSum(sb.toString());
                    String cs = Integer.toHexString(checksum);
                    sb.append(cs.substring(cs.length() - 2));
                    sb.append("167B");
                    stringBuilder.append(sb.toString());
                    //Toast.makeText(getApplicationContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                    BluetoothToolsMainActivity.data = "";
                    String tx = stringBuilder.toString().toUpperCase().replace(" ", "");
                    BluetoothToolsMainActivity.writeData(tx);
                } else {
                    CommonUtils.showToast(context, "您选择的抄表周期与上传周期冲突");
                }
            }
        });
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (CheckBoxsyn.isChecked()) {
                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);
                    mSecond = c.get(Calendar.SECOND);
                    tv_date.setText(new StringBuilder().append(mYear).append("-")
                            .append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1))
                            .append("-")
                            .append((mDay < 10) ? "0" + mDay : mDay));
                    tv_time.setText(new StringBuilder().append((mHour < 10) ? "0" + mHour : mHour)
                            .append(":")
                            .append((mMinute < 10) ? "0" + mMinute : mMinute)
                            .append(":")
                            .append((mSecond < 10) ? "0" + mSecond : mSecond));
                }
            }
        };
        datetimetimer = new Timer();
        datetimetimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }, 0, 1000);

    }

    public int getCheckSum(String param) {
        StringBuilder sb = new StringBuilder();
        int res = 0;
        for (int i = 0; i < param.length() / 2; i++) {
            res += AnalysisUtils.HexS2ToInt(param.substring(i * 2, i * 2 + 2));
        }
        return res;
    }

    private void updateDateTimeDisplay() {
        CheckBoxsyn.setChecked(false);
        tv_date.setText(new StringBuilder().append(mYear).append("-")
                .append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)).append("-")
                .append((mDay < 10) ? "0" + mDay : mDay));
        tv_time.setText(new StringBuilder().append((mHour < 10) ? "0" + mHour : mHour)
                .append(":")
                .append((mMinute < 10) ? "0" + mMinute : mMinute)
                .append(":")
                .append((mSecond < 10) ? "0" + mSecond : mSecond));
    }

    Handler dateandtimeHandler = new Handler() {

        @SuppressWarnings("deprecation")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_DATAPICK:
                    //showDialog(DATE_DIALOG_ID);
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            updateDateTimeDisplay();
                            //getTime.setText(year + "/" + monthOfYear + "/"+ dayOfMonth);
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                    break;
                case SHOW_TIMEPICK:
                    calendar = Calendar.getInstance();
                    TimePickerDialog tdialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            updateDateTimeDisplay();
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    tdialog.show();
                    //showDialog(TIME_DIALOG_ID);
                    break;
                case SHOW_CLOSEDATAPICK:
                    //showDialog(CLOSEDATE_DIALOG_ID);
                    break;
                default:
                    break;
            }
        }
    };

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
