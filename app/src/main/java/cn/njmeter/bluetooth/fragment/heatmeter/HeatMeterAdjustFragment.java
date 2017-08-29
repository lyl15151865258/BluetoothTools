package cn.njmeter.bluetooth.fragment.heatmeter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
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
import android.widget.Toast;

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

public class HeatMeterAdjustFragment extends BaseFragment implements View.OnClickListener {

    private ScrollView scrollView_root;
    private EditText et_meterId, et_qn_1, et_qn_3, et_qn2_1, et_qn2_3, et_qn1_1, et_qn1_3, et_qmin_1, et_qmin_3, et_adjust;
    private TextView tv_qn_2, tv_qn2_2, tv_qn1_2, tv_qmin_2;
    private TextView tv_quantity_heat, tv_quantity_cold, tv_consumption_positive;
    private TextView tv_unit_heat, tv_unit_cold, tv_unit_positive;
    private EditText et_quantity_heat, et_quantity_cold, et_consumption_positive;
    private static String productType = ProductType.HEAT_METER;
    public Context context;
    public String meterId = "";
    public static double coldUnit, heatUnit, flowUnit;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_heatmeter_adjust_bluetooth, container, false);
        context = getContext();
        initView(view);
        coldUnit = 0;
        heatUnit = 0;
        flowUnit = 0;
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

        Button btn_read_parameter, btn_factory_reset, btn_set_flow_coefficient, btn_adjust_consumption, btn_read_flow, btn_clear_flow, btn_adjust;
        scrollView_root = view.findViewById(R.id.scrollView_root);
        et_meterId = view.findViewById(R.id.et_meterId);
        et_qn_1 = view.findViewById(R.id.et_qn_1);
        et_qn_3 = view.findViewById(R.id.et_qn_3);
        et_qn2_1 = view.findViewById(R.id.et_qn2_1);
        et_qn2_3 = view.findViewById(R.id.et_qn2_3);
        et_qn1_1 = view.findViewById(R.id.et_qn1_1);
        et_qn1_3 = view.findViewById(R.id.et_qn1_3);
        et_qmin_1 = view.findViewById(R.id.et_qmin_1);
        et_qmin_3 = view.findViewById(R.id.et_qmin_3);
        et_adjust = view.findViewById(R.id.et_adjust);
        et_quantity_heat = view.findViewById(R.id.et_quantity_heat);
        et_quantity_cold = view.findViewById(R.id.et_quantity_cold);
        et_consumption_positive = view.findViewById(R.id.et_consumption_positive);
        tv_qn_2 = view.findViewById(R.id.tv_qn_2);
        tv_qn2_2 = view.findViewById(R.id.tv_qn2_2);
        tv_qn1_2 = view.findViewById(R.id.tv_qn1_2);
        tv_qmin_2 = view.findViewById(R.id.tv_qmin_2);
        tv_quantity_heat = view.findViewById(R.id.tv_quantity_heat);
        tv_quantity_cold = view.findViewById(R.id.tv_quantity_cold);
        tv_consumption_positive = view.findViewById(R.id.tv_consumption_positive);
        tv_unit_heat = view.findViewById(R.id.tv_unit_heat);
        tv_unit_cold = view.findViewById(R.id.tv_unit_cold);
        tv_unit_positive = view.findViewById(R.id.tv_unit_positive);
        btn_read_parameter = view.findViewById(R.id.btn_read_parameter);
        btn_factory_reset = view.findViewById(R.id.btn_factory_reset);
        btn_set_flow_coefficient = view.findViewById(R.id.btn_set_flow_coefficient);
        btn_adjust_consumption = view.findViewById(R.id.btn_adjust_consumption);
        btn_read_flow = view.findViewById(R.id.btn_read_flow);
        btn_clear_flow = view.findViewById(R.id.btn_clear_flow);
        btn_adjust = view.findViewById(R.id.btn_adjust);
        btn_adjust_consumption.setOnClickListener(onClickListener);
        btn_factory_reset.setOnClickListener(onClickListener);
        btn_read_parameter.setOnClickListener(onClickListener);
        btn_set_flow_coefficient.setOnClickListener(onClickListener);
        btn_clear_flow.setOnClickListener(onClickListener);
        btn_read_flow.setOnClickListener(onClickListener);
        btn_adjust.setOnClickListener(onClickListener);

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
                setInputType(et_consumption_positive, flowUnit, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_quantity_heat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setInputType(et_consumption_positive, heatUnit, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_quantity_cold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setInputType(et_consumption_positive, coldUnit, s);
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
                    if (TextUtils.isEmpty(meterId)) {
                        meterId = "AAAAAAAA";
                    } else if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请输入8位出厂编号");
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
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                int xi = 0, yi = 0, zi = 0, oi = 0;
                                                SharedPreferences sharedPreferences = context.getSharedPreferences("setQnParam", Context.MODE_PRIVATE);
                                                Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
                                                for (Map.Entry<String, String> entry : map.entrySet()) {
                                                    if (entry.getKey().endsWith(meterId + "qn_2"))
                                                        xi = Integer.parseInt(entry.getValue());
                                                    if (entry.getKey().endsWith(meterId + "qn2_2"))
                                                        yi = Integer.parseInt(entry.getValue());
                                                    if (entry.getKey().endsWith(meterId + "qn1_2"))
                                                        zi = Integer.parseInt(entry.getValue());
                                                    if (entry.getKey().endsWith(meterId + "qmin_2"))
                                                        oi = Integer.parseInt(entry.getValue());
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
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                }).create();
                        dialog1.show();
                    }
                    break;
                case R.id.btn_read_flow:
                    //读取流量
                    //清空原有显示
                    tv_quantity_heat.setText("");
                    tv_quantity_cold.setText("");
                    tv_consumption_positive.setText("");
                    tv_unit_heat.setText("");
                    tv_unit_cold.setText("");
                    tv_unit_positive.setText("");
                    //热表不能通过通配表号读取，故需要先读取内部参数获取表号，再通过表号读取流量
                    if (TextUtils.isEmpty(meterId)) {
                        //读取内部参数获取表号
                        number = "68" + productType + "AAAAAAAA" + "AAAAAA1A039A2F00";
                    } else if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请输入正确的8位编码");
                        return;
                    } else {
                        //带着获取到的表号读表数据
                        number = "68" + productType + meterId + "AAAAAA0103901F00";
                    }
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_adjust_consumption:
                    //调整流量
                    if (TextUtils.isEmpty(tv_consumption_positive.getText().toString())) {
                        CommonUtils.showToast(context, "请先读取用量");
                    } else {
                        fixConsumption();
                    }
                    break;
                case R.id.btn_clear_flow:
                    //流量清零
                    meterId = et_meterId.getText().toString();
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请先读取用量");
                    }
                    number = "68" + productType + meterId + "0011113A03901F00";
                    checkCode = AnalysisUtils.getCSSum(number, 0);
                    command = number + checkCode + "16";
                    BluetoothToolsMainActivity.writeData(command);
                    break;
                case R.id.btn_adjust:
                    meterId = et_meterId.getText().toString();
                    if (meterId.length() != 8) {
                        CommonUtils.showToast(context, "请输入8位出厂编号");
                    } else {
                        SetAdjust();
                    }
                    break;
                default:
                    break;
            }
        }
    };


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
            //如果设定值大于99999999*unit
            if (zd > maxConsumption) {
                s = s.toString().subSequence(0, s.length() - 1);
                editText.setText(s);
                //设置光标在末尾
                editText.setSelection(s.length());
                //格式化不显示科学计数法，否则“99999999”会显示为“9.9999999E7”
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setGroupingUsed(false);
                CommonUtils.showToast(context, "设定值不能大于" + numberFormat.format(maxConsumption));
            }
        } else {
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                editText.setText("");
            }
            CommonUtils.showToast(context, "请先读取用量");
        }
    }


    private void SetAdjust() {
        String x = et_adjust.getText().toString().replace(" ", "");
        int xi;
        et_adjust.setText(x);
        try {
            xi = Integer.parseInt(x);
            if (xi > 1500) {
                Toast.makeText(context, "请正确输入调整值(<1500)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (xi < 500) {
                Toast.makeText(context, "请正确输入调整值(>=500)", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(context, "请正确输入调整值(<255)", Toast.LENGTH_SHORT).show();
            return;
        }
        String tx = adjust(meterId, productType, "001111", xi);
        BluetoothToolsMainActivity.data = "";
        BluetoothToolsMainActivity.writeData(tx);
    }

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

    public String adjust(String meterid, String producttypetx, String factorycode, int x) {
        String r = "";
        try {
            String rx;
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
            String cs = AnalysisUtils.getCSSum("68" + producttypetx + meterid + factorycode + "CD04901F" + rx, 0);
            r = ("68" + producttypetx + meterid + factorycode + "CD04901F" + rx + cs + "16").toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * 修改累计冷量、热量、水量
     */
    private void fixConsumption() {
        String totalHeat = et_quantity_heat.getText().toString();
        String totalCold = et_quantity_cold.getText().toString();
        String positiveFlow = et_consumption_positive.getText().toString();
        if (TextUtils.isEmpty(totalHeat)) {
            CommonUtils.showToast(context, "请输入累计热量");
            return;
        }
        if (TextUtils.isEmpty(totalCold)) {
            CommonUtils.showToast(context, "请输入累计冷量");
            return;
        }
        if (TextUtils.isEmpty(positiveFlow)) {
            CommonUtils.showToast(context, "请输入反向流量");
            return;
        }
        double quantity_heat, quantity_cold, totalConsumption;
        try {
            quantity_heat = Double.valueOf(totalHeat) / heatUnit;
            quantity_cold = Double.valueOf(totalCold) / coldUnit;
            totalConsumption = Double.valueOf(positiveFlow) / flowUnit;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        meterId = et_meterId.getText().toString();
        String tx = getFixedAmountCommand(meterId, productType, "001111", (int) quantity_heat, (int) quantity_cold, (int) totalConsumption);
        BluetoothToolsMainActivity.data = "";
        BluetoothToolsMainActivity.writeData(tx);
    }

    /**
     * 修正用量的指令拼接
     *
     * @param meterId          热表号
     * @param productType      产品类型（热表）
     * @param factoryCode      工厂代码
     * @param quantity_heat    累计热量
     * @param quantity_cold    累计冷量
     * @param totalConsumption 累计流量
     * @return command         返回String类型指令
     */
    public String getFixedAmountCommand(String meterId, String productType, String factoryCode, int quantity_heat, int quantity_cold, int totalConsumption) {
        String command = "";
        try {
            String rx, sx, qx;
            rx = Integer.toHexString(quantity_heat);
            sx = Integer.toHexString(quantity_cold);
            qx = Integer.toHexString(totalConsumption);
            String cs = AnalysisUtils.getCSSum("68" + productType + meterId + factoryCode + "3C0F901F00" + addZeroForNum(rx, 8) + addZeroForNum(sx, 8) + addZeroForNum(qx, 8), 0);
            command = ("68" + productType + meterId + factoryCode + "3C0F901F00" + addZeroForNum(rx, 8) + addZeroForNum(sx, 8) + addZeroForNum(qx, 8) + cs + "16").toUpperCase();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //移除软键盘弹出收回监听（解决隐藏状态栏以及导航栏导致和软件盘冲突的解决所添加的监听）
        if (onGlobalLayoutListener != null) {
            getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }
}
