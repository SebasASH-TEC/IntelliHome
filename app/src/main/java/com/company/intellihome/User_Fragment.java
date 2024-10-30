package com.company.intellihome;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;

//Fragmento de Arrendatario para manejar la interfaz.
public class User_Fragment extends Fragment {

    //Definir diferentes variables
    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList = new ArrayList<>();

    //Función para inflar la interfaz de User_Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_, container, false);

        HomeActivity homeActivity = (HomeActivity) getActivity();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PropertyAdapter(propertyList, getContext());
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
                Socket socket = new Socket("192.168.0.101", 1717);

                //Crea un objeto JSON para solicitar las propiedades al servidor.
                JSONObject requestData = new JSONObject();
                requestData.put("type", "getProperties");

                //Envía la solicitud al servidor.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(requestData.toString());

                //Lee la respuesta del servidor.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();

                Log.d("ServerResponse", "Respuesta del servidor: " + response);

                //Convierte las respuesta del servidor en una lista de String.
                String[] jsonArrayString = response.split("(?<=\\}])(?=\\[\\{)");
                Log.d("PropertiesArray", "Contenido del JSONArray: " + jsonArrayString.toString());

                propertyList.clear();

                AddPropertiesInList(jsonArrayString);
                Log.d("PropertyList", "Contenido de propertyList: " + propertyList.toString());
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void AddPropertiesInList(String[] propertiesArray) {
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

                    //Extrae los datos de las caracteristicas y los convierte en una lista
                    JSONArray characteristicsArray = propertyJson.getJSONArray("characteristics");
                    List<String> characteristics = new ArrayList<>();
                    for (int j = 0; j < characteristicsArray.length(); j++) {
                        characteristics.add(characteristicsArray.getString(j));
                    }

                    propertyList.add(new Property(id, coordinates, price, availability, characteristics));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

        private List<Property> propertyList;
        private Context context;

        public PropertyAdapter(List<Property> propertyList, Context context) {
            this.propertyList = propertyList;
            this.context = context;
        }

        @NonNull
        @Override
        //Crea una nueva instancia de ViewHolder inflando el layout de cada ítem de la lista
        public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.property_item, parent, false);
            return new PropertyViewHolder(view);
        }

        @Override
        //Vincula cada propiedad en la lista con el ViewHolder
        public void onBindViewHolder(PropertyViewHolder holder, int position) {
            Property property = propertyList.get(position);
            holder.bind(property);
        }

        @Override
        //Obtiene el número de elementos en la lista.
        public int getItemCount() {
            return propertyList.size();
        }

        //Clase interna para el ViewHolder que maneja la representación de cada ítem.
        static class PropertyViewHolder extends RecyclerView.ViewHolder {
            private TextView idTextView;
            private TextView coordinatesTextView;
            private TextView priceTextView;
            private TextView availabilityTextView;
            private TextView characteristicsTextView;

            //Inicializa los elemtos de la vista
            public PropertyViewHolder(View itemView) {
                super(itemView);
                idTextView = itemView.findViewById(R.id.property_id);
                coordinatesTextView = itemView.findViewById(R.id.property_coordinates);
                priceTextView = itemView.findViewById(R.id.property_price);
                availabilityTextView = itemView.findViewById(R.id.property_availability);
                characteristicsTextView = itemView.findViewById(R.id.property_characteristics);
            }

            //Asigna los valores de cada propiedad a los TextViews
            public void bind(Property property) {
                idTextView.setText(property.getId());
                coordinatesTextView.setText(property.getCoordinates());
                priceTextView.setText(property.getPrice());
                availabilityTextView.setText(property.getAvailability());
                characteristicsTextView.setText(TextUtils.join(",",  property.getCharacteristics()));
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

        public Property(String id, String coordinates, String price, String availability, List<String> characteristics) {
            this.id = id;
            this.coordinates = coordinates;
            this.price = price;
            this.availability = availability;
            this.characteristics = characteristics;
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
    }
}