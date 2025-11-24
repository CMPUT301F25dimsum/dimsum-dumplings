package com.example.lotteryapp.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotteryapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap map;
    private ArrayList<GeoPoint> locations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        updateMarkers();
    }

    public void updateLocations(ArrayList<GeoPoint> newLocations) {
        if (newLocations != null) {
            this.locations.clear();
            this.locations.addAll(newLocations);
            if (map != null) {
                updateMarkers();
            }
        }
    }

    private void updateMarkers() {
        if (map == null) return;
        map.clear();

        if (locations != null && !locations.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            for (GeoPoint geoPoint : locations) {
                if (geoPoint != null) {
                    LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng));
                    builder.include(latLng);
                    hasPoints = true;
                }
            }

            if (hasPoints) {
                LatLngBounds bounds = builder.build();
                int padding = 100; // offset from edges of the map in pixels
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        }
    }

    // Mandatory lifecycle methods for MapView
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}
