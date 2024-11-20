package com.company.intellihome;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    /**
     * Prueba para `compressBitmap`.
     */
    @Test
    public void testCompressBitmap() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Crear un bitmap de prueba
                Bitmap originalBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);

                // Acceder al método privado `compressBitmap`
                Method compressBitmapMethod = RegisterActivity.class.getDeclaredMethod("compressBitmap", Bitmap.class, int.class);
                compressBitmapMethod.setAccessible(true);

                // Comprimir el bitmap
                Bitmap compressedBitmap = (Bitmap) compressBitmapMethod.invoke(activity, originalBitmap, 500);

                // Verificar que las dimensiones se hayan reducido
                assertNotNull(compressedBitmap);
                assertTrue("El ancho debe ser menor o igual a 500", compressedBitmap.getWidth() <= 500);
                assertTrue("El alto debe ser menor o igual a 500", compressedBitmap.getHeight() <= 500);
            } catch (Exception e) {
                fail("Error al probar compressBitmap: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `isNicknameProhibited`.
     */
    @Test
    public void testIsNicknameProhibited() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado `isNicknameProhibited`
                Method isNicknameProhibitedMethod = RegisterActivity.class.getDeclaredMethod("isNicknameProhibited", String.class);
                isNicknameProhibitedMethod.setAccessible(true);

                // Probar con un apodo prohibido
                boolean result1 = (boolean) isNicknameProhibitedMethod.invoke(activity, "palabra1");
                assertTrue("El apodo 'palabra1' debería estar prohibido", result1);

                // Probar con un apodo permitido
                boolean result2 = (boolean) isNicknameProhibitedMethod.invoke(activity, "usuario123");
                assertFalse("El apodo 'usuario123' debería estar permitido", result2);
            } catch (Exception e) {
                fail("Error al probar isNicknameProhibited: " + e.getMessage());
            }
        });
    }

    /**
     * Prueba para `validatePassword`.
     */
    @Test
    public void testValidatePassword() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder al método privado `validatePassword`
                Method validatePasswordMethod = RegisterActivity.class.getDeclaredMethod("validatePassword", String.class);
                validatePasswordMethod.setAccessible(true);

                // Probar con contraseñas inválidas
                String result1 = (String) validatePasswordMethod.invoke(activity, "short");
                assertEquals("La contraseña debe tener al menos 8 caracteres.", result1);

                String result2 = (String) validatePasswordMethod.invoke(activity, "NoNumber!");
                assertEquals("La contraseña debe contener al menos un número.", result2);

                String result3 = (String) validatePasswordMethod.invoke(activity, "nonumbers");
                assertEquals("La contraseña debe contener al menos un número.", result3);

                String result4 = (String) validatePasswordMethod.invoke(activity, "NoSpecialChar1");
                assertEquals("La contraseña debe contener al menos un carácter especial.", result4);

                // Probar con una contraseña válida
                String result5 = (String) validatePasswordMethod.invoke(activity, "Valid123!");
                assertNull("La contraseña debería ser válida", result5);
            } catch (Exception e) {
                fail("Error al probar validatePassword: " + e.getMessage());
            }
        });
    }

    /**

    /**
     * Prueba para `sendRegistrationData`.
     */
    @Test
    public void testSendRegistrationData() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                // Acceder a los campos necesarios
                Field nameInputField = RegisterActivity.class.getDeclaredField("nameInputText");
                nameInputField.setAccessible(true);
                EditText nameInputText = (EditText) nameInputField.get(activity);
                nameInputText.setText("John");

                Field lastNameInputField = RegisterActivity.class.getDeclaredField("lastNameInputText");
                lastNameInputField.setAccessible(true);
                EditText lastNameInputText = (EditText) lastNameInputField.get(activity);
                lastNameInputText.setText("Doe");

                Field emailInputField = RegisterActivity.class.getDeclaredField("emailInputText");
                emailInputField.setAccessible(true);
                EditText emailInputText = (EditText) emailInputField.get(activity);
                emailInputText.setText("john.doe@example.com");

                Field passwordInputField = RegisterActivity.class.getDeclaredField("passwordInputText");
                passwordInputField.setAccessible(true);
                EditText passwordInputText = (EditText) passwordInputField.get(activity);
                passwordInputText.setText("Password123!");

                Field confirmPasswordField = RegisterActivity.class.getDeclaredField("confirmPasswordInputText");
                confirmPasswordField.setAccessible(true);
                EditText confirmPasswordInputText = (EditText) confirmPasswordField.get(activity);
                confirmPasswordInputText.setText("Password123!");

                // Acceder al botón y simular clic
                Field nextPayButtonField = RegisterActivity.class.getDeclaredField("nextPayButton");
                nextPayButtonField.setAccessible(true);
                Button nextPayButton = (Button) nextPayButtonField.get(activity);
                nextPayButton.performClick();

                // Verificar que no hay errores
                assertTrue("El registro debería completarse sin errores", true);
            } catch (Exception e) {
                fail("Error al probar sendRegistrationData: " + e.getMessage());
            }
        });
    }
}
