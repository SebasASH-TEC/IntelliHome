package com.company.intellihome;

import android.net.Uri;
import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AddPropertyActivityTest {

    @Rule
    public ActivityScenarioRule<AddPropertyActivity> activityRule =
            new ActivityScenarioRule<>(AddPropertyActivity.class);

    /**
     * Prueba para verificar el método encodeImageToBase64.
     */
    @Test
    public void testEncodeImageToBase64() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Generar un bitmap dinámico
                Bitmap dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                dummyBitmap.eraseColor(Color.RED); // Rellena con color rojo

                // Guardar el bitmap en un URI temporal
                String path = MediaStore.Images.Media.insertImage(
                        activity.getContentResolver(),
                        dummyBitmap,
                        "TestImage",
                        null
                );
                Uri dummyUri = Uri.parse(path);

                // Obtener el método privado
                Method method = AddPropertyActivity.class.getDeclaredMethod("encodeImageToBase64", Uri.class);
                method.setAccessible(true);

                // Llamar al método
                String base64 = (String) method.invoke(activity, dummyUri);

                // Verificar que no sea null
                assertNotNull("La conversión a Base64 no debe devolver null", base64);
                assertTrue("La cadena debe contener datos", base64.length() > 0);
            } catch (Exception e) {
                fail("Error al probar encodeImageToBase64: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para verificar el método addMarkerAtLocation.
     */
    @Test
    public void testAddMarkerAtLocation() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Crear un GeoPoint simulado
                GeoPoint testPoint = new GeoPoint(10.12345, -75.12345);

                // Acceder al método privado addMarkerAtLocation
                Method method = AddPropertyActivity.class.getDeclaredMethod("addMarkerAtLocation", GeoPoint.class);
                method.setAccessible(true);
                method.invoke(activity, testPoint);

                // Acceder al campo currentMarker
                Field markerField = AddPropertyActivity.class.getDeclaredField("currentMarker");
                markerField.setAccessible(true);
                Marker currentMarker = (Marker) markerField.get(activity);

                assertNotNull("El marcador no debe ser null", currentMarker);
                assertEquals("La posición del marcador debe coincidir", testPoint, currentMarker.getPosition());
            } catch (Exception e) {
                fail("Error al probar addMarkerAtLocation: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para verificar el método saveProperty.
     */
    @Test
    public void testSaveProperty() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Configurar los campos necesarios
                Field coordinatesField = AddPropertyActivity.class.getDeclaredField("coordinatesEditText");
                coordinatesField.setAccessible(true);
                EditText coordinatesEditText = (EditText) coordinatesField.get(activity);
                coordinatesEditText.setText("10.12345, -75.12345");

                Field priceField = AddPropertyActivity.class.getDeclaredField("priceInput");
                priceField.setAccessible(true);
                EditText priceInput = (EditText) priceField.get(activity);
                priceInput.setText("$100 USD");

                Field availabilityField = AddPropertyActivity.class.getDeclaredField("availabilityInput");
                availabilityField.setAccessible(true);
                EditText availabilityInput = (EditText) availabilityField.get(activity);
                availabilityInput.setText("Disponible del 20/11/2024 al 30/11/2024");

                // Acceder y modificar listas
                Field rulesListField = AddPropertyActivity.class.getDeclaredField("rulesList");
                rulesListField.setAccessible(true);
                List<String> rulesList = (List<String>) rulesListField.get(activity);
                rulesList.add("No mascotas");

                Field selectedItemsField = AddPropertyActivity.class.getDeclaredField("selectedItems");
                selectedItemsField.setAccessible(true);
                List<String> selectedItems = (List<String>) selectedItemsField.get(activity);
                selectedItems.add("WiFi");

                // Llamar al método saveProperty
                Method method = AddPropertyActivity.class.getDeclaredMethod("saveProperty");
                method.setAccessible(true);
                method.invoke(activity);

                assertTrue("La lista de reglas debe contener 'No mascotas'", rulesList.contains("No mascotas"));
                assertTrue("La lista de características debe contener 'WiFi'", selectedItems.contains("WiFi"));
            } catch (Exception e) {
                fail("Error al probar saveProperty: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para verificar el método showAvailabilityPicker.
     */
    @Test
    public void testShowAvailabilityPicker() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado showAvailabilityPicker
                Method method = AddPropertyActivity.class.getDeclaredMethod("showAvailabilityPicker");
                method.setAccessible(true);
                method.invoke(activity);

                // Acceder a los campos startDate y endDate
                Field startDateField = AddPropertyActivity.class.getDeclaredField("startDate");
                startDateField.setAccessible(true);
                Calendar startDate = (Calendar) startDateField.get(activity);

                Field endDateField = AddPropertyActivity.class.getDeclaredField("endDate");
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
     * Prueba para verificar la lista de reglas.
     */
    @Test
    public void testAddRules() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al campo rulesList
                Field rulesListField = AddPropertyActivity.class.getDeclaredField("rulesList");
                rulesListField.setAccessible(true);
                List<String> rulesList = (List<String>) rulesListField.get(activity);

                // Agregar una regla simulada
                rulesList.add("No fiestas");

                assertTrue("La lista de reglas debe contener 'No fiestas'", rulesList.contains("No fiestas"));
            } catch (Exception e) {
                fail("Error al probar la lista de reglas: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para verificar la selección de fotos.
     */
    @Test
    public void testUpdateSelectedPhotos() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al campo selectedPhotosUris
                Field photosField = AddPropertyActivity.class.getDeclaredField("selectedPhotosUris");
                photosField.setAccessible(true);
                List<Uri> selectedPhotosUris = (List<Uri>) photosField.get(activity);

                // Agregar un URI simulado
                selectedPhotosUris.add(Uri.parse("content://media/external/images/media/1"));

                assertEquals("Debe haber una foto seleccionada", 1, selectedPhotosUris.size());
                assertEquals("El URI debe coincidir", "content://media/external/images/media/1", selectedPhotosUris.get(0).toString());
            } catch (Exception e) {
                fail("Error al probar la selección de fotos: " + e.getMessage());
            }
        });
    }
}
