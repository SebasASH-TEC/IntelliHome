package com.company.intellihome;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.preference.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ImageView menuButton;
    private ImageView filtersButton;
    private ImageView searchButton;
    private ImageView profilePicButton;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuración de OSMdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        menuButton = findViewById(R.id.menu_button);
        filtersButton = findViewById(R.id.filters_button);
        searchButton = findViewById(R.id.search_button);
        profilePicButton = findViewById(R.id.profilePicButton);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.recycler_view);
        mapView = findViewById(R.id.mapView);

        setupMap();
        setupButtonListeners();
        setupRecyclerView();
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            showCurrentLocation();
        }
    }

    private void showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult.addOnSuccessListener(this, location -> {
                if (location != null) {
                    GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mapView.getController().setZoom(15);
                    mapView.getController().setCenter(currentLocation);

                    Marker marker = new Marker(mapView);
                    marker.setPosition(currentLocation);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapView.getOverlays().add(marker);

                    addCircleOverlay(currentLocation, 500); // Rango de 500 metros
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addCircleOverlay(GeoPoint center, double radius) {
        Polygon circle = new Polygon();
        circle.setStrokeColor(0x800000FF);
        circle.setFillColor(0x200000FF);

        List<GeoPoint> circlePoints = new ArrayList<>();
        int points = 360;
        for (int i = 0; i < points; i++) {
            double angle = i * 2 * Math.PI / points;
            double lat = center.getLatitude() + (radius / 111320) * Math.sin(angle);
            double lon = center.getLongitude() + (radius / (111320 * Math.cos(Math.toRadians(center.getLatitude())))) * Math.cos(angle);
            circlePoints.add(new GeoPoint(lat, lon));
        }

        circle.setPoints(circlePoints);
        mapView.getOverlays().add(circle);
        mapView.invalidate();
    }

    private void setupButtonListeners() {
        menuButton.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Menú seleccionado", Toast.LENGTH_SHORT).show());
        filtersButton.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Filtros seleccionados", Toast.LENGTH_SHORT).show());
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Buscando: " + query, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Por favor, ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });
        profilePicButton.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Perfil seleccionado", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add("Item " + i);
        }
        RecyclerView.Adapter adapter = new SimpleAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    public static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {

        private List<String> items;

        public SimpleAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
