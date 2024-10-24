package com.company.intellihome;

import java.text.AttributedString;
import java.util.List;

public class Property {
    private String id;
    private String coordinates;
    private String price;
    private String availability;
    private List<String> characteristics;

    public Property(String id, String coordinates, String price, String availability, List<String> characteristics)
    {
        this.id = id;
        this.coordinates = coordinates;
        this.price = price;
        this.availability = availability;
        this.characteristics = characteristics;
    }

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
