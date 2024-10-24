package com.company.intellihome;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<Property> propertyList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_, container, false);

        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
        {
            homeActivity.showFilterMenu();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PropertyAdapter(propertyList, getContext());
        recyclerView.setAdapter(adapter);

        fetchProperties();
        return  view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

    }

    private void fetchProperties()
    {
        new Thread(() -> {
            try {
                Socket socket = new Socket("192.168.0.100", 1717);

                JSONObject requestData = new JSONObject();
                requestData.put("type", "getProperties");

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(requestData.toString());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();

                Log.d("ServerResponse", "Respuesta del servidor: " + response);

                JSONArray propertiesArray = new JSONArray(response);

                propertyList.clear();

                for (int i = 0; i < propertiesArray.length(); i++) {
                    JSONObject propertyJson = propertiesArray.getJSONObject(i);

                    String id = propertyJson.getString("id");
                    String coordinates = propertyJson.getString("coordinates");
                    String price = propertyJson.getString("price");
                    String availability = propertyJson.getString("availability");

                    JSONArray characteristicsArray = propertyJson.getJSONArray("characteristics");
                    List<String> characteristics = new ArrayList<>();
                    for (int j = 0; j < characteristicsArray.length(); j++) {
                        characteristics.add(characteristicsArray.getString(j));
                    }


                    propertyList.add(new Property(id, coordinates, price, availability, characteristics));
                }
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al obtener propiedades.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}