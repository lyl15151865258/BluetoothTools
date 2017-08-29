package cn.njmeter.bluetooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.njmeter.bluetooth.R;
import cn.njmeter.bluetooth.bean.MeterRecord;
import cn.njmeter.bluetooth.widget.MyListView;

public class MeterDataAdapter extends BaseAdapter {

    private Context context;
    private List<MeterRecord> list;
    private LayoutInflater inflater;

    public MeterDataAdapter(Context context, List<MeterRecord> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MeterRecord meterRecord = (MeterRecord) this.getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_list_read_meter_data, parent, false);
            viewHolder.mTextl1 = convertView.findViewById(R.id.text_l1);
            viewHolder.mTextl2 = convertView.findViewById(R.id.text_l2);
            viewHolder.lv_data = convertView.findViewById(R.id.lv_data);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mTextl1.setText(meterRecord.getList1());
        viewHolder.mTextl2.setText(meterRecord.getList2());
        List<MeterRecord.MeterData> meterDataList = meterRecord.getMeterDataList();
        MeterDataListAdapter meterDataListAdapter = new MeterDataListAdapter(context, meterDataList);
        viewHolder.lv_data.setAdapter(meterDataListAdapter);
//        ListViewUtil.setListViewHeightBasedOnChildren(viewHolder.lv_data, meterDataListAdapter);
        return convertView;
    }

    private class ViewHolder {
        private TextView mTextl1;
        private TextView mTextl2;
        private MyListView lv_data;
    }

}