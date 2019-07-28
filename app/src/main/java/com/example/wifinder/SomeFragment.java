package com.example.wifinder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;
import static com.example.wifinder.Constants.HEAT_MAP_FILE;

public class SomeFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlay mOverlay;

    private Button addValButton;
    private Button saveButton;
    private Button resetButton;

    private LatLng myLatLng = new LatLng(0,0);
    private FusedLocationProviderClient fusedLocationClient;

    private ArrayList<WeightedLatLng> fileList = new ArrayList<>();

    private HashMap<LatLng, Integer> heatMapVal = new HashMap<>();

    int[] colors = {
            Color.rgb(255, 0, 0),    // red
            Color.rgb(255,255,0), // yellow
            Color.rgb(102, 225, 0), // green
    };

    float[] startPoints = {
            0.2f, 0.5f, 1f
    };

    Gradient gradient = new Gradient(colors, startPoints);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        addValButton = v.findViewById(R.id.buttonAddVal);
        saveButton = v.findViewById(R.id.buttonSave);
        resetButton = v.findViewById(R.id.buttonReset);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        addValButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveAndSaveLastKnownLocation();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHeatMap();
                Log.d(TAG, "onClick: step1 ok");
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        moveToLastKnownLocation();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void moveToLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            myLatLng = new LatLng(5, 5);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 1000));

                            ConnectivityManager connManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        }
                    }
                });
    }

    private void moveAndSaveLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            myLatLng = new LatLng(5, 5);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 1000));

                            ConnectivityManager connManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (mWifi.isConnected()) {
                                saveCoordSignalStrength(myLatLng);
                            }
                        }
                    }
                });
    }

    private void saveCoordSignalStrength(LatLng latLng) {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        int wifipercent = Wifipercentage(wifiManager.getConnectionInfo().getRssi());
        String dp5 = "###.#####";
        DecimalFormat decimalFormat5 = new DecimalFormat(dp5);
        heatMapVal.put(latLng, wifipercent);
        String toWrite = decimalFormat5.format(latLng.latitude) + "," + decimalFormat5.format(latLng.longitude) + "," + wifipercent + "\n";

        try {
            FileOutputStream fileOutputStream = getActivity().openFileOutput(HEAT_MAP_FILE , Context.MODE_APPEND);
            fileOutputStream.write(toWrite.getBytes());
            fileOutputStream.close();

            Toast.makeText(getContext(), "Info saved" + wifipercent,
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "saveCoordSignalStrength: " + toWrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetMap() {
        try {
            mOverlay.remove();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "resetMap: map removed");
        String empty = "";
        try {
            FileOutputStream fileOutputStream = getActivity().openFileOutput(HEAT_MAP_FILE, MODE_PRIVATE);
            fileOutputStream.write(empty.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int Wifipercentage(int rssi) {
        return Math.min(Math.max(2 * (rssi + 100), 0), 100);

    }

    private boolean fileExist (String filename) {
        File file = getActivity().getFileStreamPath(filename);
        return file.exists();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (!fileExist(HEAT_MAP_FILE)) {
            File file = new File(getActivity().getFilesDir(), HEAT_MAP_FILE);
        }

        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        moveToLastKnownLocation();

    }

    public void readFile() {
        try {
            FileInputStream fileInputStream = getActivity().openFileInput(HEAT_MAP_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            Log.d(TAG, "readFile: open ok");

            String lines;

            while ((lines = bufferedReader.readLine()) != null) {
                String[] arrOfStr = lines.split(",", 3);
                
                LatLng tempLatLng = new LatLng(Double.parseDouble(arrOfStr[0]), Double.parseDouble(arrOfStr[1]));
                Integer tempInt = Integer.parseInt(arrOfStr[2]) * 10;
                fileList.add(new WeightedLatLng(tempLatLng, tempInt));
            }
            fileInputStream.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void addHeatMap() {

        ArrayList<LatLng> list = new ArrayList<>();

        // Get the data: latitude/longitude positions of points.
        readFile();

        //ArrayList<Pair<LatLng, Integer>> listFromFile = fileList;
        if (heatMapVal.isEmpty()) {
            Toast.makeText(getContext(), "Please add some data points", Toast.LENGTH_SHORT);
            return;
        }

        for (Map.Entry temp : heatMapVal.entrySet()) {
            list.add((LatLng) temp.getKey());
        }

        // Create a heat map tile provider, passing it the latlng points.

        heatmapTileProvider = new HeatmapTileProvider.Builder()
                .weightedData(fileList)
                .radius(50)
                .gradient(gradient)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
    }
}
