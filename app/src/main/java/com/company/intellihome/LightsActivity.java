package com.company.intellihome;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class LightsActivity extends AppCompatActivity {

    private ImageView houseMap;

    private Entities entities = new Entities();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Habilita el modo de pantalla completa
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lights);

        //Configura el layout para que ocupe toda la pantalla y ajuste su padding según los bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        houseMap = findViewById(R.id.houseMap);

        houseMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Detecta solo el evento de toque inicial (ACTION_DOWN)
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    checkRoomTapped(x, y);
                }
                return true;
            }
        });
    }

    //Función para verificar si el toque corresponde a una habitación y alterna la luz en consecuencia
    private void checkRoomTapped(float x, float y) {
        String roomLight="";
        if (isInSala(x, y)) {
            toogleLight("Sala");
            roomLight="Living room";
        } else if (isInCuarto1(x, y)) {
            toogleLight("Cuarto 1");
            roomLight="Room 1";
        } else if (isInCuarto2(x, y)) {
            toogleLight("Cuarto 2");
            roomLight="Room 2";
        } else if (isInCuarto3(x, y)) {
            toogleLight("Cuarto 3");
            roomLight="Room 3";
        } else if (isBaño1(x, y)) {
            toogleLight("Baño 1");
            roomLight="Bathroom 1";
        } else if (isInBaño2(x, y)) {
            toogleLight("Baño 2");
            roomLight="Bathroom 2";
        } else if (isInCocina(x, y)) {
            toogleLight("Cocina");
            roomLight="Kitchen";
        } else if (isInLimpieza(x, y)) {
            toogleLight("Cuarto de Lavado");
            roomLight="Laundry room";
        } else if (isInGaraje(x, y)) {
            toogleLight("Garaje");
            roomLight="Garage";
        }else{
            toogleLight("No seleccionó una habitación valida");
            roomLight="None";
        }

        String finalRoomLight = roomLight;
        new Thread(() -> {
            try {
                Socket socket = new Socket(entities.Host, 1717);
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                JSONObject lightData = new JSONObject();
                lightData.put("type", "lights");
                lightData.put("room", finalRoomLight);

                writer.println(lightData.toString());
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
                    runOnUiThread(() -> Toast.makeText(LightsActivity.this, "Error al conectar con el servidor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
    }

    private void handleServerResponse(String response) {
        Log.d("LightsActivity", "Server Response: " + response.trim());

        // Verificar si la respuesta es válida
        if (response == null || response.trim().isEmpty()) {
            Toast.makeText(LightsActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Dividir la respuesta en partes
            String[] parts = response.trim().split(":");
            String status = parts[0]; // "SUCCESS" o "FAIL"

            if (status.equals("SUCCESS")) {
                String roomLight = parts[1]; // Obtener la habitacion con la que se desea interactuar
                Toast.makeText(LightsActivity.this, "Interaccion con luz de " + roomLight, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LightsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                String errorMessage = "Fallo al interactuar con luz de " + parts[1]; // Obtener la habitacion con la que se dessea interactuar
                Toast.makeText(LightsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("LightsActivity", "Error procesando la respuesta del servidor", e);
            Toast.makeText(LightsActivity.this, "Error procesando la respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    //Métodos para comprobar si las coordenadas del toque están dentro del área de cada habitación
    private boolean isInSala(float x, float y) {
        return (x > 50 && x < 300 && y > 200 && y < 900);
    } private boolean isInCuarto1(float x, float y) {
        return (x > 400 && x < 650 && y > 200 && y < 400);
    } private boolean isInCuarto2(float x, float y) {
        return (x > 700 && x < 900 && y > 200 && y < 750);
    } private boolean isInCuarto3(float x, float y) {
        return (x > 700 && x < 1000 && y > 1000 && y < 1550);
    } private boolean isBaño1(float x, float y) {
        return (x > 400 && x < 550 && y > 600 && y < 900);
    } private boolean isInBaño2(float x, float y) {
        return (x > 700 && x < 900 && y > 750 && y < 1000);
    } private boolean isInCocina(float x, float y) {
        return (x > 300 && x < 450 && y > 1400 && y < 1600);
    } private boolean isInLimpieza(float x, float y) {
        return (x > 475 && x < 800 && y > 1400 && y < 1600);
    } private boolean isInGaraje(float x, float y) {
        return (x > 300 && x < 1000 && y > 1750 && y < 2000);
    }

    //Alterna el estado de la luz de la habitación y muestra un mensaje.
    private void toogleLight(String room) {
        Toast.makeText(this, room + " lights toogled", Toast.LENGTH_SHORT).show();

    }

    // Método para enviar la solicitud de alternar la luz
    private void sendToggleLightRequest(String room) {
        try {
            // Crear el JSON con el tipo de solicitud y la habitación
            JSONObject requestData = new JSONObject();
            requestData.put("type", "toggle_light");
            requestData.put("room", room);

            // Enviar los datos JSON al servidor
            sendDataToServer(requestData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendDataToServer(final String jsonData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Dirección IP y puerto del servidor
                    String serverIP = "192.168.18.81"; // Reemplaza con la IP de tu servidor
                    int serverPort = 1717;

                    // Crear la conexión al servidor
                    Socket socket = new Socket(serverIP, serverPort);
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);

                    // Enviar el JSON al servidor
                    writer.println(jsonData);
                    writer.flush();

                    // Recibir la respuesta del servidor
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = reader.readLine();

                    // Manejar la respuesta en el hilo principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleServerResponse(response);
                        }
                    });

                    // Cerrar recursos
                    writer.close();
                    reader.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}