package com.bram.belajarosmdroid.BU;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.bram.belajarosmdroid.R;
import com.bram.belajarosmdroid.Services.AlarmReceive;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private org.osmdroid.views.MapView map = null;
    private MyLocationNewOverlay myLocationNewOverlay;
    private CompassOverlay mCompassOverlay;
    final GeoPoint absenSpot = new GeoPoint(-8.1509529, 113.7154793);
    private Button btnAbsen;
    Polygon oPolygon;
    private TextView txtDistance;
    private Spinner spinner;
    private static final String[] paths = {"Non shift masuk", "Non shift pulang", "Pagi masuk", "Pagi pulang",
            "Siang masuk", "Siang pulang", "Malam masuk", "Malam Pulang", "Lembur masuk", "Lembur Pulang"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        btnAbsen = findViewById(R.id.btnAbsen);
        spinner = findViewById(R.id.spinnerWaktu);
        txtDistance = findViewById(R.id.txtDistance);
        btnAbsen.setEnabled(false);

        //----- Optional : Background onLocationChanged---//
        if(alarmManager == null){
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceive.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000,
                    pendingIntent);
        }
        //----- Optional : Background onLocationChanged---//

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(absenSpot);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_person_pin_circle_24));
        startMarker.setTitle("Rs Dr Soebandi");
        map.getOverlays().add(startMarker);

        oPolygon = new Polygon(map);
        final double radius = 225;
        ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
        for (float f = 0; f < 360; f += 1){
            circlePoints.add(new GeoPoint(-8.1509529 , 113.7154793 ).destinationPoint(radius, f));
        }

        oPolygon.setPoints(circlePoints);
        oPolygon.setTitle("Absen Point");
        map.getOverlays().add(oPolygon);

        GpsMyLocationProvider provider = new GpsMyLocationProvider(this);
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
//        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);

        final IMapController mapController = map.getController();

        //mylocation
        myLocationNewOverlay = new MyLocationNewOverlay(provider, map);
        myLocationNewOverlay.enableFollowLocation();
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                if (myLocationNewOverlay != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapController.setZoom(19.5);
                            mapController.animateTo(myLocationNewOverlay.getMyLocation());
                            mapController.setCenter(myLocationNewOverlay.getMyLocation());
                            btnAbsen.setEnabled(true);

                            new Timer().scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    if (myLocationNewOverlay != null) {
                                        txtDistance.setText(String.valueOf(distanceGeoPoints(myLocationNewOverlay.getMyLocation(), absenSpot)));
                                        btnAbsen.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (distanceGeoPoints(myLocationNewOverlay.getMyLocation(), absenSpot)<225){
                                                    Toast.makeText(getApplicationContext(), "Anda telah berhasil login !", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }, 0, 1000);
                        }
                    });
                }
            }
        });

        btnAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Jarak = " + distanceGeoPoints(myLocationNewOverlay.getMyLocation(), absenSpot), Toast.LENGTH_SHORT).show();
            }
        });

        map.getOverlays().add(myLocationNewOverlay);
        requestPermissionIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        // === coba polyline === //
        //dipindahkan ke void navigasi()
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void navigasi(GeoPoint geo) {
        RoadManager roadManager = new OSRMRoadManager(this);

        final GeoPoint endPoint = new GeoPoint(-8.1509529, 113.7154793);
        Log.d("jarak", "jarak : " + distanceGeoPoints(geo, endPoint));
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();

        geoPoints.add(geo);
        geoPoints.add(endPoint);

        Road road = roadManager.getRoad(geoPoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }

    public double distanceGeoPoints(GeoPoint geoPoint01, GeoPoint geoPoint02) {
        double lat1 = geoPoint01.getLatitudeE6() / 1E6;
        double lng1 = geoPoint01.getLongitudeE6() / 1E6;
        double lat2 = geoPoint02.getLatitudeE6() / 1E6;
        double lng2 = geoPoint02.getLongitudeE6() / 1E6;
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
        return geopointDistance;
    }

    private void requestPermissionIfNecessary(String[] permissions) {
        ArrayList<String> permissionToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //permission not granted
                permissionToRequest.add(permission);
            }
        }
        if (permissionToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}