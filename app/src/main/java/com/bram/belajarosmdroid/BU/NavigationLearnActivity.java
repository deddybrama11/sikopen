package com.bram.belajarosmdroid.BU;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Spinner;

import com.bram.belajarosmdroid.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class NavigationLearnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_learn);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        MapView map = findViewById(R.id.map_nav);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(-8.1509529,113.7154793);
        IMapController mapController = map.getController();
        mapController.setZoom(13);
        mapController.setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_person_pin_circle_24));
        startMarker.setTitle("Rs Dr Soebandi");
        map.getOverlays().add(startMarker);
        map.invalidate();

        //------------Tutorial 1
        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        GeoPoint endPoint = new GeoPoint(-8.1668201,113.7237407);
        waypoints.add(startPoint);
        waypoints.add(endPoint);
        Road road = roadManager.getRoad(waypoints);

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);


        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_default);
        for (int i = 0; i<road.mNodes.size(); i ++){
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            //memberi icon pada bubble
            nodeMarker.setIcon(nodeIcon);
            //memberi judul pada bubble
            nodeMarker.setTitle("Step  "+i);
            //memberi deskripsi instruksi penjelasan
            nodeMarker.setSnippet(node.mInstructions);
            //memberi subdeskripsi lama perjalanan
            nodeMarker.setSubDescription(Road.getLengthDurationText(this,node.mLength,node.mDuration));
            map.getOverlays().add(nodeMarker);

        }
        map.invalidate();
    }
}