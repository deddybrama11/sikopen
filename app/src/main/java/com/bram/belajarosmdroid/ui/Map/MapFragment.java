package com.bram.belajarosmdroid.ui.Map;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bram.belajarosmdroid.API.BaseApiService;
import com.bram.belajarosmdroid.API.Shift;
import com.bram.belajarosmdroid.API.Utils;
import com.bram.belajarosmdroid.Preferences;
import com.bram.belajarosmdroid.R;
import com.bram.belajarosmdroid.Services.AlarmReceive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private org.osmdroid.views.MapView map = null;
    private MyLocationNewOverlay myLocationNewOverlay;
    private CompassOverlay mCompassOverlay;
    //    final GeoPoint absenSpot = new GeoPoint(-8.1509529, 113.7154793);
    private Button btnAbsen, btnAbsenPulang;
    Polygon oPolygon;
    private TextView txtDistance, txtTanggalJam, txtNIP, txtNama;
    private Spinner spinner;
    Marker startMarker;
    BaseApiService mApi;
    ArrayList<Shift> arrayList = new ArrayList<>();
    ArrayList<GeoPoint> arrGeo = new ArrayList<>();
    View llProgressBar;
    GeoPoint absenSpot;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        //-------------------------------------------------------------------------------------//
        mApi = Utils.getAPI();

        final Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        btnAbsen = root.findViewById(R.id.btnAbsen);
        btnAbsenPulang = root.findViewById(R.id.btnAbsenPulang);
        spinner = root.findViewById(R.id.spinnerWaktu);
        txtDistance = root.findViewById(R.id.txtDistance);
        txtTanggalJam = root.findViewById(R.id.textTanggal);
        txtNIP = root.findViewById(R.id.textNip);
        txtNama = root.findViewById(R.id.textNama);
        llProgressBar = root.findViewById(R.id.llProgressBar);
        llProgressBar.setVisibility(View.VISIBLE);
        btnAbsen.setEnabled(false);
        btnAbsenPulang.setEnabled(false);

        txtNIP.setText(": " + Preferences.getKeyEmployeId(getActivity()));
        txtNama.setText(": " + Preferences.getNama(getActivity()));

        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        txtTanggalJam.setText(currentDateTimeString);

        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtTanggalJam.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        //----- Optional : Background onLocationChanged-----//
        if (alarmManager == null) {
            alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(ctx, AlarmReceive.class);
            pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000,
                    pendingIntent);
        }

        //---------- Set Map -------------//
        map = root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        setAllShift();
        setMesin();

        //---------------------------- Agar bisa dijalankan di main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //-----------------------------

        return root;
    }

    public double distanceGeoPoints(GeoPoint geoPoint01) {
        List<Double> arrJarak = new ArrayList<>();
        for (int i = 0; i < arrGeo.size(); i++) {
            double lat1 = geoPoint01.getLatitudeE6() / 1E6;
            double lng1 = geoPoint01.getLongitudeE6() / 1E6;
            double lat2 = arrGeo.get(i).getLatitudeE6() / 1E6;
            double lng2 = arrGeo.get(i).getLongitudeE6() / 1E6;
            double earthRadius = 3958.75;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double dist = earthRadius * c;
            int meterConversion = 1609;
            double geopointDistance = dist * meterConversion;
            arrJarak.add(geopointDistance);
        }
        return Collections.min(arrJarak);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    private void setAllShift() {
        mApi.getAllShift().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for (int i = 0; i < jsonArray.length(); i++) {

                            Integer jamkerja_id = jsonArray.getJSONObject(i).getInt("jamkerja_id");
                            Integer hcr_group_id = jsonArray.getJSONObject(i).getInt("hcr_group_id");
                            Integer shift = jsonArray.getJSONObject(i).getInt("shift");
                            String jmasuk = jsonArray.getJSONObject(i).getString("jmasuk");
                            String jpulang = jsonArray.getJSONObject(i).getString("jpulang");
                            Integer flexi = jsonArray.getJSONObject(i).getInt("flexi");
                            Integer jefektif = jsonArray.getJSONObject(i).getInt("jefektif");
                            String ket = jsonArray.getJSONObject(i).getString("ket");
                            Boolean jamkerja_aktif = jsonArray.getJSONObject(i).getBoolean("jamkerja_aktif");

                            arrayList.add(new Shift(jamkerja_id, hcr_group_id, shift, jmasuk, jpulang,
                                    flexi, jefektif, ket, jamkerja_aktif));
                        }
                        ArrayAdapter<Shift> adapter = new ArrayAdapter<Shift>(getActivity(),
                                android.R.layout.simple_spinner_item, arrayList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        Log.d("mappa", "onResponse: sukses " + response.body());
                        Log.d("mappa", "length array: " + jsonArray.length());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("mappa", "onResponse: gk sukses");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setAllShift();
                Log.d("mappa", "onResponse: failure");
            }
        });
    }

    private void setMesin() {
        mApi.getMesin().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            absenSpot = new GeoPoint(Double.parseDouble(jsonArray.getJSONObject(i).getString("mesin_latitude")), Double.parseDouble(jsonArray.getJSONObject(i).getString("mesin_longitude")));
                            arrGeo.add(absenSpot);
                            startMarker = new Marker(map);
                            startMarker.setPosition(absenSpot);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_person_pin_circle_24));
                            startMarker.setSubDescription("lat : " + jsonArray.getJSONObject(i).getString("mesin_latitude") + ", long : " + jsonArray.getJSONObject(i).getString("mesin_longitude"));
                            startMarker.setTitle(jsonArray.getJSONObject(i).getString("mesin_nama"));

                            Polygon oPolygon = new Polygon(map);
                            final double radius = 225;
                            ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
                            for (float f = 0; f < 360; f += 1) {
                                circlePoints.add(absenSpot.destinationPoint(radius, f));
                            }
                            oPolygon.setPoints(circlePoints);
                            map.getOverlays().add(startMarker);
                            map.getOverlays().add(oPolygon);
                            Log.d("map", "mesin : " + jsonArray.getJSONObject(i).getString("mesin_nama"));
                        }
                        Log.d("map", "mesin: " + jsonArray);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    setMap();
                } else {

                }
                btnAbsen.setEnabled(true);
                btnAbsenPulang.setEnabled(true);
                llProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setMesin();
                btnAbsen.setEnabled(false);
                btnAbsenPulang.setEnabled(false);
                llProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setMap() {
        GpsMyLocationProvider provider = new GpsMyLocationProvider(getActivity());
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);

        final IMapController mapController = map.getController();

        //mylocation
        myLocationNewOverlay = new MyLocationNewOverlay(provider, map);
        myLocationNewOverlay.enableFollowLocation();
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                if (myLocationNewOverlay != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapController.setZoom(19.5);
                            mapController.animateTo(myLocationNewOverlay.getMyLocation());
                            mapController.setCenter(myLocationNewOverlay.getMyLocation());

                            new Timer().scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    if (isVisible()) {
                                        if (myLocationNewOverlay != null) {
                                            txtDistance.setText(": " + String.valueOf((int) distanceGeoPoints(myLocationNewOverlay.getMyLocation())) + " m");
                                            btnAbsen.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (distanceGeoPoints(myLocationNewOverlay.getMyLocation()) < 800000) {
                                                        llProgressBar.bringToFront();
                                                        llProgressBar.setVisibility(View.VISIBLE);
                                                        btnAbsen.setVisibility(View.GONE);
                                                        absenRequest(1, Preferences.getKeyEmployeId(getActivity()), arrayList.get(spinner.getSelectedItemPosition()).getShift(),
                                                                arrayList.get(spinner.getSelectedItemPosition()).getJmasuk(), arrayList.get(spinner.getSelectedItemPosition()).getJpulang(),
                                                                arrayList.get(spinner.getSelectedItemPosition()).getJefektif().toString(), 1, String.valueOf(myLocationNewOverlay.getMyLocation().getLatitude()),
                                                                String.valueOf(myLocationNewOverlay.getMyLocation().getLongitude()));

                                                    } else {
                                                        Toast.makeText(getActivity(), "Anda tidak di sekitar kantor, Harap absen disekitar kantor", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            btnAbsenPulang.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (distanceGeoPoints(myLocationNewOverlay.getMyLocation()) < 800000) {
                                                        llProgressBar.bringToFront();
                                                        llProgressBar.setVisibility(View.VISIBLE);
                                                        btnAbsen.setVisibility(View.GONE);
                                                        absenRequest(2, Preferences.getKeyEmployeId(getActivity()), arrayList.get(spinner.getSelectedItemPosition()).getShift(),
                                                                arrayList.get(spinner.getSelectedItemPosition()).getJmasuk(), arrayList.get(spinner.getSelectedItemPosition()).getJpulang(),
                                                                arrayList.get(spinner.getSelectedItemPosition()).getJefektif().toString(), 1, String.valueOf(myLocationNewOverlay.getMyLocation().getLatitude()),
                                                                String.valueOf(myLocationNewOverlay.getMyLocation().getLongitude()));
                                                    } else {
                                                        Toast.makeText(getActivity(), "Anda tidak di sekitar kantor, Harap absen disekitar kantor", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }, 0, 1000);
                        }
                    });
                }
            }
        });
        map.getOverlays().add(myLocationNewOverlay);
    }

    private void absenRequest(Integer cc_type, Integer employee_id, Integer shift, String jmasuk,
                              String jpulang, String jefektif, Integer mesin_id, String lat, String lng) {
        Log.d("absen", "absenRequest: " + cc_type + ", " + employee_id + ", " + shift + ", " + ", " + jmasuk + ", " + jpulang + ", " + jefektif + ", " + mesin_id + ", " + lat + ", " + lng + " ");
        mApi.absenRequest(cc_type, employee_id, shift, jmasuk, jpulang, jefektif, mesin_id, lat, lng).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("absen", "onResponse: absen berhasil");
                    Toast.makeText(getActivity(), "Login berhasil !", Toast.LENGTH_SHORT).show();
                    llProgressBar.setVisibility(View.GONE);
                    btnAbsen.setVisibility(View.VISIBLE);
                    btnAbsenPulang.setVisibility(View.VISIBLE);
                } else {
                    Log.d("absen", "onResponse: absen gagal");
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        if (jsonObject.getString("message").equals("User already checked-in today")) {
                            Toast.makeText(getActivity(), "Anda sudah absen hari ini", Toast.LENGTH_LONG).show();
                            llProgressBar.setVisibility(View.GONE);
                            btnAbsen.setVisibility(View.VISIBLE);
                            btnAbsenPulang.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(getActivity(), "Terjadi kesalahan : Pastikan anda telah memilih shift dan tidak salah memilih absen", Toast.LENGTH_LONG).show();
                            llProgressBar.setVisibility(View.GONE);
                            btnAbsen.setVisibility(View.VISIBLE);
                            btnAbsenPulang.setVisibility(View.VISIBLE);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("absen", "onResponse: absen failure");
                Toast.makeText(getActivity(), "Tolong cek koneksi anda!", Toast.LENGTH_SHORT).show();
                
                llProgressBar.setVisibility(View.VISIBLE);
                btnAbsen.setVisibility(View.GONE);
                btnAbsenPulang.setVisibility(View.GONE);
            }
        });
    }

    //
//    private void checkIfNetworkLocationAvailable() {
//        boolean gps_enabled = false;
//        boolean network_enabled = false;
//
//        LocationManager lm = (LocationManager) getActivity()
//                .getSystemService(Context.LOCATION_SERVICE);
//
//        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        Log.d("map", "checkIfNetworkLocationAvailable gps: "+gps_enabled);
//        Log.d("map", "checkIfNetworkLocationAvailable network: "+network_enabled);
//
//        Location net_loc = null, gps_loc = null, finalLoc = null;
////
////        if (gps_loc != null && net_loc != null) {
////
////            //smaller the number more accurate result will
////            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
////                finalLoc = net_loc;
////            else
////                finalLoc = gps_loc;
////        }
//
//    }
}