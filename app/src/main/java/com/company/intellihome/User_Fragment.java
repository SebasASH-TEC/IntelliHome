package com.company.intellihome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Fragmento de Arrendatario para manejar la interfaz.
public class User_Fragment extends Fragment {
    private Entities entities = new Entities();

    //Definir diferentes variables
    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList = new ArrayList<>();
    private Map<String, List<Bitmap>> propertyImagesMap = new HashMap<>();

    //Función para inflar la interfaz de User_Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_, container, false);
        HomeActivity homeActivity = (HomeActivity) getActivity();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PropertyAdapter(propertyList, getContext(), propertyImagesMap);
        recyclerView.setAdapter(adapter);

        fetchProperties();
        return  view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //Función para recibir las propiedades existentes
    private void fetchProperties() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(entities.Host, 1717);

                //Crea un objeto JSON para solicitar las propiedades al servidor.
                JSONObject requestData = new JSONObject();
                requestData.put("type", "getProperties");

                //Envía la solicitud al servidor.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(requestData.toString());

                //Lee la respuesta del servidor.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();

                //Convierte las respuesta del servidor en una lista de String.
                String[] jsonArrayString = response.split("(?<=\\}])(?=\\[\\{)");

                propertyList.clear();
                AddPropertiesInList(jsonArrayString, propertyList);

                //Solicita imágenes para cada propiedad
                for (Property property: propertyList) {
                    fetchPropertyImages(property.getId(), propertyImagesMap, adapter, getContext());
                }

                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //Función para agregar las propiedades en una lista
    protected void AddPropertiesInList(String[] propertiesArray, List<Property> list) {
        //Itera a través de cada objeto en el arreglo de propiedades.
        for (String arrayString : propertiesArray) {
            try {
                //Convierte cada fragmento en un JSONArray
                JSONArray propiertiesArray = new JSONArray(arrayString);

                //Bucle para extraer objetos JSON
                for (int i = 0; i < propiertiesArray.length(); i++){
                    JSONObject propertyJson = propiertiesArray.getJSONObject(i);

                    //Extrae los datos
                    String id = propertyJson.getString("id");
                    String coordinates = propertyJson.getString("coordinates");
                    String price = propertyJson.getString("price");
                    String availability = propertyJson.getString("availability");

                    Log.d("PropertyData", "ID: " + id + ", Coordinates: " + coordinates + ", Price: " + price + ", Availability: " + availability);

                    //Extrae los datos de las caracteristicas y los convierte en una lista
                    JSONArray characteristicsArray = propertyJson.getJSONArray("characteristics");
                    List<String> characteristics = new ArrayList<>();
                    for (int j = 0; j < characteristicsArray.length(); j++) {
                        characteristics.add(characteristicsArray.getString(j));
                    }

                    //Extrae los datos de las imagenes y los convierte en una lista
                    JSONArray imagesArray = propertyJson.getJSONArray("images");
                    List<String> images = new ArrayList<>();
                    for (int j = 0; j < imagesArray.length(); j++) {
                        images.add(imagesArray.getString(j));
                    }

                    list.add(new Property(id, coordinates, price, availability, characteristics, images));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Función para recibir la imagen principal de la propiedad
    protected void fetchPropertyImages(String propertyId, Map<String, List<Bitmap>> propertyImagesMap, PropertyAdapter adapter, Context context) {
        new Thread(() -> {
           try {
               Socket socket = new Socket(entities.Host, 1717);

               Log.d("Fetch", "Si entro");
               //Crea un objeto JSON para solicitar las imágenes
               JSONObject requestData = new JSONObject();
               requestData.put("type", "getImage");
               requestData.put("property_id", propertyId);
               requestData.put("image_name", "photo_1.jpg");

               PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
               out.println(requestData.toString());

               BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               String response = in.readLine();
               Log.d("Response", "Todo bien y esta es la respuesta: " + response);
               JSONObject jsonResponse = new JSONObject(response);

               if (jsonResponse.has("image_data")) {
                   Log.d("Ftech", "Si tiene image_data");
                   String imageBase64 = jsonResponse.getString("image_data");
                   byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                   Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                   Log.d("Ftech", "Si se decodifico la imagen");

                   //Almacena a imagen en el mapa de la propiedad
                   if (!propertyImagesMap.containsKey(propertyId)) {
                       Log.d("Fetch", "Si entro a esta vara rara");
                       propertyImagesMap.put(propertyId, new ArrayList<>());
                       Log.d("Ftech", "Si subio la imagen en lo de propertyImagesMap");
                   }

                   propertyImagesMap.get(propertyId).add(decodedImage);
                   Log.d("Ftech", "Si guardo");

                   // Actualiza la UI en el contexto apropiado
                   if (context instanceof FragmentActivity) {
                       Log.d("Fragmento", "");
                       ((FragmentActivity) context).runOnUiThread(() -> adapter.notifyDataSetChanged());
                   } else {
                       Log.d("Actividad", "");
                       new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
                   }

                   getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
               }
               socket.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
        }).start();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

        private List<Property> propertyList;
        private Context context;
        private Map<String, List<Bitmap>> propertyImagesMap;

        public PropertyAdapter(List<Property> propertyList, Context context, Map<String, List<Bitmap>> propertyImagesMap) {
            this.propertyList = propertyList;
            this.context = context;
            this.propertyImagesMap = propertyImagesMap;
        }

        @Override
        //Vincula cada propiedad en la lista con el ViewHolder
        public void onBindViewHolder(PropertyViewHolder holder, int position) {
            Log.d("User", "Esto es lo que se recibió en onBindViewHolder - Holder: " + holder + ", Position: " + position);
            Property property = propertyList.get(position);
            Log.d("User", "Propiedad en la posición: " + property.toString());  // Imprime detalles de la propiedad


            // Verifica si el mapa es null
            //if (propertyImagesMap != null) {
            Log.d("User", "Mapa de imágenes no es null. Pasando al método bind. Y este es el imageMap: " + propertyImagesMap);
            holder.bind(property, propertyImagesMap);  // Solo se pasa el mapa si no es null
            //} else {
              //  Log.d("User", "Mapa de imágenes es null. No se pasa al método bind.");
                //holder.bind(property, null);
            //}
        }

        @NonNull
        @Override
        //Crea una nueva instancia de ViewHolder inflando el layout de cada ítem de la lista
        public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.property_item, parent, false);
            return new PropertyViewHolder(view, context, propertyList);
        }

        @Override
        //Obtiene el número de elementos en la lista.
        public int getItemCount() {
            return propertyList.size();
        }


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Clase interna para el ViewHolder que maneja la representación de cada ítem.
        static class PropertyViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView coordinatesTextView;
            private TextView priceTextView;
            private TextView availabilityTextView;
            private TextView characteristicsTextView;

            //Inicializa los elementos de la vista
            public PropertyViewHolder(View itemView, Context context, List<Property> propertyList) {
                super(itemView);
                imageView = itemView.findViewById(R.id.property_image);
                coordinatesTextView = itemView.findViewById(R.id.property_coordinates);
                priceTextView = itemView.findViewById(R.id.property_price);
                availabilityTextView = itemView.findViewById(R.id.property_availability);
                characteristicsTextView = itemView.findViewById(R.id.property_characteristics);

                //Manejar los clic en el elemento del RecyclerView
                itemView.setOnClickListener(v -> {
                    Log.d("Verificación", "Si toca la propiedad");
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {

                        //Obtener la propiedad que se hizo clic
                        Property property = propertyList.get(position);
                        Intent intent = new Intent(context, AlquilerActivity.class);

                        intent.putExtra("property_id", property.getId());
                        intent.putExtra("property_coordinates", property.getCoordinates());
                        intent.putExtra("property_price", property.getPrice());
                        intent.putExtra("property_availability", property.getAvailability());
                        intent.putExtra("property_amenidades", property.getCharacteristics().toArray(new String[0]));
                        intent.putExtra("property_images", property.getImages().toArray(new String[0]));
                        context.startActivity(intent);
                    }
                });
            }

            //Asigna los valores de cada propiedad a los TextViews
            public void bind(Property property, Map<String, List<Bitmap>> propertyImagesMap) {
                Log.d("Adapter", "Bind llamado con propiedad: " + property.toString());
                //Log.d("Adapter", "Y este es lo que se recibio de Map: " + propertyImagesMap);

                coordinatesTextView.setText(property.getCoordinates());
                priceTextView.setText(property.getPrice());
                availabilityTextView.setText(property.getAvailability());

                if (propertyImagesMap == null) {
                    Log.d("Adapter", "Mapa de imágenes es null. Mostrando placeholder y actualizando solo datos de la propiedad.");
                    imageView.setImageResource(R.drawable.placeholder);

                } else {
                    Log.d("Adapter", "Mapa de imágenes recibido: " + propertyImagesMap.toString());
                    List<Bitmap> images = propertyImagesMap.get(property.getId());

                    if (images != null && !images.isEmpty()) {
                        Log.d("Adapter", "Se encontró una imagen para la propiedad con ID: " + property.getId());
                        imageView.setImageBitmap(images.get(0));
                    } else {
                        Log.d("Adapter", "No se encontraron imágenes para la propiedad con ID: " + property.getId() + ". Mostrando placeholder.");
                        imageView.setImageResource(R.drawable.placeholder);
                    }
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Property {
        private String id;
        private String coordinates;
        private String price;
        private String availability;
        private List<String> characteristics;
        private List<String> images;

        public Property(String id, String coordinates, String price, String availability, List<String> characteristics, List<String> images) {
            this.id = id;
            this.coordinates = coordinates;
            this.price = price;
            this.availability = availability;
            this.characteristics = characteristics;
            this.images = images;
        }

        public String toString() {
            return "Property{" +
                    "id='" + id + '\'' +
                    ", coordinates='" + coordinates + '\'' +
                    ", price='" + price + '\'' +
                    ", availability='" + availability + '\'' +
                    ", characteristics=" + characteristics +
                    ", images=" + images +
                    '}';
        }

        //Métodos para acceder a los atributos de la clase
        public String getId(){
            return id;
        }
        public String getCoordinates(){
            return coordinates;
        }
        public String getPrice(){
            return price;
        }
        public String getAvailability(){
            return availability;
        }
        public List<String> getCharacteristics(){
            return characteristics;
        }
        public List<String> getImages() {return images;}
    }
}