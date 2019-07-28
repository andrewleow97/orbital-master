package com.example.wifinder;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    ArrayList<String> wifilist;
    ArrayList<String> signalstrength;
    ArrayList<String> securitystatus;

    public ItemAdapter(Context c, ArrayList<String> w, ArrayList<String> si, ArrayList<String> se) {
        wifilist = w;
        signalstrength = si;
        securitystatus = se;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return wifilist.size();
    }

    @Override
    public Object getItem(int position) {
        return wifilist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.mylistview, null);
        TextView networkView = v.findViewById(R.id.networkView);
        TextView signalView = v.findViewById(R.id.signalView);
        //TextView securityView = v.findViewById(R.id.securityView);

        String network = wifilist.get(position);
        String signal = signalstrength.get(position);
        //String secure = securitystatus.get(position);

        networkView.setText(network);
        signalView.setText(signal);
        //securityView.setText(secure);

        return v;
    }
}
