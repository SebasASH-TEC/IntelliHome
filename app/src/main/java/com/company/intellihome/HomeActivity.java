package com.company.intellihome;

import static com.company.intellihome.R.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Location;
import android.os.Bundle;
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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;
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

    androidx.biometric.BiometricPrompt biometricPrompt;
    androidx.biometric.BiometricPrompt.PromptInfo promptInfo;
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

        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(getApplicationContext());
        switch (biometricManager.canAuthenticate()) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                Toast.makeText(this, "Biométricos disponibles", Toast.LENGTH_SHORT).show();
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No hay hardware biométrico", Toast.LENGTH_SHORT).show();
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Hardware biométrico no disponible", Toast.LENGTH_SHORT).show();
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No hay credenciales biométricos configuradas", Toast.LENGTH_SHORT).show();
                break;

        }
        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new androidx.biometric.BiometricPrompt(this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errorString) {
                super.onAuthenticationError(errorCode, errorString);
                Toast.makeText(getApplicationContext(), "Error de autenticación", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Autenticación exitosa", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }
        });
        promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder().setTitle("Autenticación biométrica")
                .setDescription("Autenticate para continuar").setDeviceCredentialAllowed(true).build();

        biometricPrompt.authenticate(promptInfo);


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
        int width = (int) (300 * getResources().getDisplayMetrics().density);

        //Tamaño del popup y configuración de los elementos de la interfaz
        final PopupWindow popupWindow = new PopupWindow(filterView, 900, 1500, true);

        //Configuración de los elementos de los filtros
        TextView priceValue = filterView.findViewById(id.priceValue);
        TextView personValue = filterView.findViewById(id.personText);
        SeekBar priceSeekBar = filterView.findViewById(id.priceSeekBar);
        SeekBar personSeekBar = filterView.findViewById(id.personSeekBar);
        Button applyFilters = filterView.findViewById(id.applyFiltersButton);
        //Amenidades
        CheckBox kitchen = filterView.findViewById(id.kitchenCheckBox);
        CheckBox acondicionador = filterView.findViewById(id.airCheckBox);
        CheckBox calefaccion = filterView.findViewById(id.calefacciónCheckBox);
        CheckBox jardin = filterView.findViewById(id.gardenCheckBox);
        CheckBox wifi = filterView.findViewById(id.wifiCheckBox);
        CheckBox tv = filterView.findViewById(id.tvCheckBox);
        CheckBox lavadora = filterView.findViewById(id.washerdryerCheckBox);
        CheckBox piscina = filterView.findViewById(id.poolCheckBox);
        CheckBox parrilla = filterView.findViewById(id.parrilaCheckBox);
        CheckBox terraza = filterView.findViewById(id.terraceCheckBox);
        CheckBox gimnasio = filterView.findViewById(id.gymCheckBox);
        CheckBox garaje = filterView.findViewById(id.garageCheckBox);
        CheckBox seguridad = filterView.findViewById(id.securityCheckBox);
        CheckBox habitacion = filterView.findViewById(id.suiteCheckBox);
        CheckBox microondas = filterView.findViewById(id.microwaveCheckBox);
        CheckBox lavavajillas = filterView.findViewById(id.dishwasherCheckBox);
        CheckBox coffemaker = filterView.findViewById(id.coffemakerCheckBox);
        CheckBox ropa = filterView.findViewById(id.clothesCheckBox);
        CheckBox areascomunes = filterView.findViewById(id.commonareasCheckBox);
        CheckBox cama = filterView.findViewById(id.bedCheckBox);
        CheckBox limpieza = filterView.findViewById(id.cleanCheckBox);
        CheckBox transporte = filterView.findViewById(id.transportCheckBox);
        CheckBox mascotas = filterView.findViewById(id.petsCheckBox);
        CheckBox restaurantes = filterView.findViewById(id.shopCheckBox);
        CheckBox sueloradiante = filterView.findViewById(id.sueloradianteCheckBox);
        CheckBox escritorio = filterView.findViewById(id.deskCheckBox);
        CheckBox entretenimiento = filterView.findViewById(id.entertainmentCheckBox);
        CheckBox chimenea = filterView.findViewById(id.chimeneaCheckBox);
        CheckBox internet = filterView.findViewById(id.internetCheckBox);

        applyFilters.setOnClickListener(v -> Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show());

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
