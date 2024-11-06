package com.company.intellihome;

import static com.company.intellihome.R.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import androidx.preference.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.util.concurrent.Executor;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private ImageView filtersButton;
    private ImageView searchButton;
    private ImageView profilePicButton;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private MapView mapView;
    private Fragment selectedFragment;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private File tempFiltersFile;
    private List<CheckBox> ListFilters;

    private HouseFilters_Fragment.HouseSearch houseSearch;

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
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupMap();
        setupButtonListeners();
        setupRecyclerView();
    }

    @Override
    //Maneja la selección de opciones en el menú de navegación
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Identifica la opción y abre el fragmento o actividad correspondiente
        if (id == R.id.nav_user) {
            selectedFragment = new User_Fragment();
            Toast.makeText(this, "Arrendatario seleccionado", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_owner) {
            selectedFragment = new Owner_Fragment();
            Toast.makeText(this, "Propietario seleccionado", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_lights) {
            Intent intent = new Intent(this, LightsActivity.class);
            startActivity(intent);
            finish();
        }

        //Carga el fragmento seleccionado
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        //Cierra el menú lateral si está abierto, de lo contraio, realiza la acción predeterminada
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        menuButton.setOnClickListener(v -> {
            Toast.makeText(this, "Menú seleccionado", Toast.LENGTH_SHORT).show();

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        filtersButton.setOnClickListener(v -> showFilterMenu());
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Buscando: " + query, Toast.LENGTH_SHORT).show();
                houseSearch = HouseFilters_Fragment.HouseSearch.newInstance(query);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(id.fragment_container, houseSearch);
                transaction.addToBackStack(null);
                transaction.commit();

            } else {
                Toast.makeText(HomeActivity.this, "Por favor, ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });
        profilePicButton.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Perfil seleccionado", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> items = new ArrayList<>();
        items.add("Añadir propiedad"); // Elemento para añadir propiedad
        for (int i = 0; i < 10; i++) {
            items.add("Item " + i);
        }
        SimpleAdapter adapter = new SimpleAdapter(items, this); // Pasa el contexto
        recyclerView.setAdapter(adapter);
    }

    //Muestra un menú de filtros en un PopupWindow
    protected void showFilterMenu() {
        Toast.makeText(this, "Filtros seleccionado", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View filterView = inflater.inflate(R.layout.filtrer_menu, null);

        //Crear una lista para almacenar los checkBox
        ListFilters = new ArrayList<>();
        List<String> selectedTextFilters = new ArrayList<>();

        //Tamaño del popup y configuración de los elementos de la interfaz
        final PopupWindow popupWindow = new PopupWindow(filterView, 900, 1500, true);

        //Configuración de los elementos de los filtros
        TextView priceValue = filterView.findViewById(id.priceValue);
        TextView personValue = filterView.findViewById(id.personText);
        SeekBar priceSeekBar = filterView.findViewById(id.priceSeekBar);
        SeekBar personSeekBar = filterView.findViewById(id.personSeekBar);
        Button applyFilters = filterView.findViewById(id.applyFiltersButton);
        //Amenidades
        AddAmenidades(filterView, ListFilters);

        applyFilters.setOnClickListener(v -> {
            Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show();

            selectedTextFilters.clear();

            CheckBoxSelections(selectedTextFilters, ListFilters);
            Log.d("FiltersSelections", "Filtros seleccionados: " + selectedTextFilters);
            int priceProgress = priceSeekBar.getProgress();
            //int personProgress = personSeekBar.getProgress();


            selectedFragment = new HouseFilters_Fragment(selectedTextFilters, priceSeekBar.getProgress(), personSeekBar.getProgress());

            getSupportFragmentManager().beginTransaction().replace(id.fragment_container, selectedFragment).commit();

        });

        personSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                personValue.setText("Cant. Personas: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceValue.setText("Precio: $" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        popupWindow.showAtLocation(findViewById(R.id.filters_button), Gravity.CENTER, 0, 0);
    }

    protected void CheckBoxSelections(List<String> selected, List<CheckBox> ListFilters) {
        for (CheckBox checkBox : ListFilters) {
            if (checkBox != null && checkBox.isChecked()) {
                selected.add(checkBox.getText().toString());
            }
        }
    }

    protected void AddAmenidades(View filter, List<CheckBox> list) {
        Log.d("HomeActivity", "Si entra para agregar");
        list.add(filter.findViewById(id.kitchenCheckBox));
        Log.d("HomeActivity", "Si agrego el primer id");
        list.add(filter.findViewById(id.airCheckBox));
        list.add(filter.findViewById(id.calefacciónCheckBox));
        list.add(filter.findViewById(id.gardenCheckBox));
        list.add(filter.findViewById(id.wifiCheckBox));
        list.add(filter.findViewById(id.tvCheckBox));
        list.add(filter.findViewById(id.washerdryerCheckBox));
        list.add(filter.findViewById(id.poolCheckBox));
        list.add(filter.findViewById(id.parrilaCheckBox));
        list.add(filter.findViewById(id.terraceCheckBox));
        list.add(filter.findViewById(id.gymCheckBox));
        list.add(filter.findViewById(id.garageCheckBox));
        list.add(filter.findViewById(id.securityCheckBox));
        list.add(filter.findViewById(id.suiteCheckBox));
        list.add(filter.findViewById(id.microwaveCheckBox));
        list.add(filter.findViewById(id.dishwasherCheckBox));
        list.add(filter.findViewById(id.coffemakerCheckBox));
        list.add(filter.findViewById(id.clothesCheckBox));
        list.add(filter.findViewById(id.commonareasCheckBox));
        list.add(filter.findViewById(id.bedCheckBox));
        list.add(filter.findViewById(id.cleanCheckBox));
        list.add(filter.findViewById(id.transportCheckBox));
        list.add(filter.findViewById(id.petsCheckBox));
        list.add(filter.findViewById(id.shopCheckBox));
        list.add(filter.findViewById(id.sueloradianteCheckBox));
        list.add(filter.findViewById(id.deskCheckBox));
        list.add(filter.findViewById(id.entertainmentCheckBox));
        list.add(filter.findViewById(id.chimeneaCheckBox));
        list.add(filter.findViewById(id.internetCheckBox));
        Log.d("HomeActivity", "Se agregaron todas las cosas");
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {
        private List<String> items;
        private HomeActivity homeActivity; // Para manejar la actividad

        public SimpleAdapter(List<String> items, HomeActivity activity) {
            this.items = items;
            this.homeActivity = activity; // Guarda la actividad
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
            holder.itemView.setOnClickListener(v -> {
                // Aquí puedes manejar el evento de clic para el elemento "Añadir propiedad"
                if (position == 0) {
                    Intent intent = new Intent(homeActivity, AddPropertyActivity.class);
                    homeActivity.startActivity(intent); // Inicia la nueva actividad
                }
            });
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
