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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lights);
        //Configurar para que la pantalla ocupe todo el espacio
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        houseMap = findViewById(R.id.houseMap);

        //Detectar toques en la imagen del plano de la casa
        houseMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    float x = event.getX();
                    float y = event.getY();
                    checkRoomTapped(x, y);
                }
                return true;
            }
        });
    }

    private void checkRoomTapped(float x, float y)
    {
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

    //Coordenadas de la sala
    private boolean isInSala(float x, float y) {
        return (x > 50 && x < 300 && y > 200 && y < 900);
    } private boolean isInCuarto1(float x, float y) {
        return (x > 400 && x < 650 && y > 200 && y < 400);
    } private boolean isInCuarto2(float x, float y) {
        return (x > 700 && x < 900 && y > 200 && y < 750);
    } private boolean isInCuarto3(float x, float y) {
        return (x > 500 && x < 1500 && y > 250 && y < 550);
    } private boolean isBaño1(float x, float y) {
        return (x > 300 && x < 500 && y > 1100 && y < 1400);
    } private boolean isInBaño2(float x, float y) {
        return (x > 1000 && x < 2500 && y > 900 && y < 1000);
    } private boolean isInCocina(float x, float y) {
        return (x > 400 && x < 600 && y > 500 && y < 700);
    } private boolean isInLimpieza(float x, float y) {
        return (x > 475 && x < 800 && y > 100 && y < 1400);
    } private boolean isInGaraje(float x, float y) {
        return (x > 600 && x < 800 && y > 300 && y < 700);
    }

    private void toogleLight(String room)
    {
        Toast.makeText(this, room + " lights toogled", Toast.LENGTH_SHORT).show();
    }
}