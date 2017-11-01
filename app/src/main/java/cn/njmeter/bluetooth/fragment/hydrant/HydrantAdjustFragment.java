package cn.njmeter.bluetooth.fragment.hydrant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Map;

import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.activity.bluetoothtools.BluetoothToolsMainActivity;
import cn.njmeter.bluetooth.constant.ProductType;
import cn.njmeter.bluetooth.fragment.BaseFragment;
import cn.njmeter.bluetooth.interfaces.OnMultiClickListener;
import cn.njmeter.bluetooth.utils.AnalysisUtils;
import cn.njmeter.bluetooth.utils.CommonUtils;
import cn.njmeter.bluetooth.utils.MathUtils;

public class HydrantAdjustFragment extends BaseFragment implements View.OnClickListener {

    private ScrollView scrollView_root;
    private EditText et_meterId, et_qn_1, et_qn_3, et_qn2_1, et_qn2_3, et_qn1_1, et_qn1_3, et_qmin_1, et_qmin_3;
    private TextView tv_qn_2, tv_qn2_2, tv_qn1_2, tv_qmin_2;
    private TextView tv_consumption_positive, tv_consumption_reverse;
    private EditText et_consumption_positive, et_consumption_reverse, et_set_common_pressure;
    private String meterId = "";
    private static String productType = ProductType.HYDRANT;
    public Context context;
    public static double flowUnit_positive, flowUnit_reserve;               //正反向流量单位与标准单位的倍数关系，用于设置流量时使用

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_hydrant_adjust_bluetooth, container, false);
        context = getContext();
        initView(view);
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

        Button btn_read_parameter, btn_factory_reset, btn_set_flow_coefficient, btn_adjust_consumption, btn_read_flow, btn_clear_flow,
                btn_read_pressure_calibration, btn_set_zero_point_calibration, btn_set_normal_point_calibration;
        scrollView_root = (ScrollView) view.findViewById(R.id.scrollView_root);
        et_meterId = (EditText) view.findViewById(R.id.et_meterId);
        et_qn_1 = (EditText) view.findViewById(R.id.et_qn_1);
        et_qn_3 = (EditText) view.findViewById(R.id.et_qn_3);
        et_qn2_1 = (EditText) view.findViewById(R.id.et_qn2_1);
        et_qn2_3 = (EditText) view.findViewById(R.id.et_qn2_3);
        et_qn1_1 = (EditText) view.findViewById(R.id.et_qn1_1);
        et_qn1_3 = (EditText) view.findViewById(R.id.et_qn1_3);
        et_qmin_1 = (EditText) view.findViewById(R.id.et_qmin_1);
        et_qmin_3 = (EditText) view.findViewById(R.id.et_qmin_3);
        et_consumption_positive = (EditText) view.findViewById(R.id.et_consumption_positive);
        et_consumption_reverse = (EditText) view.findViewById(R.id.et_consumption_reverse);
        et_set_common_pressure = (EditText) view.findViewById(R.id.et_set_common_pressure);
        tv_qn_2 = (TextView) view.findViewById(R.id.tv_qn_2);
        tv_qn2_2 = (TextView) view.findViewById(R.id.tv_qn2_2);
        tv_qn1_2 = (TextView) view.findViewById(R.id.tv_qn1_2);
        tv_qmin_2 = (TextView) view.findViewById(R.id.tv_qmin_2);
        tv_consumption_positive = (TextView) view.findViewById(R.id.tv_flow_positive);
        tv_consumption_reverse = (TextView) view.findViewById(R.id.tv_flow_reserve);
        btn_read_parameter = (Button) view.findViewById(R.id.btn_read_parameter);
        btn_factory_reset = (Button) view.findViewById(R.id.btn_factory_reset);
        btn_set_flow_coefficient = (Button) view.findViewById(R.id.btn_set_flow_coefficient);
        btn_adjust_consumption = (Button) view.findViewById(R.id.btn_adjust_consumption);
        btn_read_flow = (Button) view.findViewById(R.id.btn_read_flow);
        btn_clear_flow = (Button) view.findViewById(R.id.btn_clear_flow);
        btn_read_pressure_calibration = (Button) view.findViewById(R.id.btn_read_pressure_calibration);
        btn_set_zero_point_calibration = (Button) view.findViewById(R.id.btn_set_zero_point_calibration);
        btn_set_normal_point_calibration = (Button) view.findViewById(R.id.btn_set_normal_point_calibration);
        btn_adjust_consumption.setOnClickListener(onClickListener);
        btn_factory_reset.setOnClickListener(onClickListener);
        btn_read_parameter.setOnClickListener(onClickListener);
        btn_set_flow_coefficient.setOnClickListener(onClickListener);
        btn_clear_flow.setOnClickListener(onClickListener);
        btn_read_flow.setOnClickListener(onClickListener);
        btn_read_pressure_calibration.setOnClickListener(onClickListener);
        btn_set_zero_point_calibration.setOnClickListener(onClickListener);
        btn_set_normal_point_calibration.setOnClickListener(onClickListener);

        setCharSequence(et_qn_1.getText());
        setCharSequence(et_qn2_1.getText());
        setCharSequence(et_qn1_1.getText());
        setCharSequence(et_qmin_1.getText());
        setCharSequence(et_qn_3.getText());
        setCharSequence(et_qn2_3.getText());
        setCharSequence(et_qn1_3.getText());
        setCharSequence(et_qmin_3.getText());
        setCharSequence(et_consumption_positive.getText());
        setCharSequence(et_consumption_reverse.getText());

        et_qn_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double x1, xx, xn;
                try {
                    x1 = -Double.valueOf(s.toString().replace(" ", ""));
                } catch (Exception e) {
                    return;
                }
                try {
                    xx = Double.valueOf(tv_qn_2.getText().toString().replace(" ", ""));
                    xn = xx * (1 + x1 * 0.01);
                } catch (Exception e) {
                    return;
                }
                et_qn_3.setText(String.valueOf((int) xn));
            }
        });

        et_qn2_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double x1, xx, xn;
                try {
                    x1 = -Double.valueOf(s.toString().replace(" ", ""));
                } catch (Exception e) {

                    return;
                }
                try {
                    xx = Double.valueOf(tv_qn2_2.getText().toString().replace(" ", ""));
                    xn = xx * (1 + x1 * 0.01);
                } catch (Exception e) {

                    return;
                }
                et_qn2_3.setText(String.valueOf((int) xn));
            }
        });
        et_qn1_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                //
                double x1, xx, xn;
                try {
                    x1 = -Double.valueOf(s.toString().replace(" ", ""));
                } catch (Exception e) {
                    //Toast.makeText(context, "error1", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    xx = Double.valueOf(tv_qn1_2.getText().toString().replace(" ", ""));
                    xn = xx * (1 + x1 * 0.01);
                } catch (Exception e) {
                    // Toast.makeText(context, "error2", Toast.LENGTH_SHORT).show();
                    return;
                }
                et_qn1_3.setText(String.valueOf((int) xn));
                //  try
                // {
                //	   x2=Double.valueOf(s.toString().replace(" ", ""));
                //  }catch (Exception e) {
                //	   return;
                //   }
                //DecimalFormat df = new DecimalFormat("#.##");
                //et_qn2_1.setText(df.format((x1+x2)/2));
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });
        et_qmin_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                double x1, xx, xn;
                try {
                    x1 = -Double.valueOf(s.toString().replace(" ", ""));
                } catch (Exception e) {
                    return;
                }
                try {
                    xx = Double.valueOf(tv_qmin_2.getText().toString().replace(" ", ""));
                    xn = xx * (1 + x1 * 0.01);
                } catch (Exception e) {
                    return;
                }
                et_qmin_3.setText(String.valueOf((int) xn));
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
        });

        et_consumption_positive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setInputType(et_consumption_positive, flowUnit_positive, s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_consumption_reverse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setInputType(et_consumption_reverse, flowUnit_reserve, s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        et_set_common_pressure.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //限制小数点后面最多两位
                if (s.toString().contains(".")) {
                    //限制小数位数不大于2个
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
                        et_set_common_pressure.setText(s);
                        //设置光标在末尾
                        et_set_common_pressure.setSelection(s.length());
                        CommonUtils.showToast(context, "最多两位小数");
                    }
                    //如果输入的值超过范围（0.00＜X≤10.00）弹出提示
                    if (s.length() - 1 - s.toString().indexOf(".") == 1 || s.length() - 1 - s.toString().indexOf(".") == 2) {
                        if (et_set_common_pressure.getText().toString().equals("0.00") || Float.valueOf(et_set_common_pressure.getText().toString()) > 10.00f) {
                            //去掉刚输入的数字
                            s = s.toString().subSequence(0, s.length() - 1);
                            et_set_common_pressure.setText(s);
                            //设置光标在末尾
                            et_set_common_pressure.setSelection(s.length());
                            CommonUtils.showToast(context, "设定范围0.00＜X≤10.00");
                        }
                    }
                } else if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.valueOf(et_set_common_pressure.getText().toString()) > 10) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        et_set_common_pressure.setText(s);
                        //设置光标在末尾
                        et_set_common_pressure.setSelection(s.length());
                        CommonUtils.showToast(context, "设定范围0.00＜X≤10.00");
                    }
                }
                //如果直接输入小数点，前面自动补0
                if (s.toString().trim().equals(".")) {
                    s = "0" + s;
                    et_set_common_pressure.setText(s);
                    et_set_common_pressure.setSelection(2);
                }
                //除了小数开头不能为0，而且开头不允许连续出现0
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        et_set_common_pressure.setText(s.subSequence(0, 1));
                        et_set_common_pressure.setSelection(1);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private OnMultiClickListener onClickListener = new OnMultiClickListener() {
        @Override
        public void onMultiClick(View v) {
            String number, checkCode, command;
            switch (v.getId()) {
                case R.id.btn_set_flow_coefficient:
                    meterId = "";
                    meterId = et_meterId.getText().toString().replace(" ", "");
                    et_meterId.setText(meterId);
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请先读取流量系数");
                    } else {
                        setFlowCoefficient();
                    }
                    break;
                case R.id.btn_read_parameter:
                    et_meterId.setText("");
                    et_qn_1.setText("");
                    tv_qn_2.setText("");
                    et_qn_3.setText("");
                    et_qn2_1.setText("");
                    tv_qn2_2.setText("");
                    et_qn2_3.setText("");
                    et_qn1_1.setText("");
                    tv_qn1_2.setText("");
                    et_qn1_3.setText("");
                    et_qmin_1.setText("");
                    tv_qmin_2.setText("");
                    et_qmin_3.setText("");
                    meterId = et_meterId.getText().toString();
                    if (TextUtils.isEmpty(meterId)) {
                        meterId = "FFFFFFFF";
                    } else if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请输入正确的消火栓编号");
                        return;
                    }
                    number = "68" + productType + meterId + "AAAAAA1A039A2F00";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_factory_reset:
                    meterId = "";
                    meterId = et_meterId.getText().toString().replace(" ", "");
                    et_meterId.setText(meterId);
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请先读取流量系数");
                    } else {
                        AlertDialog dialog1 = new AlertDialog.Builder(context)
                                .setTitle("警告!")
                                .setMessage("确定要恢复出厂值吗？")
                                .setPositiveButton("确定",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                int xi = 0, yi = 0, zi = 0, oi = 0;
                                                SharedPreferences sharedPreferences = context.getSharedPreferences("setQnParam", Context.MODE_PRIVATE);
                                                Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                                                for (Map.Entry<String, String> entry : map.entrySet()) {
                                                    if (entry.getKey().endsWith(meterId + "qn_2")) {
                                                        xi = Integer.parseInt(entry.getValue());
                                                    }
                                                    if (entry.getKey().endsWith(meterId + "qn2_2")) {
                                                        yi = Integer.parseInt(entry.getValue());
                                                    }
                                                    if (entry.getKey().endsWith(meterId + "qn1_2")) {
                                                        zi = Integer.parseInt(entry.getValue());
                                                    }
                                                    if (entry.getKey().endsWith(meterId + "qmin_2")) {
                                                        oi = Integer.parseInt(entry.getValue());
                                                    }
                                                }

                                                if (xi != 0 && yi != 0 && zi != 0 && oi != 0) {
                                                    String tx = getSetFlowCoefficientCommand(meterId, productType, "001111", xi, yi, zi, oi);
                                                    et_qn_1.setText("");
                                                    tv_qn_2.setText("");
                                                    et_qn_3.setText("");

                                                    et_qn2_1.setText("");
                                                    tv_qn2_2.setText("");
                                                    et_qn2_3.setText("");

                                                    et_qn1_1.setText("");
                                                    tv_qn1_2.setText("");
                                                    et_qn1_3.setText("");

                                                    et_qmin_1.setText("");
                                                    tv_qmin_2.setText("");
                                                    et_qmin_3.setText("");

                                                    et_qn_3.setText(String.valueOf(xi));
                                                    et_qn2_3.setText(String.valueOf(yi));
                                                    et_qn1_3.setText(String.valueOf(zi));
                                                    et_qmin_3.setText(String.valueOf(oi));
                                                    tv_qn_2.setText(String.valueOf(xi));
                                                    tv_qn2_2.setText(String.valueOf(yi));
                                                    tv_qn1_2.setText(String.valueOf(zi));
                                                    tv_qmin_2.setText(String.valueOf(oi));
                                                    BluetoothToolsMainActivity.data = "";
                                                    BluetoothToolsMainActivity.writeData(tx);
                                                } else {
                                                    CommonUtils.showToast(context, "没有保存出厂值！");
                                                }

                                            }
                                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                }).create();
                        dialog1.show();
                    }
                    break;
                case R.id.btn_read_flow:
                    //读取流量
                    meterId = et_meterId.getText().toString();
                    if (TextUtils.isEmpty(meterId)) {
                        meterId = "FFFFFFFF";
                    } else if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请输入正确的消火栓编号");
                        return;
                    }
                    number = "68" + productType + meterId + "0011110103901F00";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_adjust_consumption:
                    //调整流量
                    meterId = et_meterId.getText().toString();
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请先读取流量");
                    } else {
                        fixConsumption();
                    }
                    break;
                case R.id.btn_clear_flow:
                    //流量清零
                    meterId = et_meterId.getText().toString();
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请先读取流量");
                    }
                    number = "68" + productType + meterId + "0011113A03901F00";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_read_pressure_calibration:
                    //读取压力标定
                    number = "6848111111110011113B03805500";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_set_normal_point_calibration:
                    //常用点标定
                    if (TextUtils.isEmpty(et_set_common_pressure.getText())) {
                        CommonUtils.showToast(context, "请先输入设定值");
                        return;
                    } else if (et_set_common_pressure.getText().toString().contains(".")) {
                        if (et_set_common_pressure.getText().toString().length() - 1 - et_set_common_pressure.getText().toString().indexOf(".") != 2) {
                            CommonUtils.showToast(context, "设定值必须为两位小数");
                            return;
                        }
                    } else {
                        CommonUtils.showToast(context, "设定值必须为两位小数");
                        return;
                    }
                    int normal_point_calibration = Math.round(Float.valueOf(et_set_common_pressure.getText().toString()) * 100000);
                    StringBuilder sb = new StringBuilder();
                    sb.append("6848111111110011113B07805401");
                    sb.append(addZeroForNum(String.valueOf(Integer.toHexString(normal_point_calibration)), 8));
                    number = sb.toString();
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_set_zero_point_calibration:
                    //零点标定
                    number = "6848111111110011113B07805400" + "00000000";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 设置流量系数
     */
    private void setFlowCoefficient() {
        String x = et_qn_3.getText().toString().replace(" ", "");
        int xi;
        et_qn_3.setText(x);
        try {
            xi = Integer.parseInt(x);
            if (xi > 65535) {
                CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
                return;
            }
        } catch (Exception e) {
            CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
            return;
        }
        String y = et_qn2_3.getText().toString().replace(" ", "");
        int yi;
        et_qn2_3.setText(y);
        try {
            yi = Integer.parseInt(y);
            if (yi > 65535) {
                CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
                return;
            }
        } catch (Exception e) {
            CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
            return;
        }
        String z = et_qn1_3.getText().toString().replace(" ", "");
        int zi;
        et_qn1_3.setText(z);
        try {
            zi = Integer.parseInt(z);
            if (zi > 65535) {
                CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
                return;
            }
        } catch (Exception e) {
            CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
            return;
        }
        String o = et_qmin_3.getText().toString().replace(" ", "");
        int oi;
        et_qmin_3.setText(o);
        try {
            oi = Integer.parseInt(o);
            if (oi > 65535) {
                CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
                return;
            }
        } catch (Exception e) {
            CommonUtils.showToast(context, "请正确输入常用流量系数(<65535)");
            return;
        }
        String tx = getSetFlowCoefficientCommand(meterId, productType, "001111", xi, yi, zi, oi);
        et_qn_1.setText("");
        tv_qn_2.setText("");
        et_qn_3.setText("");

        et_qn2_1.setText("");
        tv_qn2_2.setText("");
        et_qn2_3.setText("");

        et_qn1_1.setText("");
        tv_qn1_2.setText("");
        et_qn1_3.setText("");

        et_qmin_1.setText("");
        tv_qmin_2.setText("");
        et_qmin_3.setText("");
        BluetoothToolsMainActivity.data = "";
        BluetoothToolsMainActivity.writeData(tx);
    }

    /**
     * 修改正反向流量
     */
    private void fixConsumption() {
        if (TextUtils.isEmpty(tv_consumption_positive.getText().toString()) || TextUtils.isEmpty(tv_consumption_reverse.getText().toString())) {
            CommonUtils.showToast(context, "请先读取流量");
            return;
        }
        String positiveFlow = et_consumption_positive.getText().toString();
        String reserveFlow = et_consumption_reverse.getText().toString();
        if (TextUtils.isEmpty(positiveFlow)) {
            CommonUtils.showToast(context, "请输入正向流量");
            return;
        }
        if (TextUtils.isEmpty(reserveFlow)) {
            CommonUtils.showToast(context, "请输入反向流量");
            return;
        }
        double flow_positive, flow_reserve;
        try {
            flow_positive = Double.valueOf(positiveFlow) / flowUnit_positive;
            flow_reserve = Double.valueOf(reserveFlow) / flowUnit_reserve;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String tx = getAdjustConsumptionCommand(meterId, productType, "001111", (int) flow_reserve, 0, (int) flow_positive);
        BluetoothToolsMainActivity.data = "";
        BluetoothToolsMainActivity.writeData(tx);
    }

    /**
     * 获取设置流量系数的指令
     *
     * @param meterId     表号
     * @param productType 产品类型
     * @param factoryCode 工厂代码
     * @param x           修常用
     * @param y           修0.2
     * @param z           修0.1
     * @param o           修最小
     * @return 返回指令
     */
    public String getSetFlowCoefficientCommand(String meterId, String productType, String factoryCode, int x, int y, int z, int o) {
        String r = "";
        try {
            String rx, ry, rz, ro;
            rx = addZeroForNum(Integer.toHexString(x), 4);
            rx = rx.substring(2, 4) + rx.substring(0, 2);
            ry = addZeroForNum(Integer.toHexString(y), 4);
            ry = ry.substring(2, 4) + ry.substring(0, 2);
            rz = addZeroForNum(Integer.toHexString(z), 4);
            rz = rz.substring(2, 4) + rz.substring(0, 2);
            ro = addZeroForNum(Integer.toHexString(o), 4);
            ro = ro.substring(2, 4) + ro.substring(0, 2);
            String cs = AnalysisUtils.getCSSum("68" + productType + meterId + factoryCode + "360CA0198800" + rx + ry + rz + ro, 0);
            r = ("68" + productType + meterId + factoryCode + "360CA0198800" + rx + ry + rz + ro + cs + "16").toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * 修正用量的指令拼接
     *
     * @param meterId            水表号
     * @param productType        产品类型（水表）
     * @param factoryCode        工厂代码
     * @param reverseConsumption 反向流量
     * @param blank              空白（原热表协议的参数，水表用不到）
     * @param consumption        正向流量
     * @return command           返回String类型指令
     */
    public String getAdjustConsumptionCommand(String meterId, String productType, String factoryCode, int reverseConsumption, int blank, int consumption) {
        String command = "";
        try {
            String rx, sx, qx;
            rx = Integer.toHexString(reverseConsumption);
            sx = Integer.toHexString(blank);
            qx = Integer.toHexString(consumption);
            String cs = AnalysisUtils.getCSSum("68" + productType + meterId + factoryCode + "3C0F901F00" +
                    addZeroForNum(rx, 8) + addZeroForNum(sx, 8) + addZeroForNum(qx, 8), 0);
            command = ("68" + productType + meterId + factoryCode + "3C0F901F00" + addZeroForNum(rx, 8) +
                    addZeroForNum(sx, 8) + addZeroForNum(qx, 8) + cs + "16").toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return command;
    }

    /**
     * 位数不够补零，超过长度截取
     *
     * @param str       数字字符串
     * @param strLength 需要的长度
     * @return str      返回String类型字符串
     */
    public String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            //长度不够在左边补0
            while (strLen < strLength) {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(0, "0");//左补0
//		    	sb.insert(strLen,"0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        } else {
            //长度够则截取最后的strLength位
            str = str.substring(strLen - strLength, strLength);
        }
        return str;
    }

    /**
     * 将EditText光标置于末尾
     *
     * @param charSequence EditText的内容
     */
    private void setCharSequence(CharSequence charSequence) {
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    /**
     * 设置正反向流量输入框输入格式
     *
     * @param editText 输入框对象
     * @param unit     单位（小数，与标准单位的数量级）
     * @param s        输入的内容
     */
    private void setInputType(EditText editText, double unit, CharSequence s) {
        if (unit != 0) {
            double zd;
            //允许小数的位数
            int i = MathUtils.getDecimalDigits(unit);
            if (unit < 1) {
                //如果直接输入小数点，前面自动补0
                if (s.toString().trim().equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }
                //除了小数开头不能为0，而且开头不允许连续出现0
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        s = s.toString().subSequence(0, s.length() - 1);
                        editText.setText(s);
                        //设置光标在末尾
                        editText.setSelection(s.length());
                    }
                }
                //限制小数位数
                if (s.toString().contains(".")) {
                    //包含小数点，限制小数位数不大于i个
                    if (s.length() - 1 - s.toString().indexOf(".") > i) {
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + i + 1);
                        editText.setText(s);
                        //设置光标在末尾
                        editText.setSelection(s.length());
                        CommonUtils.showToast(context, "最多输入" + i + "位小数");
                    }
                }
            } else {
                //开头不能输入0和小数点
                if (s.toString().trim().equals(".") || s.toString().trim().equals("0")) {
                    editText.setText("");
                }
            }
            //允许的最大流量
            double maxConsumption = 99999999 * unit;
            try {
                zd = Double.valueOf(s.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            //如果流量值大于99999999*unit
            if (zd > maxConsumption) {
                s = s.toString().subSequence(0, s.length() - 1);
                editText.setText(s);
                //设置光标在末尾
                editText.setSelection(s.length());
                //格式化不显示科学计数法，否则“99999999”会显示为“9.9999999E7”
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setGroupingUsed(false);
                CommonUtils.showToast(context, "流量值不能大于" + numberFormat.format(maxConsumption));
            }
        } else {
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                editText.setText("");
            }
            CommonUtils.showToast(context, "请先读取流量");
        }
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
