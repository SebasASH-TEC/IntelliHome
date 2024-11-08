package com.company.intellihome;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONObject;

import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private Entities entities = new Entities();

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.login_activity);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        forgotPasswordText = findViewById(R.id.forgot_password);

        setupLoginButton();
        setupRegisterButton();
        setupForgotPasswordText();
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                sendLoginDataToServer(username, password);
            } else {
                Toast.makeText(LoginActivity.this, "Ingrese un nombre de usuario y contraseña válidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLoginDataToServer(String username, String password) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(entities.Host, 1717);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                JSONObject loginData = new JSONObject();
                loginData.put("type", "login");
                loginData.put("username", username);
                loginData.put("password", password);

                writer.println(loginData.toString());
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String serverResponse = responseBuilder.toString();

                writer.close();
                reader.close();
                socket.close();

                runOnUiThread(() -> handleServerResponse(serverResponse));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error al conectar con el servidor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleServerResponse(String response) {
        Log.d("LoginActivity", "Server Response: " + response.trim());

        // Verificar si la respuesta es válida
        if (response == null || response.trim().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Dividir la respuesta en partes
            String[] parts = response.trim().split(":");
            String status = parts[0]; // "SUCCESS" o "FAIL"

            if (status.equals("SUCCESS")) {
                String username = parts[1]; // Obtener el nombre de usuario
                Toast.makeText(LoginActivity.this, "Login exitoso para " + username, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                String errorMessage = "Fallo de login para " + parts[1]; // Obtener el nombre de usuario
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error procesando la respuesta del servidor", e);
            Toast.makeText(LoginActivity.this, "Error procesando la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupForgotPasswordText() {
        forgotPasswordText.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar Contraseña");

        final EditText emailInput = new EditText(this);
        builder.setView(emailInput);
        builder.setMessage("Ingrese su correo electrónico:");

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = emailInput.getText().toString();
            if (!email.isEmpty()) {
                sendForgotPasswordRequest(email);
            } else {
                Toast.makeText(LoginActivity.this, "Por favor, ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendForgotPasswordRequest(String email) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(entities.Host, 1717);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                JSONObject requestData = new JSONObject();
                requestData.put("type", "forgot_password");
                requestData.put("email", email);

                writer.println(requestData.toString());
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String serverResponse = responseBuilder.toString();

                writer.close();
                reader.close();
                socket.close();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(serverResponse.trim());
                        if (jsonResponse.has("status") && jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(LoginActivity.this, "Se ha enviado un correo electrónico con la nueva contraseña.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error al conectar con el servidor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
