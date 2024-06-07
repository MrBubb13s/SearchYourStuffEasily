package com.example.searchyourstuffeasily.ui.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.searchyourstuffeasily.R;

import java.util.List;
import java.util.Map;

public class RoomButtonAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> roomNames;
    private Map<String, String> roomMap;
    onRoomButtonClickListener ClickListener;

    public interface onRoomButtonClickListener{
        void onRoomButtonClick(String roomName, String roomId);
    }
    public RoomButtonAdapter(Context context, List<String> names, Map<String, String> map, onRoomButtonClickListener listener){
        super(context, 0, names);
        this.context = context;
        this.roomNames = names;
        this.roomMap = map;
        this.ClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_room_button, parent, false);
        
        Button Btn_Room = convertView.findViewById(R.id.roomButton);
        String roomName = roomNames.get(position);

        Btn_Room.setText(roomName);
        Btn_Room.setTag(roomMap.get(roomName));

        int buttonSize = Btn_Room.getHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(parent.getWidth(), buttonSize);
        Btn_Room.setLayoutParams(params);

        Btn_Room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ClickListener != null)
                    ClickListener.onRoomButtonClick(roomName, roomMap.get(roomName));
            }
        });

        return convertView;
    }
}