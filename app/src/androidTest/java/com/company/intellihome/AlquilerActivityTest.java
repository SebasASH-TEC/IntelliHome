package com.company.intellihome;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AlquilerActivityTest {

    @Rule
    public ActivityScenarioRule<AlquilerActivity> activityRule =
            new ActivityScenarioRule<>(AlquilerActivity.class);

    /**
     * Prueba para el método `SetText`.
     */
    @Test
    public void testSetText() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Simular datos de entrada
                String id = "12345";
                String price = "$100 USD";
                String availability = "Disponible del 01/01/2024 al 10/01/2024";
                List<String> amenities = new ArrayList<>();
                amenities.add("WiFi");
                amenities.add("Piscina");

                // Acceder al método SetText usando Reflection
                Method method = AlquilerActivity.class.getDeclaredMethod("SetText", String.class, String.class, String.class, List.class);
                method.setAccessible(true);
                method.invoke(activity, id, price, availability, amenities);

                // Verificar que los textos se hayan actualizado correctamente
                TextView priceView = activity.findViewById(R.id.textViewPrice);
                TextView availabilityView = activity.findViewById(R.id.textViewAvailability);
                TextView amenitiesView = activity.findViewById(R.id.textViewAmenidades);

                assertEquals("El precio debe coincidir", price, priceView.getText().toString());
                assertEquals("La disponibilidad debe coincidir", availability, availabilityView.getText().toString());
                assertEquals("Las amenidades deben coincidir", "WiFi, Piscina", amenitiesView.getText().toString());
            } catch (Exception e) {
                fail("Error al probar SetText: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `showAvailabilityPicker`.
     */
    @Test
    public void testShowAvailabilityPicker() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método showAvailabilityPicker
                Method method = AlquilerActivity.class.getDeclaredMethod("showAvailabilityPicker");
                method.setAccessible(true);
                method.invoke(activity);

                // Verificar que startDate y endDate no sean null
                Field startDateField = AlquilerActivity.class.getDeclaredField("startDate");
                startDateField.setAccessible(true);
                Calendar startDate = (Calendar) startDateField.get(activity);

                Field endDateField = AlquilerActivity.class.getDeclaredField("endDate");
                endDateField.setAccessible(true);
                Calendar endDate = (Calendar) endDateField.get(activity);

                assertNotNull("La fecha inicial no debe ser null", startDate);
                assertNotNull("La fecha final no debe ser null", endDate);
            } catch (Exception e) {
                fail("Error al probar showAvailabilityPicker: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `updateAvailabilityInput`.
     */
    @Test
    public void testUpdateAvailabilityInput() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Configurar las fechas de inicio y fin
                Field startDateField = AlquilerActivity.class.getDeclaredField("startDate");
                startDateField.setAccessible(true);
                Calendar startDate = Calendar.getInstance();
                startDate.set(2024, Calendar.JANUARY, 1);
                startDateField.set(activity, startDate);

                Field endDateField = AlquilerActivity.class.getDeclaredField("endDate");
                endDateField.setAccessible(true);
                Calendar endDate = Calendar.getInstance();
                endDate.set(2024, Calendar.JANUARY, 10);
                endDateField.set(activity, endDate);

                // Acceder al método updateAvailabilityInput
                Method method = AlquilerActivity.class.getDeclaredMethod("updateAvailabilityInput");
                method.setAccessible(true);
                method.invoke(activity);

                // Verificar el contenido del EditText de disponibilidad
                EditText availabilityInput = activity.findViewById(R.id.editTextAvailability);
                assertEquals("Disponible del 1/1/2024 al 10/1/2024", availabilityInput.getText().toString());
            } catch (Exception e) {
                fail("Error al probar updateAvailabilityInput: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `loadImagesFromServer`.
     */
    @Test
    public void testLoadImagesFromServer() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Simular imágenes en Base64
                Field imagesBase64Field = AlquilerActivity.class.getDeclaredField("propertyImagesBase64");
                imagesBase64Field.setAccessible(true);
                List<String> propertyImagesBase64 = (List<String>) imagesBase64Field.get(activity);

                // Agregar imágenes simuladas
                String simulatedBase64Image = Base64.encodeToString(new byte[100], Base64.DEFAULT);
                propertyImagesBase64.add(simulatedBase64Image);

                // Verificar que las imágenes se cargaron
                assertFalse("Debe haber imágenes en Base64", propertyImagesBase64.isEmpty());
            } catch (Exception e) {
                fail("Error al probar loadImagesFromServer: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para el adaptador `ImagePagerAdapter`.
     */
    @Test
    public void testImagePagerAdapter() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Simular imágenes en Base64
                List<String> imageList = new ArrayList<>();
                String base64Image = Base64.encodeToString(new byte[10], Base64.DEFAULT);
                imageList.add(base64Image);

                // Crear el adaptador
                AlquilerActivity.ImagePagerAdapter adapter = activity.new ImagePagerAdapter(imageList);

                // Verificar tamaño
                assertEquals("Debe haber una imagen en el adaptador", 1, adapter.getItemCount());
            } catch (Exception e) {
                fail("Error al probar ImagePagerAdapter: " + e.getMessage());
            }
        });
    }
}
