package cn.njmeter.bluetooth.bean;

import java.util.List;

/**
 * Created by LIYuliang on 2017/7/17 0017.
 * 蓝牙读表数据封装类
 */

public class MeterRecord {

    private String list1;
    private String list2;
    private List<MeterData> meterDataList;

    public MeterRecord() {
        super();
    }

    public MeterRecord(String list1, String list2, List<MeterData> meterDataList) {
        super();
        this.list1 = list1;
        this.list2 = list2;
        this.meterDataList = meterDataList;
    }


    public String getList1() {
        return list1;
    }

    public void setList1(String list1) {
        this.list1 = list1;
    }

    public String getList2() {
        return list2;
    }

    public void setList2(String list2) {
        this.list2 = list2;
    }

    public List<MeterData> getMeterDataList() {
        return meterDataList;
    }

    public void setMeterDataList(List<MeterData> meterDataList) {
        this.meterDataList = meterDataList;
    }

    public class MeterData {

        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}