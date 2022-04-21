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


        //I will display the :  mac / url/ Rssi
        // now we will display  mac -- > tv_name
        //                      url -- > tv_url
        //                      rssi -- > tv_rssi

        TextView tvName =view.findViewById(R.id.tv_name);
        TextView tvUrl = view.findViewById(R.id.tv_url);
        TextView tvRssi = view.findViewById(R.id.tv_rssi);

        tvName.setText(list.get(position).getBleAddress());
        tvUrl.setText(list.get(position).getUrl());
        tvRssi.setText(String.valueOf(list.get(position).getRssi()));

        return view;
    }
}
