package com.example.searchyourstuffeasily;

import android.content.Context;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Product> implements Filterable {
    private Context context;
    private int resource;
    private List<Product> originalList, filteredList;

    public CustomAdapter(Context context, int resource, ArrayList<Product> itemList) {
        super(context, resource, itemList);
        this.context = context;
        this.resource = resource;
        this.originalList = itemList;
        this.filteredList = itemList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);
        }

        ImageView itemImage = convertView.findViewById(R.id.item_image);
        TextView itemName = convertView.findViewById(R.id.item_name);
        TextView itemLocation = convertView.findViewById(R.id.item_room_furniture);

        Product product = filteredList.get(position);

        itemName.setText(product.getName());
        if (product.getRoomName() != null && product.getFurnitureName() != null)
            itemLocation.setText(product.getRoomName() + " - " + product.getFurnitureName());
         else
            itemLocation.setText("Unknown Room - Unknown Furniture");

         //해당 코드를 위해 uploadImage 메서드가 db에 imageUrl을 저장함.
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty())
            Glide.with(context).load(product.getImageUrl()).into(itemImage);
         else
            itemImage.setImageResource(R.drawable.ic_dir); // 기본 이미지 설정

        return convertView;
    }

    @Override
    public int getCount(){
        return filteredList.size();
    }
    @Override
    public Product getItem(int pos){
        return filteredList.get(pos);
    }
    @Override
    public long getItemId(int pos) {
        return pos;
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
                    ArrayList<Product> pList = new ArrayList<>();
                    for(Product p : originalList){
                        String compare = p.getName().toLowerCase();
                        if(compare.contains(sequence))
                            pList.add(p);
                    }
                    results.values = pList;
                    results.count = pList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                filteredList = (ArrayList<Product>) results.values;
                if(results.count > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }
        };
    }
}