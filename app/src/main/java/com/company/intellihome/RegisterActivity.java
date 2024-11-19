package com.company.intellihome;

import static java.util.Base64.*;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Base64;
import java.util.Calendar;
import java.util.Arrays;    // Para usar Arrays
import java.util.List;      // Para usar List

import android.util.Log;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;

    private Entities entities = new Entities();
    private Button profilePicButton;
    private EditText nameInputText;
    private EditText lastNameInputText;
    private EditText birthDateInput;
    private EditText phoneNumberInputText;
    private EditText addressInputText;
    private EditText emailInputText;
    private EditText nicknameInputText;
    private EditText passwordInputText;
    private EditText confirmPasswordInputText;
    private EditText hobbiesInputText;
    private Spinner houseTypeComBox;
    private Button nextPayButton;
    private ImageView profilePicView;
    private ImageView backspaceImage;
    private String selectedHouseType = "";

    // Lista de apodos prohibidos
    private final List<String> prohibitedNicknames = Arrays.asList("palabra1", "palabra2", "palabra3"); // Añade más palabras prohibidas aquí
    // Lista de nombres y apellidos prohibidos
    private final List<String> prohibitedNames = Arrays.asList("palabra1", "palabra2", "palabra3"); // Añade más palabras prohibidas aquí

    // ActivityResultLauncher para la cámara y la galería
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();  // Verifica si `Extras` no es nulo
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        if (bitmap != null) {  // Verifica si el bitmap no es nulo
                            Bitmap compressedBitmap = compressBitmap(bitmap, 800);
                            profilePicView.setImageBitmap(compressedBitmap);
                        } else {
                            Toast.makeText(this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No se recibieron datos de imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();

                    if (selectedImageUri != null) {
                        try {
                            //Cargar y comprimir la imagen antes de mostrarla
                            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), selectedImageUri);

                            //Comprimir la imagen manteniendo calidad aceptable
                            Bitmap compressedBitmap = compressBitmap(originalBitmap, 800);
                            profilePicView.setImageBitmap(compressedBitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forzar la aplicación a modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.register_activity);

        // Inicialización de los elementos de la interfaz de usuario
        backspaceImage = findViewById(R.id.backspace_image);
        profilePicButton = findViewById(R.id.profilePicButton);
        profilePicView = findViewById(R.id.imageViewRegister);
        nameInputText = findViewById(R.id.nameInputText);
        lastNameInputText = findViewById(R.id.lastNameInputText);
        birthDateInput = findViewById(R.id.birthDateInput);
        phoneNumberInputText = findViewById(R.id.phoneNumberInputText);
        addressInputText = findViewById(R.id.addressInputText);
        emailInputText = findViewById(R.id.emailInputText);
        nicknameInputText = findViewById(R.id.nicknameInputText);
        passwordInputText = findViewById(R.id.passwordInputText);
        confirmPasswordInputText = findViewById(R.id.confirmPasswordInputText);
        hobbiesInputText = findViewById(R.id.hobbiesInputText);
        houseTypeComBox = findViewById(R.id.houseTypeComBox);
        nextPayButton = findViewById(R.id.nextPayButton);

        // Verificar permisos
        checkPermissions();

        //Configuración para que se devuelva a la pantalla de Login
        backspaceImage.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Configuración del DatePicker para el campo de fecha de nacimiento
        birthDateInput.setOnClickListener(v -> showDatePickerDialog());

        // Configuración para seleccionar imagen o tomar foto
        profilePicButton.setOnClickListener(v -> showImagePickerDialog());

        // Lista de opciones para el Spinner
        String[] tipoCasas = {"Minimalista", "Rustica", "Moderna", "Mansion"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipoCasas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        houseTypeComBox.setAdapter(adapter);

        // Manejar la selección del Spinner
        houseTypeComBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHouseType = (String) parent.getItemAtPosition(position);
                Toast.makeText(RegisterActivity.this, "El tipo de casa seleccionado es: " + selectedHouseType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona nada
            }
        });

        // Configurar el botón "nextPayButton" para enviar los datos al servidor
        nextPayButton.setOnClickListener(v -> sendRegistrationData());
    }

    // Mostrar el diálogo para seleccionar imagen o tomar foto
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una opción");
        String[] options = {"Cámara", "Galería"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Abrir la cámara
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(cameraIntent);  // Usar ActivityResultLauncher
            } else {
                // Abrir la galería
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                galleryLauncher.launch(Intent.createChooser(galleryIntent, "Selecciona una imagen"));  // Usar ActivityResultLauncher
            }
        });
        builder.show();
    }

    // Verificar permisos
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Mostrar el DatePickerDialog
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthDateInput.setText(selectedDate);
                }, year, month, day);

        // Establecer la fecha máxima seleccionable como la fecha actual
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    //Función para enviar los datos para el registro
    private void sendRegistrationData() {
        String name = nameInputText.getText().toString();
        String lastName = lastNameInputText.getText().toString();
        String birthDate = birthDateInput.getText().toString();
        String phoneNumber = phoneNumberInputText.getText().toString();
        String address = addressInputText.getText().toString();
        String email = emailInputText.getText().toString();
        String nickname = nicknameInputText.getText().toString();
        String password = passwordInputText.getText().toString();
        String confirmPassword = confirmPasswordInputText.getText().toString();
        String hobbies = hobbiesInputText.getText().toString();
        List<String> noRent = new ArrayList<>();
        List<String> inRent = new ArrayList<>();

        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que el apodo no contenga palabras prohibidas
        if (isNicknameProhibited(nickname)) {
            Toast.makeText(this, "El apodo contiene contenido inapropiado. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que el nombre no contenga palabras prohibidas
        if (isNameProhibited(name)) {
            Toast.makeText(this, "El nombre contiene contenido inapropiado. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que el apellido no contenga palabras prohibidas
        if (isLastNameProhibited(lastName)) {
            Toast.makeText(this, "El apellido contiene contenido inapropiado. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contraseña
        String passwordErrorMessage = validatePassword(password);
        if (passwordErrorMessage != null) {
            Toast.makeText(this, passwordErrorMessage, Toast.LENGTH_LONG).show();
            return;
        }

        //Convertir la imagen a Base64
        String imageBase64 = "";
        //Guarda la imagen con el 'username' antes de enviarla al servidor
        if(profilePicView.getDrawable() != null) {
            Bitmap originalBitmap = ((BitmapDrawable) profilePicView.getDrawable()).getBitmap();
            Bitmap compressedBitmap = compressBitmap(originalBitmap, 200);

            //Convertir a Base 64
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] byteArray = stream.toByteArray();
            imageBase64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
        } else {
            Toast.makeText(this, "No se ha cargado ninguna imagen de perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        //Crear el objeto JSON
        JSONObject registrationData = new JSONObject();
        try {
            registrationData.put("name", name);
            registrationData.put("lastName", lastName);
            registrationData.put("birthDate", birthDate);
            registrationData.put("phoneNumber", phoneNumber);
            registrationData.put("address", address);
            registrationData.put("email", email);
            registrationData.put("username", nickname);
            registrationData.put("password", password);
            registrationData.put("confirmPassword", confirmPassword);
            registrationData.put("hobbies", hobbies);
            registrationData.put("houseType", selectedHouseType);
            registrationData.put("type", "register");

            JSONArray noRentArray = new JSONArray(noRent);
            JSONArray inRentArray = new JSONArray(inRent);
            registrationData.put("noRent", noRentArray);
            registrationData.put("inRent", inRentArray);
            registrationData.put("profilePicture", imageBase64);
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error creando JSON para los datos de registro", e);
            return;
        }

        new Thread(() -> {
            try {
                Socket socket = new Socket(entities.Host, 1717);
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
                 );

                writer.write(registrationData.toString());
                writer.flush();

                writer.close();
                socket.close();

                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registro con éxito", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                Log.e("RegisterActivity", "Error enviando los datos al servidor", e);
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Error al enviar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //Método para comprimir el Bitmap
    private Bitmap compressBitmap(Bitmap original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        //Si la imagen es más pequeña que el tamaño máximo, no la redimensionamos
        if (ratio >= 1.0f) return original;
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    // Método para validar el apodo
    private boolean isNicknameProhibited(String nickname) {
        for (String prohibited : prohibitedNicknames) {
            if (nickname.toLowerCase().contains(prohibited.toLowerCase())) {
                return true; // El apodo es inapropiado
            }
        }
        return false; // El apodo es apropiado
    }

    // Método para validar el nombre
    private boolean isNameProhibited(String name) {
        for (String prohibited : prohibitedNames) {
            if (name.toLowerCase().contains(prohibited.toLowerCase())) {
                return true; // El nombre es inapropiado
            }
        }
        return false; // El nombre es apropiado
    }

    // Método para validar el apellido
    private boolean isLastNameProhibited(String lastName) {
        for (String prohibited : prohibitedNames) {
            if (lastName.toLowerCase().contains(prohibited.toLowerCase())) {
                return true; // El apellido es inapropiado
            }
        }
        return false; // El apellido es apropiado
    }

    // Método para validar la contraseña
    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "La contraseña debe contener al menos un número.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "La contraseña debe contener al menos una letra minúscula.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!password.matches(".*[@#$%^&+=!.?].*")) {
            return "La contraseña debe contener al menos un carácter especial.";
        }
        return null;  // La contraseña es válida
    }
}
