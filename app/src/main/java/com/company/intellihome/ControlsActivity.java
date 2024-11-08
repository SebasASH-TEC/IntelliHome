package com.company.intellihome;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONException;

public class ControlsActivity extends AppCompatActivity {

    private static final String SERVER_IP = "192.168.18.212"; // Cambia a la IP del servidor
    private static final int SERVER_PORT = 1717; // Cambia al puerto del servidor
    private static final int LISTENING_PORT = 1718; // Nuevo puerto para recibir mensajes

    // Variables para los botones
    private ImageButton btnHumidityTemperature;
    private ImageButton btnFlameSensor;
    private ImageButton btnEarthquakeSensor;
    private ImageButton btnPhotoresistor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controls_activity);

        // Inicialización de botones
        btnHumidityTemperature = findViewById(R.id.btn_humidity_temperature);
        btnFlameSensor = findViewById(R.id.btn_flame_sensor);
        btnEarthquakeSensor = findViewById(R.id.btn_earthquake_sensor);
        btnPhotoresistor = findViewById(R.id.btn_photoresistor);

        // Configuración de clics para cada botón de sensor
        btnHumidityTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer("sensor_humidity");
                Toast.makeText(ControlsActivity.this, "Sensor de Humedad y Temperatura Activado", Toast.LENGTH_SHORT).show();
            }
        });

        btnFlameSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer("sensor_flame");
                Toast.makeText(ControlsActivity.this, "Sensor de Llama Activado", Toast.LENGTH_SHORT).show();
            }
        });

        btnEarthquakeSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer("sensor_earthquake");
                Toast.makeText(ControlsActivity.this, "Sensor de Sismo Activado", Toast.LENGTH_SHORT).show();
            }
        });

        btnPhotoresistor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer("sensor_photoresistor");
                Toast.makeText(ControlsActivity.this, "Fotoresistencia Activada", Toast.LENGTH_SHORT).show();
            }
        });

        // Iniciar hilo para recibir mensajes del servidor
        startListeningToServer();
    }

    /**
     * Envía un mensaje al servidor con el tipo de sensor activado.
     *
     * @param sensorType Tipo de sensor ("sensor_humidity", "sensor_flame", "sensor_earthquake", "sensor_photoresistor")
     */
    private void sendMessageToServer(final String sensorType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
                    // Crear un JSON con el tipo de sensor
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", sensorType);

                    // Enviar el JSON al servidor
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(jsonObject.toString().getBytes());
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Iniciar un hilo para escuchar mensajes del servidor.
     */
    private void startListeningToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(SERVER_IP, LISTENING_PORT)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Mostrar el mensaje recibido en un Toast
                        String finalLine = line;
                        runOnUiThread(() -> Toast.makeText(ControlsActivity.this, "Mensaje recibido: " + finalLine, Toast.LENGTH_SHORT).show());
                        handleServerMessage(finalLine);
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(ControlsActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Maneja los mensajes del servidor y cambia el estado del ícono correspondiente.
     *
     * @param message Mensaje recibido del servidor.
     */
    private void handleServerMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    String sensorType = jsonObject.getString("type");

                    // Comprobar si contiene "data" y procesarlo, si existe
                    String status = "unknown";
                    if (jsonObject.has("data")) {
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        status = dataObject.getString("status");
                    }

                    // Toast informativo sobre el mensaje recibido
                    Toast.makeText(ControlsActivity.this, "Sensor: " + sensorType + ", Estado: " + status, Toast.LENGTH_SHORT).show();

                    // Actualizar íconos en base al tipo y estado
                    switch (sensorType) {
                        case "photoresistor":
                            updateIconState(btnPhotoresistor, status.equals("low_light") ? 0xFFFFFF00 : 0xFF000000); // Amarillo para baja luz
                            break;
                        case "sensor_flame":
                            updateIconState(btnFlameSensor, 0xFFFF0000); // Rojo
                            break;
                        case "sensor_earthquake":
                            updateIconState(btnEarthquakeSensor, 0xFF00FF00); // Verde
                            break;
                        case "sensor_humidity":
                            updateIconState(btnHumidityTemperature, 0xFF0000FF); // Azul
                            break;
                        default:
                            Toast.makeText(ControlsActivity.this, "Sensor desconocido: " + sensorType, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    Toast.makeText(ControlsActivity.this, "Error al analizar mensaje JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Actualiza el estado visual del ícono para indicar que el sensor fue activado.
     *
     * @param button Botón del sensor a actualizar.
     * @param color  Color a aplicar al botón.
     */
    private void updateIconState(ImageButton button, int color) {
        // Cambia el color de fondo del botón como indicador visual
        button.setColorFilter(color, PorterDuff.Mode.SRC_ATOP); // Cambia el color según el sensor
        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.clearColorFilter(); // Restaurar el color original después de un tiempo
            }
        }, 2000); // Restaura el color después de 2 segundos
    }
}
