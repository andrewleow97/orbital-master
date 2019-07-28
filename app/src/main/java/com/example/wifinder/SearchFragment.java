package com.example.wifinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class SearchFragment extends Fragment {

    private ListView myListView;

    private WifiManager mWifiManager;
    private SwipeRefreshLayout pullToRefresh;
    private List<ScanResult> mResults;
    private ArrayList<String> wifiname = new ArrayList<>();
    private ArrayList<String> signalstrength = new ArrayList<>();
    private ArrayList<String> securitystatus = new ArrayList<>();
    private ArrayList<String> wififrequency = new ArrayList<>();
    private ItemAdapter itemAdapter;
    private PopupAdapter popupAdapter;
    private ArrayList<String> networkpop = new ArrayList<>();
    private ArrayList<String> publicpop = new ArrayList<>();
    private ArrayList<String> signalpop = new ArrayList<>();
    private ArrayList<String> securitypop = new ArrayList<>();
    private ArrayList<String> frequencypop = new ArrayList<>();
    private ArrayList<String> ippop = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search, null);
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Resources res = getResources();
        pullToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanWifi();
                pullToRefresh.setRefreshing(false);
            }
        });

        myListView = (ListView) view.findViewById(R.id.myListVieW);

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(getContext(), "Wifi is disabled, please turn on WiFi", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }

        itemAdapter = new ItemAdapter(getContext(), wifiname, signalstrength, securitystatus);
        popupAdapter = new PopupAdapter(getContext(), networkpop, publicpop, signalpop, securitypop, frequencypop);
        myListView.setAdapter(itemAdapter);
        scanWifi();

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popupwindow, null);
                //myListView.setAdapter(popupAdapter);
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                //myListView.setAdapter(popupAdapter);
                TextView networktext = popupWindow.getContentView().findViewById(R.id.networkPop);
                TextView leveltext = popupWindow.getContentView().findViewById(R.id.signalPop);
                TextView bssidtext = popupWindow.getContentView().findViewById(R.id.publicnamePop);
                TextView securitytext = popupWindow.getContentView().findViewById(R.id.securityPop);
                TextView frequencytext = popupWindow.getContentView().findViewById(R.id.frequencyPop);
                //TextView iptext = (TextView) popupWindow.getContentView().findViewById(R.id.BSSIDPop);
                networktext.setText("Network Name: " + networkpop.get(i) + "\n");
                leveltext.setText("Signal Strength: " + signalpop.get(i) + "\n");
                bssidtext.setText("MAC Address: " + publicpop.get(i) + "\n");
                securitytext.setText("Security Type: " + securitypop.get(i) + "\n");
                frequencytext.setText("Frequency: " + frequencypop.get(i) + "\n");
                //iptext.setText(ippop.get(i));
                popupAdapter.notifyDataSetChanged();
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window token
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                dimBehind(popupWindow);
                String toasttext = "Currently connected to: " + mWifiManager.getConnectionInfo().getSSID() + "\n" + "IP Address: " + ippop.get(0);
                Toast.makeText(getContext(), toasttext, Toast.LENGTH_LONG).show();
                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }

        });
    }

    private void scanWifi() {
        wifiname.clear();
        signalstrength.clear();
        securitystatus.clear();
        wififrequency.clear();
        getActivity().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();
        Toast.makeText(getContext(), "Scanning for WiFi networks...", Toast.LENGTH_LONG).show();
    }

    private String Wifilevel(int x) {
        if (x >= -55) {
            return "Excellent";
        } else if (x >= -65) {
            return "Good";
        } else if (x >= -75) {
            return "Fair";
        } else {
            return "Poor";
        }
    }

    private int Wifipercentage(int x) {
        return Math.min(Math.max(2 * (x + 100), 0), 100);
    }

    private String shortSecurity (String str) {
        String z;
        z = str.split("]")[0];
        return z;
    }

    private static byte[] convert2Bytes(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };
        return addressBytes;
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mResults = mWifiManager.getScanResults();
            getActivity().unregisterReceiver(this);
            for (ScanResult scanResult : mResults) {
                if (scanResult.SSID.length() < 1) {
                    wifiname.add("[Hidden Network]");
                } else {
                    wifiname.add(scanResult.SSID);
                }
                //Using words
                //signalstrength.add(Wifilevel(scanResult.level));

                //Using percentages
                String temp = Integer.toString(Wifipercentage(scanResult.level)) + '%';
                signalstrength.add(temp);

                //Using raw data
                //signalstrength.add(Integer.toString(scanResult.level));
                securitystatus.add(shortSecurity(scanResult.capabilities) + ']');
                if (scanResult.frequency/1000 >= 5) {
                    wififrequency.add("5.0GHz");
                } else {
                    wififrequency.add("2.4GHz");
                }
                DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                byte[] ipAddress = convert2Bytes(dhcpInfo.serverAddress);
                //String apIpAddr = Inet4Address.getHostAddress();
                WifiInfo wifiInf = mWifiManager.getConnectionInfo();
                int ipad = wifiInf.getIpAddress();
                String ip = String.format("%d.%d.%d.%d", (ipad & 0xff),(ipad >> 8 & 0xff),(ipad >> 16 & 0xff),(ipad >> 24 & 0xff));
                networkpop.add(scanResult.SSID);
                publicpop.add(scanResult.BSSID);
                securitypop.add(scanResult.capabilities);
                frequencypop.add(scanResult.frequency + "Hz");
                signalpop.add(Integer.toString(scanResult.level) + "dBm");
                ippop.add(ip);
                itemAdapter.notifyDataSetChanged();
                itemAdapter.notifyDataSetChanged();
            }
        }
    };
}
