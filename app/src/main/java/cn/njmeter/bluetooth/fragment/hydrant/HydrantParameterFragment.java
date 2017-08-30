package cn.njmeter.bluetooth.fragment.hydrant;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.constant.ProductType;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;

public class HydrantParameterFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.scrollView_root)
    ScrollView scrollView_root;

    @BindView(R.id.btn_read_parameter)
    Button btn_read_parameter;
    @BindView(R.id.tv_meterId)
    TextView tv_meterId;
    @BindView(R.id.btn_amend_meterid)
    Button btn_amend_meterid;
    @BindView(R.id.et_amend_meterId)
    EditText et_amend_meterId;

    @BindView(R.id.Buttonadjusttime)
    Button Buttonadjusttime;
    @BindView(R.id.CheckBoxsyn)
    CheckBox CheckBoxsyn;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.Buttontotalunit)
    Button Buttontotalunit;
    @BindView(R.id.RadioButtonunitm)
    RadioButton RadioButtonunitm;
    @BindView(R.id.RadioButtonunitgal)
    RadioButton RadioButtonunitgal;

    @BindView(R.id.Buttonamendx)
    Button Buttonamendx;
    @BindView(R.id.EditTextamendx)
    EditText EditTextamendx;
    @BindView(R.id.Buttonamendslope)
    Button Buttonamendslope;
    @BindView(R.id.EditTextslope)
    EditText EditTextslope;
    @BindView(R.id.Buttonstartf)
    Button Buttonstartf;
    @BindView(R.id.EditTextstartf)
    EditText EditTextstartf;

    @BindView(R.id.EditTextdiv1)
    EditText EditTextdiv1;
    @BindView(R.id.EditTextdiv2)
    EditText EditTextdiv2;
    @BindView(R.id.EditTextdiv3)
    EditText EditTextdiv3;
    @BindView(R.id.spinnerpointwhere)
    Spinner spinnerpointwhere;
    private String[] pointwhereitems = {"1000", "100", "10"};
    private String pointwhere = "1000";
    @BindView(R.id.spinnermetersize)
    Spinner spinnermetersize;
    @BindView(R.id.EditTextsleeptime)
    EditText EditTextsleeptime;
    @BindView(R.id.Buttonprograme)
    Button Buttonprograme;

    private String[] metersize15_40items = {"15", "20", "25", "32", "40"};
    private String[] metersize50_125items = {"50", "65", "80", "100", "125"};
    private String[] metersize150_500items = {"150", "200", "250", "300", "350", "400", "450", "500"};
    private String metersize = "20";
    private int pointwhereint = 0;

    private String productType = ProductType.HYDRANT;

    @BindView(R.id.Buttoncs16)
    Button Buttoncs16;
    @BindView(R.id.Buttonsend)
    Button Buttonsend;
    @BindView(R.id.EditTextsend)
    EditText EditTextsend;

    private int nYear;
    private int nMonth;
    private int nDay;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private int mSecond;

    private Timer datetimetimer = new Timer();

    private static final int SHOW_DATAPICK = 0;
    private static final int SHOW_TIMEPICK = 1;
    private static final int SHOW_ENDDATAPICK = 2;
    private static final int TIME_DIALOG_ID = 3;
    private static final int SHOW_CLOSEDATAPICK = 4;
    private static final int CLOSEDATE_DIALOG_ID = 5;

    public Context context;
    private Button btn_readType, btn_changeType;
    private RadioGroup radioGroup_type;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_hydrant_parameter_bluetooth, container, false);
        ButterKnife.bind(this, view);
        context = getContext();
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
            //当软键盘弹出的时候，heightDifference - height_menu>0，设置正常
            //当软键盘隐藏的时候，heightDifference - height_menu<0，此时需要设置为0，否则ScrollView最下面一部分会被Activity的菜单栏遮挡
            layoutParams.setMargins(0, 0, 0, heightDifference - height_menu < 0 ? 0 : heightDifference - height_menu);
            scrollView_root.requestLayout();
        }
    };

    @Override
    public void initView(View view) {

        //监听软键盘，滚动ScrollView以适应输入框
        getActivity().getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        radioGroup_type = (RadioGroup) view.findViewById(R.id.radioGroup_type);
        btn_readType = (Button) view.findViewById(R.id.btn_readType);
        btn_readType.setOnClickListener(onClickListener);
        btn_changeType = (Button) view.findViewById(R.id.btn_changeType);
        btn_changeType.setOnClickListener(onClickListener);

        //RadioButtonunitm.setChecked(true);
        // RadioButtonunitgal.setChecked(false);
        ArrayAdapter<String> pointwherelist = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout_head, pointwhereitems);
        final ArrayAdapter<String> metersize15_40list = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout_head, metersize15_40items);
        final ArrayAdapter<String> metersize50_125list = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout_head, metersize50_125items);
        final ArrayAdapter<String> metersize150_500list = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout_head, metersize150_500items);
        spinnerpointwhere.setAdapter(pointwherelist);
        spinnerpointwhere.setSelection(0, false);
        spinnermetersize.setAdapter(metersize15_40list);
        spinnermetersize.setSelection(0, false);

        spinnerpointwhere.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                pointwhere = pointwhereitems[arg2];
                if (pointwhere.endsWith("1000")) {
                    spinnermetersize.setAdapter(metersize15_40list);
                    spinnermetersize.setSelection(1, false);
                    metersize = "20";
                    pointwhereint = 0;
                } else if (pointwhere.endsWith("100")) {
                    spinnermetersize.setAdapter(metersize50_125list);
                    spinnermetersize.setSelection(0, false);
                    metersize = "50";
                    pointwhereint = 1;
                } else if (pointwhere.endsWith("10")) {
                    spinnermetersize.setAdapter(metersize150_500list);
                    spinnermetersize.setSelection(0, false);
                    metersize = "150";
                    pointwhereint = 2;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        spinnermetersize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (pointwhere.endsWith("1000")) {
                    metersize = metersize15_40items[arg2];
                } else if (pointwhere.endsWith("100")) {
                    metersize = metersize50_125items[arg2];
                } else if (pointwhere.endsWith("10")) {
                    metersize = metersize150_500items[arg2];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        Buttonprograme.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    int xi = 0;
                    String y = "";
                    int yi = 0;
                    String z = "";
                    int zi = 0;
                    String o = "";
                    int oi = 0;
                    String p = "";
                    int pi = 0;
                    String q = "";
                    x = EditTextdiv1.getText().toString().replace(" ", "");
                    EditTextdiv1.setText(x);
                    try {
                        xi = Integer.parseInt(x);
                        if (xi > 65536) {
                            CommonUtils.showToast(context, "请正确输入分界点1(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入分界点1(<65536)");
                        return;
                    }

                    y = EditTextdiv2.getText().toString().replace(" ", "");
                    EditTextdiv2.setText(y);
                    try {
                        yi = Integer.parseInt(y);
                        if (yi > 65536) {
                            CommonUtils.showToast(context, "请正确输入分界点2(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入分界点2(<65536)");
                        return;
                    }

                    z = EditTextdiv3.getText().toString().replace(" ", "");
                    EditTextdiv3.setText(z);
                    try {
                        zi = Integer.parseInt(z);
                        if (zi > 65536) {
                            CommonUtils.showToast(context, "请正确输入分界点3(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入分界点3(<65536)");
                        return;
                    }
                    o = pointwhere;
                    try {
                        oi = Integer.parseInt(o);
                        if (oi > 65536) {
                            CommonUtils.showToast(context, "请正确选则显示单位(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确选则显示单位(<65536)");
                        return;
                    }

                    p = metersize;
                    try {
                        pi = Integer.parseInt(p);
                        if (pi > 65536) {
                            CommonUtils.showToast(context, "请正确显示仪表口(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确显示仪表口(<65536)");
                        return;
                    }
                    q = EditTextsleeptime.getText().toString().replace(" ", "");
                    EditTextsleeptime.setText(q);
                    if ((q.length() > 4) || (q.length() <= 0)) {
                        CommonUtils.showToast(context, "请正确输入睡眠时间(<FFFF)");
                        return;
                    }
                    String tx = programme(meterid, productType, "001111", xi, yi, zi, oi, pi, q);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putString("div1",x);
//                    editor.putString("div2",y);
//                    editor.putString("div3",z);
//                    editor.putInt("pointwhereint",pointwhereint);
//                    editor.putString("sleeptime",q);
//                    editor.commit();//提交修改
                }
            }
        });
        btn_read_parameter.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                tv_meterId.setText("");
                BluetoothToolsMainActivity.data = "";
                String tx = "6820AAAAAAAAAAAAAA1A039A2F001416";
                EditTextsend.setText(tx);
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

        btn_amend_meterid.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String amendmeterid = "";
                    amendmeterid = et_amend_meterId.getText().toString().replace(" ", "");
                    et_amend_meterId.setText(amendmeterid);
                    if (amendmeterid.length() != 8) {
                        CommonUtils.showToast(context, "请输入8位修改表号");
                    } else {
                        try {
                            Integer.parseInt(amendmeterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位修改表号");
                            return;
                        }
                        String tx = amendmeterid(meterid, productType, "001111", amendmeterid);
                        EditTextsend.setText(tx);
                        BluetoothToolsMainActivity.data = "";
                        BluetoothToolsMainActivity.writeData(tx);
                        et_amend_meterId.clearFocus();
//                        SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("meterid",meterid);
//                        editor.putString("amendmeterid",amendmeterid);
//                        editor.commit();//提交修改
                    }
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
                    tv_time.setText(new StringBuilder().append((mHour < 10) ? "0" + mHour : mHour).append(":")
                            .append((mMinute < 10) ? "0" + mMinute : mMinute).append(":")
                            .append((mSecond < 10) ? "0" + mSecond : mSecond));
                }
            }
        };
        datetimetimer = new Timer();
        datetimetimer.schedule(new TimerTask() {
            public void run() {
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        }, 0, 1000);

        tv_date.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = SHOW_DATAPICK;
                dateandtimeHandler.sendMessage(msg);
            }
        });
        tv_time.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = SHOW_TIMEPICK;
                dateandtimeHandler.sendMessage(msg);
            }
        });
        Buttonadjusttime.setOnClickListener(new View.OnClickListener() {
            // @Override
            @SuppressLint("SimpleDateFormat")
            public void onClick(View v) {
                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    String rx = "";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat sdf1 = new SimpleDateFormat("ssmmHHddMMyyyy");
                    x = tv_date.getText().toString() + " " + tv_time.getText().toString();
                    try {
                        Date xd = sdf.parse(x);
                        rx = sdf1.format(xd);
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入当前时间");
                        return;
                    }

                    String tx = adjusttime(meterid, productType, "001111", rx);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
                }
            }
        });
        Buttoncs16.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String tx = EditTextsend.getText().toString().replace(" ", "");
                if (tx.length() % 2 != 0) {
                    CommonUtils.showToast(context, "发送的指令为--奇数");
                    return;
                }
                tx = tx + AnalysisUtils.getCSSum(tx, 0) + "16";
                EditTextsend.setText(tx);
            }
        });
        Buttonsend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String tx = "";
                tx = EditTextsend.getText().toString().replace(" ", "");
                if (tx.length() % 2 != 0) {
                    CommonUtils.showToast(context, "发送的指令为--奇数");
                    return;
                }
                EditTextsend.setText(tx);
                BluetoothToolsMainActivity.data = "";
                BluetoothToolsMainActivity.writeData(tx);
            }
        });

        Buttontotalunit.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String units = "";
                    boolean b = RadioButtonunitm.isChecked();
                    if (b) units = "100E";
                    else
                        units = "E803";
                    String tx = setheatunit(meterid, productType, "001111", units);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putBoolean("unitm",b);
//                    editor.commit();//提交修改
                }
            }
        });

        Buttonamendx.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    int xi = 0;
                    x = EditTextamendx.getText().toString().replace(" ", "");
                    EditTextamendx.setText(x);
                    try {
                        xi = Integer.parseInt(x);
                        if (xi > 65536) {
                            CommonUtils.showToast(context, "请正确输入修正系数(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入修正系数(<65536)");
                        return;
                    }
                    String tx = amendx(meterid, productType, "001111", xi);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putString("amendx",x);
//                    editor.commit();//提交修改
                }
            }
        });
        Buttonstartf.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    int xi = 0;
                    x = EditTextstartf.getText().toString().replace(" ", "");
                    EditTextstartf.setText(x);
                    try {
                        xi = Integer.parseInt(x);
                        if (xi > 65536) {
                            CommonUtils.showToast(context, "请正确输入启动流量(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入启动流量(<65536)");
                        return;
                    }
                    String tx = amendstartf(meterid, productType, "001111", xi);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putString("startf",x);
//                    editor.commit();//提交修改
                }
            }
        });
        Buttonamendslope.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    int xi = 0;
                    x = EditTextslope.getText().toString().replace(" ", "");
                    EditTextslope.setText(x);
                    try {
                        xi = Integer.parseInt(x);
                        if (xi > 65536) {
                            CommonUtils.showToast(context, "请正确输入斜率修正(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入斜率修正(<65536)");
                        return;
                    }
                    String tx = amendslope(meterid, productType, "001111", xi);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putString("slope",x);
//                    editor.commit();//提交修改
                }
            }
        });
        Buttonstartf.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {

                String meterid = "";
                meterid = tv_meterId.getText().toString().replace(" ", "");
                //tv_meterId.setText(meterid);
                if (meterid.length() != 8) {
                    CommonUtils.showToast(context, "请输入8位出厂编码");
                } else {
                    if (!meterid.endsWith("FFFFFFFF")) {
                        try {
                            Integer.parseInt(meterid);
                        } catch (Exception e) {
                            CommonUtils.showToast(context, "请输入8位出厂编码");
                            return;
                        }
                    }
                    String x = "";
                    int xi = 0;
                    x = EditTextstartf.getText().toString().replace(" ", "");
                    EditTextstartf.setText(x);
                    try {
                        xi = Integer.parseInt(x);
                        if (xi > 65536) {
                            CommonUtils.showToast(context, "请正确输入启动流量(<65536)");
                            return;
                        }
                    } catch (Exception e) {
                        CommonUtils.showToast(context, "请正确输入启动流量(<65536)");
                        return;
                    }
                    String tx = amendstartf(meterid, productType, "001111", xi);
                    EditTextsend.setText(tx);
                    BluetoothToolsMainActivity.data = "";
                    BluetoothToolsMainActivity.writeData(tx);
//                    SharedPreferences sharedPreferences = getSharedPreferences("setparameter", Context.MODE_PRIVATE);
//                    Editor editor = sharedPreferences.edit();
//                    editor.putString("meterid",meterid);
//                    editor.putString("startf",x);
//                    editor.commit();//提交修改
                }
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String meterId, number, checkCode, command;
            meterId = tv_meterId.getText().toString();
            if (TextUtils.isEmpty(meterId)) {
                CommonUtils.showToast(context, "请先读取参数获取表号");
            }
            switch (v.getId()) {
                case R.id.btn_readType:
                    //读取类型
                    number = "68" + productType + meterId + "0011112803901F00";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_changeType:
                    //修改类型
                    String type;
                    switch (radioGroup_type.getCheckedRadioButtonId()) {
                        case R.id.radioButton_typeI:
                            type = "01";
                            break;
                        case R.id.radioButton_typeII:
                            type = "02";
                            break;
                        case R.id.radioButton_typeIII:
                            type = "FF";
                            break;
                        default:
                            type = "FF";
                            break;
                    }
                    number = "68" + productType + meterId + "0011112803902F" + type;
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
            }
        }
    };

    Handler dateandtimeHandler = new Handler() {
        Calendar calendar;// 用来装日期的
        DatePickerDialog dialog;

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
                case SHOW_ENDDATAPICK:
                    calendar = Calendar.getInstance();
                    DatePickerDialog enddialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            nYear = year;
                            nMonth = monthOfYear;
                            nDay = dayOfMonth;
                            //  updateEndDateTimeDisplay();
                            //getTime.setText(year + "/" + monthOfYear + "/"+ dayOfMonth);
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    enddialog.show();
                    break;
                case SHOW_CLOSEDATAPICK:
                    //showDialog(CLOSEDATE_DIALOG_ID);
                    break;

            }
        }

    };

    private void updateDateTimeDisplay() {
        CheckBoxsyn.setChecked(false);
        tv_date.setText(new StringBuilder().append(mYear).append("-")
                .append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)).append("-")
                .append((mDay < 10) ? "0" + mDay : mDay));
        tv_time.setText(new StringBuilder().append((mHour < 10) ? "0" + mHour : mHour).append(":")
                .append((mMinute < 10) ? "0" + mMinute : mMinute).append(":")
                .append((mSecond < 10) ? "0" + mSecond : mSecond));
    }


    private String SetPulseParam(String meterid, String producttypetx, String factorycode, String strDN, String strUnit, String strCycle, String strWidth) {
        String tx = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "C307" + strDN + strUnit + strCycle + strWidth, 0);
            tx = ("68" + producttypetx + meterid + factorycode + "C307" + strDN + strUnit + strCycle + strWidth + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return tx;
    }

    public String SetLoraCh(String meterid, String producttypetx, String factorycode, String strch) {
        String tx = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + "11111111" + factorycode + "0705701C" + strch + "0000", 0);
            tx = ("68" + producttypetx + "11111111" + factorycode + "0705701C" + strch + "0000" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return tx;
    }

    public String amendmeterid(String meterid, String producttypetx, String factorycode, String amendmeterid) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "150AA018AA" + amendmeterid + factorycode, 0);
            r = ("68" + producttypetx + meterid + factorycode + "150AA018AA" + amendmeterid + factorycode + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String clearmeter(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "3A03901F00", 0);
            r = ("68" + producttypetx + meterid + factorycode + "3A03901F00" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String initreport(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "0403A09A00", 0);
            r = ("68" + producttypetx + meterid + factorycode + "0403A09A00" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String clearbatter(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "3A03907F00", 0);
            r = ("68" + producttypetx + meterid + factorycode + "3A03907F00" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String CearRstTimes(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "4A0301F100", 0);
            r = ("68" + producttypetx + meterid + factorycode + "4A0301F100" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String adjusttime(String meterid, String producttypetx, String factorycode, String x) {
        String r = "";
        String rx = "";
        try {
            rx = x.substring(0, 10) + x.substring(12, 14) + x.substring(10, 12);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "040AA01500" + rx, 0);
            r = "68" + producttypetx + meterid + factorycode + "040AA01500" + rx + cs + "16";
        } catch (Exception e) {
        }
        return r;
    }

    public String setheatunit(String meterid, String producttypetx, String factorycode, String units) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "B204901F" + units, 0);
            r = ("68" + producttypetx + meterid + factorycode + "B204901F" + units + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendx(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "B104901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "B104901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendslope(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "B304901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "B304901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendstartf(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "B404901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "B404901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String programme(String meterid, String producttypetx, String factorycode, int x, int y, int z, int o, int p, String q) {
        String r = "";
        try {
            String rx = "";
            String ry = "";
            String rz = "";
            String ro = "";
            String rp = "";
            String rq = "0000";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            ry = Integer.toHexString(y);
            switch (ry.length()) {
                case 0:
                    ry = "0000";
                    break;
                case 1:
                    ry = "000" + ry;
                    break;
                case 2:
                    ry = "00" + ry;
                    break;
                case 3:
                    ry = "0" + ry;
                    break;
                case 4:
                    break;
                default:
                    ry = ry.substring(ry.length() - 4, ry.length());
                    break;
            }
            rz = Integer.toHexString(z);
            switch (rz.length()) {
                case 0:
                    rz = "0000";
                    break;
                case 1:
                    rz = "000" + rz;
                    break;
                case 2:
                    rz = "00" + rz;
                    break;
                case 3:
                    rz = "0" + rz;
                    break;
                case 4:
                    break;
                default:
                    rz = rz.substring(rz.length() - 4, rz.length());
                    break;
            }
            ro = Integer.toHexString(o);
            switch (ro.length()) {
                case 0:
                    ro = "0000";
                    break;
                case 1:
                    ro = "000" + ro;
                    break;
                case 2:
                    ro = "00" + ro;
                    break;
                case 3:
                    ro = "0" + ro;
                    break;
                case 4:
                    break;
                default:
                    ro = ro.substring(ro.length() - 4, ro.length());
                    break;
            }
            rp = Integer.toString(p);
            switch (rp.length()) {
                case 0:
                    rp = "0000";
                    break;
                case 1:
                    rp = "000" + rp;
                    break;
                case 2:
                    rp = "00" + rp;
                    break;
                case 3:
                    rp = "0" + rp;
                    break;
                case 4:
                    break;
                default:
                    rp = rp.substring(rp.length() - 4, rp.length());
                    break;
            }
            switch (q.length()) {
                case 0:
                    rq = "0000";
                    break;
                case 1:
                    rq = "000" + q;
                    break;
                case 2:
                    rq = "00" + q;
                    break;
                case 3:
                    rq = "0" + q;
                    break;
                case 4:
                    rq = q;
                    break;
                default:
                    rq = q.substring(q.length() - 4, q.length());
                    break;
            }
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "3D0F903F00" + rx + ry + rz + ro + rq + rp, 0);
            r = ("68" + producttypetx + meterid + factorycode + "3D0F903F00" + rx + ry + rz + ro + rq + rp + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String setport(String meterid, String producttypetx, String factorycode, String port) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "C203901F" + port, 0);
            r = ("68" + producttypetx + meterid + factorycode + "C203901F" + port + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendq(String meterid, String producttypetx, String factorycode, int x, int y, int z, int o) {
        String r = "";
        try {
            String rx = "";
            String ry = "";
            String rz = "";
            String ro = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            ry = Integer.toHexString(y);
            switch (ry.length()) {
                case 0:
                    ry = "0000";
                    break;
                case 1:
                    ry = "000" + ry;
                    break;
                case 2:
                    ry = "00" + ry;
                    break;
                case 3:
                    ry = "0" + ry;
                    break;
                case 4:
                    break;
                default:
                    ry = ry.substring(ry.length() - 4, ry.length());
                    break;
            }
            ry = ry.substring(2, 4) + ry.substring(0, 2);
            rz = Integer.toHexString(z);
            switch (rz.length()) {
                case 0:
                    rz = "0000";
                    break;
                case 1:
                    rz = "000" + rz;
                    break;
                case 2:
                    rz = "00" + rz;
                    break;
                case 3:
                    rz = "0" + rz;
                    break;
                case 4:
                    break;
                default:
                    rz = rz.substring(rz.length() - 4, rz.length());
                    break;
            }
            rz = rz.substring(2, 4) + rz.substring(0, 2);
            ro = Integer.toHexString(o);
            switch (ro.length()) {
                case 0:
                    ro = "0000";
                    break;
                case 1:
                    ro = "000" + ro;
                    break;
                case 2:
                    ro = "00" + ro;
                    break;
                case 3:
                    ro = "0" + ro;
                    break;
                case 4:
                    break;
                default:
                    ro = ro.substring(ro.length() - 4, ro.length());
                    break;
            }
            ro = ro.substring(2, 4) + ro.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "360CA0198800" + rx + ry + rz + ro, 0);
            r = ("68" + producttypetx + meterid + factorycode + "360CA0198800" + rx + ry + rz + ro + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendtcoef(String meterid, String producttypetx, String factorycode, int x, int y) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);

            String ry = "";
            ry = Integer.toHexString(y);
            switch (ry.length()) {
                case 0:
                    ry = "0000";
                    break;
                case 1:
                    ry = "000" + ry;
                    break;
                case 2:
                    ry = "00" + ry;
                    break;
                case 3:
                    ry = "0" + ry;
                    break;
                case 4:
                    break;
                default:
                    ry = ry.substring(ry.length() - 4, ry.length());
                    break;
            }
            ry = ry.substring(2, 4) + ry.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "3808A0198800" + rx + ry, 0);
            r = ("68" + producttypetx + meterid + factorycode + "3808A0198800" + rx + ry + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendt(String meterid, String producttypetx, String factorycode, int x, int y, int z, int o, int p) {
        String r = "";
        try {
            String rx = "";
            String ry = "";
            String rz = "";
            String ro = "";
            String rp = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            ry = Integer.toHexString(y);
            switch (ry.length()) {
                case 0:
                    ry = "0000";
                    break;
                case 1:
                    ry = "000" + ry;
                    break;
                case 2:
                    ry = "00" + ry;
                    break;
                case 3:
                    ry = "0" + ry;
                    break;
                case 4:
                    break;
                default:
                    ry = ry.substring(ry.length() - 4, ry.length());
                    break;
            }
            ry = ry.substring(2, 4) + ry.substring(0, 2);
            rz = Integer.toHexString(z);
            switch (rz.length()) {
                case 0:
                    rz = "0000";
                    break;
                case 1:
                    rz = "000" + rz;
                    break;
                case 2:
                    rz = "00" + rz;
                    break;
                case 3:
                    rz = "0" + rz;
                    break;
                case 4:
                    break;
                default:
                    rz = rz.substring(rz.length() - 4, rz.length());
                    break;
            }
            rz = rz.substring(2, 4) + rz.substring(0, 2);
            ro = Integer.toHexString(o);
            switch (ro.length()) {
                case 0:
                    ro = "0000";
                    break;
                case 1:
                    ro = "000" + ro;
                    break;
                case 2:
                    ro = "00" + ro;
                    break;
                case 3:
                    ro = "0" + ro;
                    break;
                case 4:
                    break;
                default:
                    ro = ro.substring(ro.length() - 4, ro.length());
                    break;
            }
            ro = ro.substring(2, 4) + ro.substring(0, 2);
            rp = Integer.toString(p);
            switch (rp.length()) {
                case 0:
                    rp = "0000";
                    break;
                case 1:
                    rp = "000" + rp;
                    break;
                case 2:
                    rp = "00" + rp;
                    break;
                case 3:
                    rp = "0" + rp;
                    break;
                case 4:
                    break;
                default:
                    rp = rp.substring(rp.length() - 4, rp.length());
                    break;
            }
            rp = rp.substring(2, 4) + rp.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "370EA0198800" + rx + ry + rz + ro + rp, 0);
            r = ("68" + producttypetx + meterid + factorycode + "370EA0198800" + rx + ry + rz + ro + rp + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String resetparameter(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "B703901F00", 0);
            r = ("68" + producttypetx + meterid + factorycode + "B703901F00" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendclosedate(String meterid, String producttypetx, String factorycode, String x) {
        String r = "";
        try {
            String rx = "";
            String ry = "";
            String rz = "";
            String ro = "";
            rx = Integer.toHexString(Integer.parseInt(x.subSequence(0, 2).toString()));
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            ry = Integer.toHexString(Integer.parseInt(x.subSequence(2, 4).toString()));
            switch (ry.length()) {
                case 0:
                    ry = "0000";
                    break;
                case 1:
                    ry = "000" + ry;
                    break;
                case 2:
                    ry = "00" + ry;
                    break;
                case 3:
                    ry = "0" + ry;
                    break;
                case 4:
                    break;
                default:
                    ry = ry.substring(ry.length() - 4, ry.length());
                    break;
            }
            rz = Integer.toHexString(Integer.parseInt(x.subSequence(4, 6).toString()));
            switch (rz.length()) {
                case 0:
                    rz = "0000";
                    break;
                case 1:
                    rz = "000" + rz;
                    break;
                case 2:
                    rz = "00" + rz;
                    break;
                case 3:
                    rz = "0" + rz;
                    break;
                case 4:
                    break;
                default:
                    rz = rz.substring(rz.length() - 4, rz.length());
                    break;
            }
            ro = Integer.toHexString(Integer.parseInt(x.subSequence(6, 8).toString()));
            switch (ro.length()) {
                case 0:
                    ro = "0000";
                    break;
                case 1:
                    ro = "000" + ro;
                    break;
                case 2:
                    ro = "00" + ro;
                    break;
                case 3:
                    ro = "0" + ro;
                    break;
                case 4:
                    break;
                default:
                    ro = ro.substring(ro.length() - 4, ro.length());
                    break;
            }
            ro = ro.substring(2, 4) + ro.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "360CA01A88" + rx + ry + rz + ro, 0);
            r = ("68" + producttypetx + meterid + factorycode + "360CA01A88" + rx + ry + rz + ro + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendchecktime(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "0000";
                    break;
                case 1:
                    rx = "000" + rx;
                    break;
                case 2:
                    rx = "00" + rx;
                    break;
                case 3:
                    rx = "0" + rx;
                    break;
                case 4:
                    break;
                default:
                    rx = rx.substring(rx.length() - 4, rx.length());
                    break;
            }
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "C504901F00" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "C504901F00" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendpermission(String meterid, String producttypetx, String factorycode, String x) {
        String r = "";
        try {
            String rx = "";
            rx = x.substring(2, 4) + x.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "C204901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "C204901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendpluse(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "00";
                    break;
                case 1:
                    rx = "0" + rx;
                    break;
                case 2:
                    break;
            }
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "3A03909A" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "3A03909A" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String amendalarm(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "00";
                    break;
                case 1:
                    rx = "0" + rx;
                    break;
                case 2:
                    break;
            }
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "6603901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "6603901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String creditdsumheat(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "00000000";
                    break;
                case 1:
                    rx = "0000000" + rx;
                    break;
                case 2:
                    rx = "000000" + rx;
                    break;
                case 3:
                    rx = "00000" + rx;
                    break;
                case 4:
                    rx = "0000" + rx;
                    break;
                case 5:
                    rx = "000" + rx;
                    break;
                case 6:
                    rx = "00" + rx;
                    break;
                case 7:
                    rx = "0" + rx;
                    break;
                case 8:
                    break;
                default:
                    rx = rx.substring(rx.length() - 8, rx.length());
                    break;
            }
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "4D07901F00" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "4D07901F00" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String cleardsumheat(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "4D03905F00", 0);
            r = ("68" + producttypetx + meterid + factorycode + "4D03905F00" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String initdsumheat(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx = "";
            rx = Integer.toHexString(x);
            switch (rx.length()) {
                case 0:
                    rx = "00000000";
                    break;
                case 1:
                    rx = "0000000" + rx;
                    break;
                case 2:
                    rx = "000000" + rx;
                    break;
                case 3:
                    rx = "00000" + rx;
                    break;
                case 4:
                    rx = "0000" + rx;
                    break;
                case 5:
                    rx = "000" + rx;
                    break;
                case 6:
                    rx = "00" + rx;
                    break;
                case 7:
                    rx = "0" + rx;
                    break;
                case 8:
                    break;
                default:
                    rx = rx.substring(rx.length() - 8, rx.length());
                    break;
            }
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "5E07901F00" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "5E07901F00" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    public String openvalve(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "0404A0170055", 0);
            r = ("68" + producttypetx + meterid + factorycode + "0404A0170055" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
    }

    @SuppressLint("DefaultLocale")
    public String closevalve(String meterid, String producttypetx, String factorycode) {
        String r = "";
        try {
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "0404A0170099", 0);
            r = ("68" + producttypetx + meterid + factorycode + "0404A0170099" + cs + "16").toUpperCase();
        } catch (Exception e) {
        }
        return r;
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
