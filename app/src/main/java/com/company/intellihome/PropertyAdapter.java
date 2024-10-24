package com.company.intellihome;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private List<Property> propertyList;
    private Context context;

    public PropertyAdapter(List<Property> propertyList, Context context) {
        this.propertyList = propertyList;
        this.context = context;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.property_item, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {
        private TextView idTextView;
        private TextView coordinatesTextView;
        private TextView priceTextView;
        private TextView availabilityTextView;
        private TextView characteristicsTextView;

        public PropertyViewHolder(View itemView)
        {
            super(itemView);
            idTextView = itemView.findViewById(R.id.property_id);
            coordinatesTextView = itemView.findViewById(R.id.property_coordinates);
            priceTextView = itemView.findViewById(R.id.property_price);
            availabilityTextView = itemView.findViewById(R.id.property_availability);
            characteristicsTextView = itemView.findViewById(R.id.property_characteristics);
        }

        public void bind(Property property)
        {
            idTextView.setText(property.getId());
            coordinatesTextView.setText(property.getCoordinates());
            priceTextView.setText(property.getPrice());
            availabilityTextView.setText(property.getAvailability());
            characteristicsTextView.setText(TextUtils.join(",",  property.getCharacteristics()));
        }
    }
}
