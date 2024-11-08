package com.company.intellihome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HouseFilters_Fragment extends Fragment {
    //Definir diferentes variables
    RecyclerView recyclerView;
    private User_Fragment userFragment;
    private User_Fragment.PropertyAdapter adapter;
    private List<User_Fragment.Property> tempList;
    private List<User_Fragment.Property> propertyList;
    private List<String> characteristicsProperty;
    private List<String> selectedFilters;
    private int priceSeekBar;
    private int personSeekBar;

    private Entities entities = new Entities();



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_, container, false);

        propertyList = new ArrayList<>();
        tempList = new ArrayList<>();
        userFragment = new User_Fragment();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new User_Fragment.PropertyAdapter(propertyList, getContext());
        recyclerView.setAdapter(adapter);
        characteristicsProperty = new ArrayList<>();

        fetchFiltersProperties();
        return  view;
    }

    public HouseFilters_Fragment(List<String> SelectedFilters, int price, int person) {
        this.selectedFilters = SelectedFilters;
        this.personSeekBar = person;
        this.priceSeekBar = price;
        Log.d("Constructor", "Retorna el valor del precio: " + priceSeekBar);
        Log.d("Constructor", "FiltrosConstructor: " + selectedFilters);
    }

    private void fetchFiltersProperties() {
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
                Log.d("Verificación de Array", "Lista completa: " + Arrays.toString(jsonArrayString));

                tempList.clear();

                userFragment.AddPropertiesInList(jsonArrayString, tempList);
                Log.d("Verificación de temList", "Lista Temporal: " + tempList.toString());

                for (int i = 0; i < tempList.size(); i++) {
                    characteristicsProperty = tempList.get(i).getCharacteristics();
                    String priceTemp = tempList.get(i).getPrice();
                    Log.d("VerifyPrice", "El valor del Precio de la casa: " + priceTemp);
                    if (VerifyFilters(characteristicsProperty) && VerifySeekBar(priceTemp)) {
                        Log.d("Verificación", "Es verdadero los filtros");
                            Log.d("Verify", "Si es verdadero");
                            propertyList.add(tempList.get(i));
                    } else if (VerifySeekBar(priceTemp) && selectedFilters.isEmpty()) {
                        Log.d("Verify2", "Si es verdadero2");
                        propertyList.add(tempList.get(i));
                    }
                }

                Log.d("Verificación Lista", "Esta es la de propertyList: " + propertyList.toString());
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private boolean VerifyFilters(List<String> characteristics) {
        for (int i = 0; i < selectedFilters.size(); i++) {
            for (int j = 0; j < characteristics.size(); j++) {
                if (selectedFilters.get(i).equalsIgnoreCase(characteristics.get(j))) {
                    Log.d("VerifyFilters", "ES VERDADERO");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean VerifySeekBar(String priceString) {
        String cleandePrice = priceString.replaceAll("[^\\d]", "");
        int price = Integer.parseInt(cleandePrice);
        Log.d("Price", "Precio de la casa: " + price);

        return price <= priceSeekBar;
    }

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static class HouseSearch extends Fragment {
        RecyclerView recyclerView;
        private User_Fragment userFragment;
        private User_Fragment.PropertyAdapter adapter;
        private List<User_Fragment.Property> tempList;
        private List<User_Fragment.Property> propertyList;
        private Entities entities = new Entities();
        private static Entities.Provincias Provincia;

        public void onViewCreated(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                String provincia = getArguments().getString("provincia");
                Provincia = ConvertProvincias(provincia);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_user_, container, false);

            entities = new Entities();
            propertyList = new ArrayList<>();
            tempList = new ArrayList<>();
            userFragment = new User_Fragment();
            recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new User_Fragment.PropertyAdapter(propertyList, getContext());
            recyclerView.setAdapter(adapter);

            Log.d("Funcionamiento", "Antes de llamar a fetchSearch");
            fetchSearch();
            return  view;
        }

        public static HouseSearch newInstance (String provincia) {
            HouseSearch fragment = new HouseSearch();
            Bundle args = new Bundle();
            args.putString("provincia", provincia);
            fragment.setArguments(args);
            Log.d("Funcionamiento", "Si entro al Constructor y esta es la provincia: " + provincia);
            Provincia = ConvertProvincias(provincia);
            return fragment;
        }

        public void fetchSearch() {
            Log.d("featSearch", "Si me llama al inicio");
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
                   Log.d("Verificación de Array", "Lista completa: " + Arrays.toString(jsonArrayString));

                   tempList.clear();

                   userFragment.AddPropertiesInList(jsonArrayString, tempList);
                   Log.d("Verificación de temList", "Lista Temporal: " + tempList.toString());

                   for (int i = 0; i < tempList.size(); i++) {
                       String tempCoordinates = tempList.get(i).getCoordinates();
                       tempCoordinates = tempCoordinates.replace("(", "").replace(")", "");

                       String[] splitCoordinates = tempCoordinates.split(",");
                       Log.d("Ubicación", "Está es las coordenadas conevrtidas: " + Arrays.toString(splitCoordinates));

                       double latitud = Double.parseDouble(splitCoordinates[0].trim());
                       double longitud = Double.parseDouble(splitCoordinates[1].trim());

                       Log.d("UbicaciónInteger", "Esta es la latitud: " + latitud);
                       Log.d("UbicaciónInteger", "Esta es la longitud: " + longitud);
                       Log.d("Provincia del texto", "Esta es la provincia que se busco: " + Provincia);


                       Entities.Provincias tempProvincia = entities.INProvincia(latitud, longitud);
                       Log.d("Provincia", "Esta es la provincia que se identifico: " + tempProvincia.toString());

                        if (Provincia.equals(tempProvincia)) {
                            Log.d("Funcionamiento", "Si sirvio las provincias");
                            propertyList.add(tempList.get(i));
                        }

                   }


                   getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                   socket.close();

               } catch (Exception e) {
                   e.printStackTrace();
                   getActivity().runOnUiThread(() ->
                           Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
               }

            }).start();
        }

        protected static Entities.Provincias ConvertProvincias(String provincia) {
            String normalizedProvincia = Normalizer.normalize(provincia, Normalizer.Form.NFD)
                    .replace("\\p{M}", "").toLowerCase();

            switch (normalizedProvincia) {
                case "alajuela":
                    return Entities.Provincias.ALAJUELA;
                case "san jose":
                    return Entities.Provincias.SAN_JOSE;
                case "cartago":
                    return Entities.Provincias.CARTAGO;
                case "puntarenas":
                    return Entities.Provincias.PUNTARENAS;
                case "heredia":
                    return Entities.Provincias.HEREDIA;
                case "limon":
                    return Entities.Provincias.LIMON;
                case "guanacaste":
                    return Entities.Provincias.GUANACASTE;
                default:
                    return null;
            }

        }
    }
}