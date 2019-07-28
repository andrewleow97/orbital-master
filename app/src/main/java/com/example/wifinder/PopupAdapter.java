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

public class PopupAdapter extends BaseAdapter {
    LayoutInflater pInflater;
    ArrayList<String> namePopup;
    ArrayList<String> publicnamePopup;
    ArrayList<String> signalPopup;
    ArrayList<String> securityPopup;
    ArrayList<String> frequencyPopup;
    @Override
    public int getCount() {
        return namePopup.size();
    }

    @Override
    public Object getItem(int position) {
        return namePopup.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public PopupAdapter(Context c, ArrayList<String> n, ArrayList<String> pn, ArrayList<String> sp, ArrayList<String> sep, ArrayList<String> fp) {
        namePopup = n;
        publicnamePopup = pn;
        signalPopup = sp;
        securityPopup = sep;
        frequencyPopup = fp;
        pInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public View getView(int position, View view, ViewGroup parent) {
        View v = pInflater.inflate(R.layout.popupwindow, null);
        TextView networkPop = v.findViewById(R.id.networkPop);
        TextView signalPop = v.findViewById(R.id.signalPop);
        TextView securityPop = v.findViewById(R.id.securityPop);
        TextView frequencyPop = v.findViewById(R.id.frequencyPop);
        TextView publicnamePop = v.findViewById(R.id.publicnamePop);

        String network = namePopup.get(position);
        String signal = signalPopup.get(position);
        String secure = securityPopup.get(position);
        String frequency = frequencyPopup.get(position);
        String publicname = publicnamePopup.get(position);

        networkPop.setText(network);
        signalPop.setText(signal);
        securityPop.setText(secure);
        frequencyPop.setText(frequency);
        publicnamePop.setText(publicname);

        return v;
    }
}