package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomBeaconAdapter extends BaseAdapter {
    List<BeaconModel> list;
    Context context;

    //Constructor
    public CustomBeaconAdapter(Context context, List<BeaconModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_beacon, null);

        //Display the three attributes : @Mac_BLE / RSSI / Url
        // mac -- > tv_name
        // rssi -- > tv_rssi
        // url -- > tv_url

        TextView tvName =view.findViewById(R.id.tv_name);
        TextView tvRssi = view.findViewById(R.id.tv_rssi);
        TextView tvUrl = view.findViewById(R.id.tv_url);

        tvName.setText(list.get(position).getBleAddress());
        tvRssi.setText(String.valueOf(list.get(position).getRssi()));
        tvUrl.setText(list.get(position).getUrl());

        return view;
    }
}
