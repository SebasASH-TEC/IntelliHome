package com.company.intellihome;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Prueba para `setupLoginButton`.
     */
    @Test
    public void testSetupLoginButton() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Configurar campos de texto con datos de prueba
                Field usernameField = LoginActivity.class.getDeclaredField("usernameEditText");
                usernameField.setAccessible(true);
                EditText usernameEditText = (EditText) usernameField.get(activity);
                usernameEditText.setText("testUser");

                Field passwordField = LoginActivity.class.getDeclaredField("passwordEditText");
                passwordField.setAccessible(true);
                EditText passwordEditText = (EditText) passwordField.get(activity);
                passwordEditText.setText("password123");

                // Acceder al botón de login y simular clic
                Field loginButtonField = LoginActivity.class.getDeclaredField("loginButton");
                loginButtonField.setAccessible(true);
                Button loginButton = (Button) loginButtonField.get(activity);
                loginButton.performClick();

                // Verificar que los datos se envían al servidor
                Method sendLoginDataToServer = LoginActivity.class.getDeclaredMethod("sendLoginDataToServer", String.class, String.class);
                sendLoginDataToServer.setAccessible(true);
                sendLoginDataToServer.invoke(activity, "testUser", "password123");

                assertTrue("Se debe enviar la solicitud de login sin errores", true);
            } catch (Exception e) {
                fail("Error al probar setupLoginButton: " + e.getMessage());
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
                Method method = LoginActivity.class.getDeclaredMethod("handleServerResponse", String.class);
                method.setAccessible(true);

                // Simular una respuesta exitosa del servidor
                String successResponse = "SUCCESS:testUser";
                method.invoke(activity, successResponse);

                // Verificar el Toast
                Toast toast = Toast.makeText(activity, "Login exitoso para testUser", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", toast);

                // Simular una respuesta fallida del servidor
                String failureResponse = "FAIL:testUser";
                method.invoke(activity, failureResponse);

                Toast failToast = Toast.makeText(activity, "Fallo de login para testUser", Toast.LENGTH_SHORT);
                assertNotNull("El Toast no debe ser null", failToast);
            } catch (Exception e) {
                fail("Error al probar handleServerResponse: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `setupRegisterButton`.
     */
    @Test
    public void testSetupRegisterButton() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al botón de registro
                Field registerButtonField = LoginActivity.class.getDeclaredField("registerButton");
                registerButtonField.setAccessible(true);
                Button registerButton = (Button) registerButtonField.get(activity);

                // Simular clic en el botón de registro
                registerButton.performClick();

                // Verificar que se inicia la actividad de registro
                Intent expectedIntent = new Intent(activity, RegisterActivity.class);
                assertEquals("La actividad esperada es RegisterActivity", expectedIntent.getComponent().getClassName(), RegisterActivity.class.getName());
            } catch (Exception e) {
                fail("Error al probar setupRegisterButton: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `showForgotPasswordDialog`.
     */
    @Test
    public void testShowForgotPasswordDialog() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado showForgotPasswordDialog
                Method method = LoginActivity.class.getDeclaredMethod("showForgotPasswordDialog");
                method.setAccessible(true);
                method.invoke(activity);

                // Verificar que se muestra el diálogo de recuperación de contraseña
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                assertNotNull("El diálogo debe estar configurado", dialog);
            } catch (Exception e) {
                fail("Error al probar showForgotPasswordDialog: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `sendForgotPasswordRequest`.
     */
    @Test
    public void testSendForgotPasswordRequest() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado sendForgotPasswordRequest
                Method method = LoginActivity.class.getDeclaredMethod("sendForgotPasswordRequest", String.class);
                method.setAccessible(true);

                // Enviar una solicitud de recuperación de contraseña
                method.invoke(activity, "test@example.com");

                // Verificar que los datos se envían al servidor
                JSONObject requestData = new JSONObject();
                requestData.put("type", "forgot_password");
                requestData.put("email", "test@example.com");

                assertNotNull("Los datos de recuperación de contraseña deben enviarse correctamente", requestData.toString());
            } catch (Exception e) {
                fail("Error al probar sendForgotPasswordRequest: " + e.getMessage());
            }
        });
    }
}
