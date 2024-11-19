package com.company.intellihome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 101;
    private static final int REQUEST_IMAGE_CAMERA = 102;

    private HomeActivity filtersFeatures = new HomeActivity();
    private Entities entities = new Entities();

    private MapView mapView;
    private EditText coordinatesEditText, priceInput, rulesInput, availabilityInput;
    private TextView selectedFeatures, selectedRules, selectedPhotosText;
    private Button uploadPhotosButton;
    private List<CheckBox> ListFilters = new ArrayList<>();                             //Se va a usar
    private List<String> rulesList = new ArrayList<>();
    private List<String> selectedItems = new ArrayList<>();                             //Se va a usar
    private List<Uri> selectedPhotosUris = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentMarker;
    private GestureDetector gestureDetector;
    private Calendar startDate, endDate;
    private ImageView backspaceImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_property_activity);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Inicializa los elementos del layout
        backspaceImage = findViewById(R.id.backspace_image2);
        mapView = findViewById(R.id.mapView);
        coordinatesEditText = findViewById(R.id.coordinates_edit_text);
        priceInput = findViewById(R.id.price_input);
        rulesInput = findViewById(R.id.rules_input);
        availabilityInput = findViewById(R.id.availability_input);
        selectedFeatures = findViewById(R.id.selected_features);
        selectedRules = findViewById(R.id.selected_rules);
        selectedPhotosText = findViewById(R.id.selected_photos_text);
        uploadPhotosButton = findViewById(R.id.upload_photos_button);

        Button removeFeatureButton = findViewById(R.id.remove_feature_button);
        Button removeRuleButton = findViewById(R.id.remove_rule_button);
        Button savePropertyButton = findViewById(R.id.save_property_button); // Nuevo botón

        setupMap();
        setupFilters();
        checkPermissions();

        //Configuración para que se devuelva a la pantalla de Login
        backspaceImage.setOnClickListener(v -> {
            Intent intent = new Intent(AddPropertyActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        availabilityInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showAvailabilityPicker();
            }
            return false;
        });

        rulesInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showAddRuleDialog();
            }
            return false;
        });

        priceInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showPriceInputDialog();
            }
            return false;
        });

        removeFeatureButton.setOnClickListener(v -> showRemoveFeaturesDialog());
        removeRuleButton.setOnClickListener(v -> showRemoveRulesDialog());

        // Asignar la función saveProperty() al botón de guardar propiedad
        savePropertyButton.setOnClickListener(v -> saveProperty());

        uploadPhotosButton.setOnClickListener(v -> showPhotoSelectionDialog());

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                GeoPoint touchedPoint = (GeoPoint) mapView.getProjection()
                        .fromPixels((int) e.getX(), (int) e.getY());
                addMarkerAtLocation(touchedPoint);
            }
        });
    }

    //Función para guardar la propiedad
    private void saveProperty() {
        new Thread(() -> {
            try {
                // Genera un ID único para la propiedad
                String propertyId = UUID.randomUUID().toString();
                String coordinates = coordinatesEditText.getText().toString();
                String price = priceInput.getText().toString();
                String availability = availabilityInput.getText().toString();

                // Construir JSON con la información de la propiedad
                JSONObject propertyData = new JSONObject();
                propertyData.put("type", "property"); // Agregar el tipo de solicitud
                propertyData.put("id", propertyId);
                propertyData.put("coordinates", coordinates);
                propertyData.put("price", price);
                propertyData.put("laws", new JSONArray(rulesList));
                propertyData.put("availability", availability);
                propertyData.put("characteristics", new JSONArray(selectedItems));

                // Enviar la información al servidor
                Socket socket = new Socket(entities.Host, 1717);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(propertyData.toString());

                // Recibir la respuesta del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                runOnUiThread(() -> {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
                });

                socket.close();
                sendPropertyImages(propertyId);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al enviar la propiedad.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //Función para enviar las imágenes una por una al servidor
    private void sendPropertyImages(String propertId) {
        for (Uri photoUri : selectedPhotosUris) {
            new Thread(() -> {
                try {
                    //Construir JSON para la imagen
                    JSONObject imageData = new JSONObject();
                    imageData.put("type", "savePhotoProperty");
                    imageData.put("propertyId", propertId);
                    imageData.put("photo", encodeImageToBase64(photoUri));

                    //Enviar la imagen al servidor
                    Socket socket = new Socket(entities.Host, 1717);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(imageData.toString());

                    //Recibir la respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    runOnUiThread(() -> Log.d("PhotoUpload", "Respuesta del servidor: " + response));
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Log.d("PhotoUpload", "Error al enviar la imagen."));
                }
            }).start();
        }
    }

    //Función para codificar una iagen desde una Uri a una cadena Base64
    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);  // Reducimos la calidad para evitar un JSON demasiado grande
            byte[] imageBytes = outputStream.toByteArray();
            return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);  // Sin saltos de línea
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Función para verificar los permismos
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    //Función para configurar el mapa
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        mapView.setOnTouchListener((v, event) -> {
            if (event.getPointerCount() == 2) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    //Función para verificar la Ubicación
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            showCurrentLocation();
        }
    }

    //Función para mostrar la ubicación
    @SuppressLint("MissingPermission")
    private void showCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapView.getController().setZoom(15);
                mapView.getController().setCenter(currentLocation);
                addMarkerAtLocation(currentLocation);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Función para agregar el marcador de la ubicación
    private void addMarkerAtLocation(GeoPoint point) {
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }

        currentMarker = new Marker(mapView);
        currentMarker.setPosition(point);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setInfoWindow(null);
        mapView.getOverlays().add(currentMarker);

        String coordinates = point.getLatitude() + ", " + point.getLongitude();
        coordinatesEditText.setText(coordinates);

        mapView.invalidate();
    }

    //Función para mostrar el calendario
    private void showAvailabilityPicker() {
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startDate.set(year, month, dayOfMonth);
            showEndDatePicker();
        }, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
        startDatePicker.setTitle("Selecciona la fecha de inicio");
        startDatePicker.show();
    }

    //Función para elegir la fecha final
    private void showEndDatePicker() {
        DatePickerDialog endDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endDate.set(year, month, dayOfMonth);
            updateAvailabilityInput();
        }, endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));
        endDatePicker.setTitle("Selecciona la fecha de fin");
        endDatePicker.show();
    }

    //Función para actualizar la disponibilidad
    private void updateAvailabilityInput() {
        String start = startDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (startDate.get(Calendar.MONTH) + 1) + "/" +
                startDate.get(Calendar.YEAR);
        String end = endDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (endDate.get(Calendar.MONTH) + 1) + "/" +
                endDate.get(Calendar.YEAR);
        availabilityInput.setText("Disponible del " + start + " al " + end);
    }

    //Función para mostrar la elección de la foto
    private void showPhotoSelectionDialog() {
        String[] options = {"Seleccionar de la galería", "Tomar una foto"};
        new AlertDialog.Builder(this)
                .setTitle("Añadir Foto")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        openCamera();
                    }
                }).show();
    }

    //Función para abrir la Galería
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    //Función para abrir la cámara
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAMERA);
    }

    // Método que maneja el resultado de actividades para seleccionar imágenes de la galería o capturadas por la cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Verifica si la solicitud proviene de la selección de imagen de la galería cámara
        if (requestCode == REQUEST_IMAGE_GALLERY) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                selectedPhotosUris.add(selectedImageUri);
                updateSelectedPhotos();
            }
        } else if (requestCode == REQUEST_IMAGE_CAMERA) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri photoUri = getImageUriFromBitmap(photo);
            if (photoUri != null) {
                selectedPhotosUris.add(photoUri);
                updateSelectedPhotos();
            }
        }
    }

    // Método para guardar un Bitmap como un archivo de imagen en el almacenamiento externo y devolver su URI
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        Uri imageUri = null;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();

        //Configura los metadatos para la imagen a guardar
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Nueva_Foto_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");

        try {
            //Inserta una nueva entrada en MediaStore y obtiene la URI
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                OutputStream outStream = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                if (outStream != null) {
                    outStream.close();
                }
            }
        } catch (IOException e) {
            Log.e("AddPhoto", "Error al guardar la imagen: " + e.getMessage());
        }
        return imageUri;
    }

    //Función para actualizar la selección de fotos
    private void updateSelectedPhotos() {
        StringBuilder photosText = new StringBuilder("Fotos seleccionadas:\n");
        for (Uri uri : selectedPhotosUris) {
            photosText.append("- ").append(uri.getLastPathSegment()).append("\n");
        }
        selectedPhotosText.setText(photosText.toString());
    }

    //Función para configurar los filtros
    private void setupFilters() {
        View filterView = findViewById(android.R.id.content);

        //Verifica si el objeto filtersFeatures ha sido incializado
        if (filtersFeatures != null) {
            filtersFeatures.AddAmenidades(filterView, ListFilters);
            selectedItems.clear();

            //Actualiza la lista de elementos seleccionados
            filtersFeatures.CheckBoxSelections(selectedItems, ListFilters);
        } else {
            Log.d("FiltersSeñections", "filtersFeatures es null.");
        }

        //Configura el botón para aplicar los filtros
        Button applyFilters = filterView.findViewById(R.id.acceptFiltrers);
        applyFilters.setOnClickListener(v -> {
            Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show();

            //Limpia y actualiza la lista de elementos seleccionados
            selectedItems.clear();
            filtersFeatures.CheckBoxSelections(selectedItems, ListFilters);
            updateSelectedFeatures();
        });
    }

    //Función para actualizar la selección de amenidades
    private void updateSelectedFeatures() {
        StringBuilder featuresText = new StringBuilder("Amenidades seleccionadas:\n");
        for (String feature : selectedItems) {
            featuresText.append("- ").append(feature).append("\n");
        }
        selectedFeatures.setText(featuresText.toString());
    }

    //Función para mostrar y agregar una regla
    private void showAddRuleDialog() {
        final EditText input = new EditText(this);
        input.setHint("Ingrese la ley");
        new AlertDialog.Builder(this)
                .setTitle("Agregar Ley")
                .setView(input)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String rule = input.getText().toString();
                    if (!rule.isEmpty()) {
                        rulesList.add(rule);
                        updateSelectedRules();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //Función para actualizar la selección de reglas
    private void updateSelectedRules() {
        StringBuilder rulesText = new StringBuilder("Leyes seleccionadas:\n");
        for (String rule : rulesList) {
            rulesText.append("- ").append(rule).append("\n");
        }
        selectedRules.setText(rulesText.toString());
    }

    //Función para mostrar la eliminación de amenidades
    private void showRemoveFeaturesDialog() {
        String[] selectedArray = selectedItems.toArray(new String[0]);
        boolean[] checkedItems = new boolean[selectedArray.length];

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Amenidades")
                .setMultiChoiceItems(selectedArray, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    for (int i = checkedItems.length - 1; i >= 0; i--) {
                        if (checkedItems[i]) {
                            selectedItems.remove(selectedArray[i]);
                        }
                    }
                    updateSelectedFeatures();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //Función para mostrar la eliminacióm de reglas
    private void showRemoveRulesDialog() {
        String[] rulesArray = rulesList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[rulesArray.length];

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Regulaciones")
                .setMultiChoiceItems(rulesArray, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    for (int i = checkedItems.length - 1; i >= 0; i--) {
                        if (checkedItems[i]) {
                            rulesList.remove(rulesArray[i]);
                        }
                    }
                    updateSelectedRules();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //Función para mostrar y agregar el precio por noche
    private void showPriceInputDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Ingrese el precio en USD");

        new AlertDialog.Builder(this)
                .setTitle("Precio por noche")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String price = input.getText().toString();
                    if (!price.isEmpty()) {
                        priceInput.setText("$" + price + " USD");
                    } else {
                        Toast.makeText(this, "El precio no puede estar vacío.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
