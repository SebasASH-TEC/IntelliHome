package com.company.intellihome;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private List<Bitmap> imageList = new ArrayList<>();
    private List<String> propertyImagesBase64 = new ArrayList<>();
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alquiler);

        //Obtener los datos del intent
        String propertyID = getIntent().getStringExtra("property_id");
        Log.d("Alquiler", "Este es el ID: " + propertyID);
        String propertyPrice = getIntent().getStringExtra("property_price");
        String propertyAvailability = getIntent().getStringExtra("property_availability");
        String[] amenidadesArray = getIntent().getStringArrayExtra("property_amenidades");
        String[] imagesArray = getIntent().getStringArrayExtra("property_images");
        List<String> propertyAmenidades = amenidadesArray != null ? Arrays.asList(amenidadesArray) : new ArrayList<>();
        List<String> propertyImages = imagesArray != null ? Arrays.asList(imagesArray) : new ArrayList<>();

        availabilityInput = findViewById(R.id.editTextAvailability);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        //Cargar imágenes desde el servidro
        new Thread(() -> loadImagesFromServer(propertyImages, propertyID)).start();

        //Configurar el adaptador para el ViewPager2
        adapter = new ImagePagerAdapter(propertyImagesBase64);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(" " + (position + 1));
            }
        }).attach();

        //Llamar a la función para configurar los textos
        SetText(propertyID, propertyPrice, propertyAvailability, propertyAmenidades);

        availabilityInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showAvailabilityPicker();
            }
            return false;
        });
    }

    //Función para configurar los textos y manejar la acción de alquiler de una propiedad
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
            new Thread(() -> {
               try {
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

    //Función para cargar las imágenes del server
    private void loadImagesFromServer(List<String> propertyImages, String ID) {
        new Thread(() -> {
            for (String imageName : propertyImages) {
                try {
                    Socket socket = new Socket(entities.Host, 1717);

                    //Crea un objeto JSON para solicitar las imágenes
                    JSONObject requestData = new JSONObject();
                    requestData.put("type", "getImage");
                    requestData.put("property_id", ID);
                    requestData.put("image_name", imageName);

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(requestData.toString());

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    JSONObject jsonResponse = new JSONObject(response);

                    if (jsonResponse.has("image_data")) {
                        String imageBase64 = jsonResponse.getString("image_data");
                        propertyImagesBase64.add(imageBase64);
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (bitmap != null) {
                            imageList.add(bitmap);
                            runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    }
                    in.close();
                    out.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //Función para mostrar la dispoibilidad en calendario
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

    //Función para mostrar la fecha final
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

    //Función para actualizar la disponibilidad
    private void updateAvailabilityInput() {
        String start = startDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (startDate.get(Calendar.MONTH) + 1) + "/" +
                startDate.get(Calendar.YEAR);
        String end = endDate.get(Calendar.DAY_OF_MONTH) + "/" +
                (endDate.get(Calendar.MONTH) + 1) + "/" +
                endDate.get(Calendar.YEAR);
        availabilityInput.setText("Disponible del " + start + " al " + end);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Clase adaptador personalizada para manejar la visualización de imágenes en un RecyclerView
    protected class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<String> imageList;

        //Constructor de la clase
        protected ImagePagerAdapter(List<String> imageList) {
            this.imageList = imageList;
        }

        //Inflar el diseño de cada elemento del RecyclerView
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
            return  new ImageViewHolder(view);
        }

        //Asociar datos de la lista a cada vista
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String base64Image = imageList.get(position);
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        }

        //Obtener en número de elementos en la lista
        public int getItemCount() {
            return imageList.size();
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Clase interna para mantener la referencia al ImageView de cada elemento
        protected class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            //Inicializar el ImageView
            protected ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}