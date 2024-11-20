package com.company.intellihome;

import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, List<Bitmap>> propertyImagesMap = new HashMap<>();

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
        adapter = new User_Fragment.PropertyAdapter(propertyList, getContext(), propertyImagesMap);
        recyclerView.setAdapter(adapter);
        characteristicsProperty = new ArrayList<>();

        fetchFiltersProperties();
        return  view;
    }

    //Constructor de la clase
    public HouseFilters_Fragment(List<String> SelectedFilters, int price, int person) {
        this.selectedFilters = SelectedFilters;
        this.personSeekBar = person;
        this.priceSeekBar = price;
    }

    //Función para conseguir y mostrar las propiedades existentes
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

                tempList.clear();
                userFragment.AddPropertiesInList(jsonArrayString, tempList);

                for (int i = 0; i < tempList.size(); i++) {
                    characteristicsProperty = tempList.get(i).getCharacteristics();
                    String priceTemp = tempList.get(i).getPrice();
                    if (VerifyFilters(characteristicsProperty) && VerifySeekBar(priceTemp)) {
                        propertyList.add(tempList.get(i));
                    } else if (VerifySeekBar(priceTemp) && selectedFilters.isEmpty()) {
                        propertyList.add(tempList.get(i));
                    }
                }

                for (User_Fragment.Property property: propertyList) {
                    Log.d("Images", "Si entra a el for y este es el ID: " + property.getId());

                    userFragment.fetchPropertyImages(property.getId(), propertyImagesMap, adapter, getContext());

                    Log.d("Images", "Si entra a el for y esta es la propertyImagesMao: " + propertyImagesMap);
                }
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                Log.d("Despues de Activity", "Si se enviaron los datos");
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //Función para verificar los Filtros
    private boolean VerifyFilters(List<String> characteristics) {
        for (int i = 0; i < selectedFilters.size(); i++) {
            for (int j = 0; j < characteristics.size(); j++) {
                if (selectedFilters.get(i).equalsIgnoreCase(characteristics.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    //Función para verificar la SeeKBar del Precio
    protected boolean VerifySeekBar(String priceString) {
        String cleandePrice = priceString.replaceAll("[^\\d]", "");
        int price = Integer.parseInt(cleandePrice);
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
        private Map<String, List<Bitmap>> propertyImagesMap = new HashMap<>();

//     public void onViewCreated(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         if (getArguments() != null) {
//             String provincia = getArguments().getString("provincia");
//             Provincia = ConvertProvincias(provincia);
//         }
//     }

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
            adapter = new User_Fragment.PropertyAdapter(propertyList, getContext(), propertyImagesMap);
            recyclerView.setAdapter(adapter);

            fetchSearch();
            return view;
        }

        public static HouseSearch newInstance (String provincia) {
            HouseSearch fragment = new HouseSearch();
            Bundle args = new Bundle();
            args.putString("provincia", provincia);
            fragment.setArguments(args);
            Provincia = ConvertProvincias(provincia);
            return fragment;
        }

        //Función del Buscador
        public void fetchSearch() {
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

                   tempList.clear();
                   userFragment.AddPropertiesInList(jsonArrayString, tempList);

                   Log.d("Search", "Si se hizo una lista la tempList: " + tempList);
                   for (int i = 0; i < tempList.size(); i++) {
                       String tempCoordinates = tempList.get(i).getCoordinates();
                       tempCoordinates = tempCoordinates.replace("(", "").replace(")", "");
                       String[] splitCoordinates = tempCoordinates.split(",");

                       double latitud = Double.parseDouble(splitCoordinates[0].trim());
                       double longitud = Double.parseDouble(splitCoordinates[1].trim());

                       Entities.Provincias tempProvincia = entities.INProvincia(latitud, longitud);
                        if (Provincia.equals(tempProvincia)) {
                            propertyList.add(tempList.get(i));
                        }
                   }

                   for (User_Fragment.Property property: propertyList) {
                       Log.d("Images", "Si entra a el for y este es el ID: " + property.getId());

                       userFragment.fetchPropertyImages(property.getId(), propertyImagesMap, adapter, getContext());

                       Log.d("Images", "Si entra a el for y esta es la propertyImagesMao: " + propertyImagesMap);
                   }

                   Log.d("Adapter", "Este es el adapter: " + adapter);
                   Log.d("Adapter", "Y este es propertyList: " + propertyList);
                   getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                   socket.close();
               } catch (Exception e) {
                   e.printStackTrace();
                   getActivity().runOnUiThread(() ->
                           Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
               }
            }).start();
        }

        //Función para convertir las provincias
        protected static Entities.Provincias ConvertProvincias(String provincia) {
            String normalizedProvincia = Normalizer.normalize(provincia, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toLowerCase();

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