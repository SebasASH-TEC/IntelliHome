package com.company.intellihome;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AlquilerActivity extends AppCompatActivity {
    private Entities entities = new Entities();
    private Calendar startDate;
    private Calendar endDate;
    private EditText availabilityInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alquiler);

        //Obtener los datos del intent
        Log.d("Alquiler", "Si entra en la actividad");
        String propertyID = getIntent().getStringExtra("property_id");
        Log.d("Alquiler", "Este es el ID: " + propertyID);
        String propertyCoordinates = getIntent().getStringExtra("property_coordinates");
        String propertyPrice = getIntent().getStringExtra("property_price");
        String propertyAvailability = getIntent().getStringExtra("property_availability");
        String[] amenidadesArray = getIntent().getStringArrayExtra("property_amenidades");
        List<String> propertyAmenidades = amenidadesArray != null ? Arrays.asList(amenidadesArray) : new ArrayList<>();

        availabilityInput = findViewById(R.id.editTextAvailability);

        SetText(propertyID, propertyPrice, propertyAvailability, propertyAmenidades);

        availabilityInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showAvailabilityPicker();
            }
            return false;
        });
    }

    private void SetText(String ID, String Price, String Availability, List<String> Amenidades) {
        TextView textViewPrice = findViewById(R.id.textViewPrice);
        TextView textViewAvailability = findViewById(R.id.textViewAvailability);
        TextView textViewCharacteristics = findViewById(R.id.textViewAmenidades);
        Button buttonAlquiler = findViewById(R.id.ButtonAlquilar);

        textViewPrice.setText(Price);
        textViewAvailability.setText(Availability);
        if (Amenidades != null && !Amenidades.isEmpty()) {
            String amenidades = TextUtils.join(", ", Amenidades);
            textViewCharacteristics.setText(amenidades);
        }

        buttonAlquiler.setOnClickListener(v -> {
            Log.d("AlquilerActivity", "Botón presionado");
            new Thread(() -> {
               try {
                   Log.d("AlquilerActivity", "Iniciando conexión con el servidor");
                    //Crear el objeto JSON con la información de la propiedad
                   JSONObject alquilerData = new JSONObject();
                   alquilerData.put("type", "alquiler");
                   alquilerData.put("property_ID", ID);
                   alquilerData.put("startDate", startDate.getTimeInMillis());
                   alquilerData.put("endDate", endDate.getTimeInMillis());

                   //Conectar con el servidor
                   Socket socket = new Socket(entities.Host, 1717);
                   OutputStream outputStream = socket.getOutputStream();
                   PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

                   //Enviar los datos al servidor
                   writer.println(alquilerData.toString());

                   //Cerrar la conexión
                   writer.close();
                   socket.close();

                   runOnUiThread(() -> Toast.makeText(this, "Solicitud de alquiler enviada", Toast.LENGTH_SHORT).show());
               } catch (Exception e) {
                   e.printStackTrace();
                   runOnUiThread(() -> Toast.makeText(this, "Error al enviar la solcitud", Toast.LENGTH_SHORT).show());
               }
            }).start();
        });
    }

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

    private void showEndDatePicker() {
        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endDate.set(year, month, dayOfMonth);
            if (endDate.before(startDate)) {
                Toast.makeText(this, "La fecha de fin no puede ser anterior a la de inicio", Toast.LENGTH_SHORT).show();
            } else {
                updateAvailabilityInput();
            }
        }, endDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
        startDatePicker.setTitle("Selecciona la fecha de fin");
        startDatePicker.show();
    }

    private void updateAvailabilityInput() {
        String start = startDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (startDate.get(Calendar.MONTH) + 1) + "/" +
                startDate.get(Calendar.YEAR);
        String end = endDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (endDate.get(Calendar.MONTH) + 1) + "/" +
                endDate.get(Calendar.YEAR);
        availabilityInput.setText("Disponible del " + start + " al " + end);
    }
}