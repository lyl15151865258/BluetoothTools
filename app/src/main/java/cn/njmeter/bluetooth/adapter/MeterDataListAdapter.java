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

public class MeterDataListAdapter extends BaseAdapter {

    private Context context;
    private List<MeterRecord.MeterData> list;
    private LayoutInflater inflater;

    public MeterDataListAdapter(Context context, List<MeterRecord.MeterData> list) {
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
        MeterRecord.MeterData meterData = (MeterRecord.MeterData) this.getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_list_read_meter_data_list, parent, false);
            viewHolder.tv_item = (TextView) convertView.findViewById(R.id.tv_item);
            viewHolder.tv_value = (TextView) convertView.findViewById(R.id.tv_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_item.setText(meterData.getKey());
        viewHolder.tv_value.setText(meterData.getValue());
        return convertView;
    }

    public static class ViewHolder {
        private TextView tv_item;
        private TextView tv_value;
    }

}