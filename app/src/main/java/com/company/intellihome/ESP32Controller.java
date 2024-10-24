package com.company.intellihome;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class ESP32Controller {

    private String esp32IPAddress;
    private int esp32Port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Constructor to initialize IP address and port
    public ESP32Controller(String ipAddress, int port) {
        this.esp32IPAddress = ipAddress;
        this.esp32Port = port;
    }

    // Connect to the ESP32 device
    public void connect() throws IOException {
        try {
            socket = new Socket(esp32IPAddress, esp32Port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to ESP32 at " + esp32IPAddress + ":" + esp32Port);
        } catch (IOException e) {
            System.err.println("Unable to connect to ESP32: " + e.getMessage());
            throw e;
        }
    }

    // Send a command to control the GPIO on the ESP32
    public void sendCommand(String gpioCommand) {
        try {
            // Create a JSON object with the command
            JSONObject jsonCommand = new JSONObject();
            jsonCommand.put("gpio", gpioCommand);

            // Send the JSON command to ESP32
            out.println(jsonCommand.toString());
            System.out.println("Sent command: " + jsonCommand.toString());

            // Read the response from ESP32
            String response = in.readLine();
            if (response != null) {
                System.out.println("ESP32 Response: " + response);
            }
        } catch (Exception e) {
            System.err.println("Error sending command: " + e.getMessage());
        }
    }

    // Disconnect from the ESP32
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from ESP32.");
        } catch (IOException e) {
            System.err.println("Error disconnecting from ESP32: " + e.getMessage());
        }
    }

    // Main method to test the class
    public static void main(String[] args) {
        ESP32Controller controller = new ESP32Controller("192.168.1.100", 5000); // Replace with your ESP32 IP address

        try {
            // Connect to ESP32
            controller.connect();

            // Turn the LED on
            controller.sendCommand("on");
            Thread.sleep(2000);  // Wait for 2 seconds

            // Turn the LED off
            controller.sendCommand("off");

            // Disconnect from ESP32
            controller.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



