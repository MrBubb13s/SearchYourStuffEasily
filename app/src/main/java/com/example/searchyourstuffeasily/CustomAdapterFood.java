package com.example.searchyourstuffeasily;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapterFood extends ArrayAdapter<Food> implements Filterable {
    private Context context;
    private int resource;
    private ArrayList<Food> originalList, filteredList;

    public CustomAdapterFood(Context context, int resource, ArrayList<Food> foodList) {
        super(context, resource, foodList);
        this.context = context;
        this.resource = resource;
        this.originalList = foodList;
        this.filteredList = foodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);

        ImageView foodImage = convertView.findViewById(R.id.item_image);
        TextView foodName = convertView.findViewById(R.id.item_name);
        TextView foodLocation = convertView.findViewById(R.id.item_room_furniture);

        Food food = filteredList.get(position);

        foodName.setText(food.getName());
        foodLocation.setText(food.getFridgeName());
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty())
            Glide.with(context).load(food.getImageUrl()).into(foodImage);
        else
            foodImage.setImageResource(R.drawable.ic_dir); // 기본 이미지 설정

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Food getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String sequence = charSequence.toString().toLowerCase();
                FilterResults results = new FilterResults();

                if(sequence.isEmpty()){
                    results.values = originalList;
                    results.count = originalList.size();
                } else {
                    ArrayList<Food> fList = new ArrayList<>();
                    for(Food f : originalList){
                        String compare = f.getName().toLowerCase();
                        if(compare.contains(sequence))
                            fList.add(f);
                    }
                    results.values = fList;
                    results.count = fList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                filteredList = (ArrayList<Food>) results.values;
                if(results.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        };
    }
}