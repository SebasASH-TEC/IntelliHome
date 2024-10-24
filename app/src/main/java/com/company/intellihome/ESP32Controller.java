package com.company.intellihome;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ESP32Controller {

    private String esp32IPAddress;
    private int esp32Port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static final String TAG = "ESP32Controller";

    // Constructor to initialize IP address and port
    public ESP32Controller(String ipAddress, int port) {
        this.esp32IPAddress = ipAddress;
        this.esp32Port = port;
    }

    // Connect to the ESP32 device
    public boolean connect() {
        try {
            socket = new Socket(esp32IPAddress, esp32Port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Log.d(TAG, "Connected to ESP32 at " + esp32IPAddress + ":" + esp32Port);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to ESP32: " + e.getMessage());
            return false;
        }
    }

    // Send a command to control the GPIO on the ESP32
    public String sendCommand(String gpioCommand) {
        try {
            // Create a JSON object with the command
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("gpio", gpioCommand);

            // Send the JSON command to ESP32
            out.println(jsonCommand.toString());
            Log.d(TAG, "Sent command: " + jsonCommand.toString());

            // Read the response from ESP32
            String response = in.readLine();
            if (response != null) {
                Log.d(TAG, "ESP32 Response: " + response);
                return response;
            } else {
                Log.e(TAG, "No response received from ESP32.");
                return "No response";
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e.getMessage());
            return "JSON error";
        } catch (IOException e) {
            Log.e(TAG, "IO error while sending command: " + e.getMessage());
            return "IO error";
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            return "Unexpected error";
        }
    }

    // Disconnect from the ESP32
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            Log.d(TAG, "Disconnected from ESP32.");
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting from ESP32: " + e.getMessage());
        }
    }

    // Main method to test the class (for testing purposes only, not used in Android app)
    public static void main(String[] args) {
        ESP32Controller controller = new ESP32Controller("192.168.18.203", 5000); // Replace with your ESP32 IP address

        if (controller.connect()) {
            // Turn the LED on
            controller.sendCommand("on");
            try {
                Thread.sleep(2000);  // Wait for 2 seconds
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted: " + e.getMessage());
            }

            // Turn the LED off
            controller.sendCommand("off");

            // Disconnect from ESP32
            controller.disconnect();
        }
    }
}