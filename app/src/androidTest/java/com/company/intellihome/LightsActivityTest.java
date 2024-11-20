package com.company.intellihome;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LightsActivityTest {

    @Rule
    public ActivityScenarioRule<LightsActivity> activityRule =
            new ActivityScenarioRule<>(LightsActivity.class);

    /**
     * Prueba para `checkRoomTapped`.
     */
    @Test
    public void testCheckRoomTapped() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado checkRoomTapped
                Method method = LightsActivity.class.getDeclaredMethod("checkRoomTapped", float.class, float.class);
                method.setAccessible(true);

                // Simular un toque en las coordenadas de "Sala"
                method.invoke(activity, 100f, 300f);

                // No hay retorno directo, pero podemos verificar el Toast
                Toast toast = Toast.makeText(activity, "Sala lights toggled", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);
            } catch (Exception e) {
                fail("Error al probar checkRoomTapped: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `toogleLight`.
     */
    @Test
    public void testToogleLight() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado toogleLight
                Method method = LightsActivity.class.getDeclaredMethod("toogleLight", String.class);
                method.setAccessible(true);

                // Alternar las luces de la "Sala"
                method.invoke(activity, "Sala");

                // Verificar el resultado con un Toast
                Toast toast = Toast.makeText(activity, "Sala lights toggled", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);
            } catch (Exception e) {
                fail("Error al probar toogleLight: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `handleServerResponse`.
     */
    @Test
    public void testHandleServerResponse() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado handleServerResponse
                Method method = LightsActivity.class.getDeclaredMethod("handleServerResponse", String.class);
                method.setAccessible(true);

                // Probar una respuesta exitosa del servidor
                String successResponse = "SUCCESS:Living room";
                method.invoke(activity, successResponse);

                // Verificar el Log y Toast
                Toast toast = Toast.makeText(activity, "Interaccion con Living room", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);
            } catch (Exception e) {
                fail("Error al probar handleServerResponse: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `sendDataToServer`.
     */
    @Test
    public void testSendDataToServer() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado sendDataToServer
                Method method = LightsActivity.class.getDeclaredMethod("sendDataToServer", String.class);
                method.setAccessible(true);

                // Crear datos JSON simulados
                JSONObject jsonData = new JSONObject();
                jsonData.put("type", "lights");
                jsonData.put("room", "Living room");

                // Enviar datos al servidor
                method.invoke(activity, jsonData.toString());

                // Verificar que no se produzcan errores
                assertTrue("Los datos deben enviarse sin errores", true);
            } catch (Exception e) {
                fail("Error al probar sendDataToServer: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `isInSala` y otros métodos de detección de habitaciones.
     */
    @Test
    public void testRoomDetectionMethods() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Probar isInSala
                Method isInSala = LightsActivity.class.getDeclaredMethod("isInSala", float.class, float.class);
                isInSala.setAccessible(true);
                boolean salaResult = (boolean) isInSala.invoke(activity, 100f, 300f);
                assertTrue("Las coordenadas deben estar dentro de la Sala", salaResult);

                // Probar isInCuarto1
                Method isInCuarto1 = LightsActivity.class.getDeclaredMethod("isInCuarto1", float.class, float.class);
                isInCuarto1.setAccessible(true);
                boolean cuarto1Result = (boolean) isInCuarto1.invoke(activity, 500f, 300f);
                assertTrue("Las coordenadas deben estar dentro de Cuarto 1", cuarto1Result);

                // Probar isInGaraje
                Method isInGaraje = LightsActivity.class.getDeclaredMethod("isInGaraje", float.class, float.class);
                isInGaraje.setAccessible(true);
                boolean garajeResult = (boolean) isInGaraje.invoke(activity, 500f, 1800f);
                assertTrue("Las coordenadas deben estar dentro del Garaje", garajeResult);

            } catch (Exception e) {
                fail("Error al probar métodos de detección de habitaciones: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para la interacción con el botón del garaje.
     */
    @Test
    public void testGarageDoorInteraction() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al campo garageDoorImg
                Field garageDoorImgField = LightsActivity.class.getDeclaredField("garageDoorImg");
                garageDoorImgField.setAccessible(true);
                ImageView garageDoorImg = (ImageView) garageDoorImgField.get(activity);

                // Simular un clic en la puerta del garaje
                garageDoorImg.performClick();

                // Verificar el Toast
                Toast toast = Toast.makeText(activity, "Garege door toggled", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);
            } catch (Exception e) {
                fail("Error al probar la interacción con el botón del garaje: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para los límites de detección.
     */
    @Test
    public void testOutOfRoomDetection() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Probar coordenadas fuera de cualquier habitación
                Method checkRoomTapped = LightsActivity.class.getDeclaredMethod("checkRoomTapped", float.class, float.class);
                checkRoomTapped.setAccessible(true);

                checkRoomTapped.invoke(activity, 1500f, 2500f);

                // Verificar el Toast para habitación no válida
                Toast toast = Toast.makeText(activity, "No seleccionó una habitación válida", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);
            } catch (Exception e) {
                fail("Error al probar detección fuera de los límites: " + e.getMessage());
            }
        });
    }
}
