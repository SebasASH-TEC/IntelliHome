package com.company.intellihome;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LightsActivity extends AppCompatActivity {
    private ImageView houseMap;

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

    //Función par verificar si el toque corresponde a una habitación y alterna la luz en consecuencia
    private void checkRoomTapped(float x, float y) {
        if (isInSala(x, y)) {
            toogleLight("Sala");
        } else if (isInCuarto1(x, y)) {
            toogleLight("Cuarto 1");
        } else if (isInCuarto2(x, y)) {
            toogleLight("Cuarto 2");
        } else if (isInCuarto3(x, y)) {
            toogleLight("Cuarto 3");
        } else if (isBaño1(x, y)) {
            toogleLight("Baño 1");
        } else if (isInBaño2(x, y)) {
            toogleLight("Baño 2");
        } else if (isInCocina(x, y)) {
            toogleLight("Cocina");
        } else if (isInLimpieza(x, y)) {
            toogleLight("Cuarto de Lavado");
        } else if (isInGaraje(x, y)) {
            toogleLight("Garaje");
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
}