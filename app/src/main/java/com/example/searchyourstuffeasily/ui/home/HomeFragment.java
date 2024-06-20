package com.example.searchyourstuffeasily.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.searchyourstuffeasily.CustomAdapter;
import com.example.searchyourstuffeasily.FurnitureActivity;
import com.example.searchyourstuffeasily.Product;
import com.example.searchyourstuffeasily.R;
import com.example.searchyourstuffeasily.Room;
import com.example.searchyourstuffeasily.RoomActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.core.QueryListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private String familyId;
    private DatabaseReference mDatabase;
    private RoomButtonAdapter roomButtonAdapter;
    private ListView listview;
    private List<String> roomNames;
    private Map<String, String> roomMap;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Button Btn_AddRoom = rootView.findViewById(R.id.AddRoomButton);
        Button Btn_Search = rootView.findViewById(R.id.searchButton);
        listview = rootView.findViewById(R.id.PlaceListView);
        roomMap = new HashMap<>();
        roomNames = new ArrayList<>(roomMap.keySet());      //Adapter에 roomMap의 key값을 전달하기 위한 ArrayList

        roomButtonAdapter = new RoomButtonAdapter(getActivity(), roomNames, roomMap, new RoomButtonAdapter.onRoomButtonClickListener() {
            @Override
            public void onRoomButtonClick(String roomName, String roomId) {
                Intent intent = new Intent(requireActivity(), RoomActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                intent.putExtra("familyId", familyId);

                startActivity(intent);
            }
        });
        listview.setAdapter(roomButtonAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        loadRoomData();

        Btn_AddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRoomDialog();
            }
        });
        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });

        return rootView;
    }

    private void loadRoomData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    familyId = snapshot.getValue(String.class);
                    if (familyId != null) {
                        DatabaseReference roomRef = mDatabase.child("HomeDB").child(familyId).child("roomList");
                        roomRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                String roomId = snapshot.getKey();
                                String roomName = snapshot.child("roomName").getValue(String.class);
                                roomButtonAdapter.add(roomName);
                                roomMap.put(roomName, roomId);
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                // 변경된 방 처리
                                String roomId = snapshot.getKey();
                                String roomName = snapshot.child("roomName").getValue(String.class);
                                String oldRoomName = getKeyByValue(roomMap, roomId);
                                if (oldRoomName != null) {
                                    int index = roomButtonAdapter.getPosition(oldRoomName);
                                    if (index >= 0) {
                                        roomButtonAdapter.remove(oldRoomName);
                                        roomButtonAdapter.insert(roomName, index);
                                        roomMap.remove(oldRoomName);
                                        roomMap.put(roomName, roomId);
                                    }
                                }
                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                // 삭제된 방 처리
                                String roomId = snapshot.getKey();
                                String roomName = getKeyByValue(roomMap, roomId);
                                if (roomName != null) {
                                    roomButtonAdapter.remove(roomName);
                                    roomMap.remove(roomName);
                                }
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                // 이동된 방 처리 (필요한 경우)
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("HomeFragment", "Database error: " + error.getMessage());
                            }
                        });
                    } else
                        Log.e("HomeFragment", "Family ID is null");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeFragment", "Failed to retrieve family ID", error.toException());
                }
            });
        } else
            Log.e("HomeFragment", "User is not logged in");
    }

    private void addRoom(String roomName) {
        if (familyId == null) {
            Log.e("HomeFragment", "Family ID is null");
            return;
        }

        DatabaseReference roomRef = mDatabase.child("HomeDB").child(familyId).child("roomList");
        Query query = roomRef.orderByChild("roomName").equalTo(roomName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 동일한 방 이름이 이미 존재하는 경우
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "이미 존재하는 방 이름입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    String roomId = roomRef.push().getKey();
                    Room room = new Room(roomId, roomName);
                    roomRef.child(roomId).setValue(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Failed to add room", error.toException());
            }
        });
    }

    private String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                return entry.getKey();
        }
        return null;
    }

    private void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("방 추가");

        final EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomName = input.getText().toString().trim();
                if (TextUtils.isEmpty(roomName))
                    Toast.makeText(getActivity(), "방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                else {
                    addRoom(roomName);
                    input.getText().clear();
                }
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input.getText().clear();
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showSearchDialog() {
        SearchDialogFragment SDFragment = new SearchDialogFragment(familyId, roomMap);
        SDFragment.show(getParentFragmentManager(), "SearchDialogFragment");
    }

    public static class SearchDialogFragment extends DialogFragment implements SearchView.OnQueryTextListener {
        private final String familyId;
        private final Map<String, String> roomMap;
        private CustomAdapter adapter;
        private ArrayList<Product> products;

        public SearchDialogFragment(String familyId, Map<String, String> roomMap) {
            this.familyId = familyId;
            this.roomMap = roomMap;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_search, null);
            builder.setView(view);

            SearchView searchView = view.findViewById(R.id.searchView);
            ListView resultListView = view.findViewById(R.id.searchListView);

            products = new ArrayList<>();
            adapter = new CustomAdapter(getContext(), R.layout.list_item, products);
            resultListView.setAdapter(adapter);

            //문자열 리스트 itemList는 사용하지 않아 삭제함. 대신 itemList가 호출하던 함수는 리턴값이 없는 함수로 변경함.(일자:24/06/12)
            getAllItemNames();
            searchView.setOnQueryTextListener(this);
            resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Product selectedItem = (Product) parent.getItemAtPosition(position);
                    openFurnitureActivity(selectedItem);
                }
            });
            return builder.create();
        }

        private void getAllItemNames() {
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().
                    child("homes").child(familyId).child("rooms");
            itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        String roomId = roomSnapshot.getKey();
                        String roomName = getRoomNameById(roomId);
                        Log.d("getAllItems", "RoomId: " + roomId + ", RoomName: " + roomName);

                        DataSnapshot fSnapshot = roomSnapshot.child("furnitures");
                        for (DataSnapshot furniture : fSnapshot.getChildren()) {
                            String furnitureId = furniture.getKey();
                            getFurnitureNameById(furnitureId, roomId, new OnFurnitureNameRetrievedListener(){
                                @Override
                                public void onFurnitureNameRetrieved(String furnitureName){
                                    Log.d("getAllitems", "furnitureId: " + furnitureId + ", furnitureName: " + furnitureName);

                                    DataSnapshot iSnapshot = furniture.child("items");
                                    for (DataSnapshot itemSnapshot : iSnapshot.getChildren()) {
                                        String itemName = itemSnapshot.child("name").getValue(String.class);
                                        String itemId = itemSnapshot.getKey();
                                        String itemImageUrl = itemSnapshot.child("imageUrl").getValue(String.class);
                                        Integer itemCount = itemSnapshot.child("count").getValue(Integer.class);
                                        Log.d("getAllItems", "ItemId: " + itemId + ", ItemName: " + itemName);

                                        Product p = new Product(itemId, itemName, roomName + " - " + furnitureName, itemCount != null ? itemCount : 0);
                                        p.setImageUrl(itemImageUrl);
                                        p.setRoomName(roomName != null ? roomName : "Unknown Room");
                                        p.setFurnitureName(furnitureName != null ? furnitureName : "Unknown Furniture");

                                        products.add(p);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SearchDialogFragment", "Failed to retrieve item names", error.toException());
                }
            });
        }

        interface OnFurnitureNameRetrievedListener {
            void onFurnitureNameRetrieved(String furnitureName);
        }

        interface OnFurnitureIdRetrievedListener {
            void onFurnitureIdRetrieved(String furnitureId);
        }

        private void getFurnitureNameById(String furnitureId, String roomId, OnFurnitureNameRetrievedListener listener){
            DatabaseReference fRef = FirebaseDatabase.getInstance().getReference().
                    child("HomeDB").child(familyId).child("roomList").
                    child(roomId).child("furnitureList").child(furnitureId);
            fRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String furnitureName = snapshot.child("name").getValue(String.class);
                    listener.onFurnitureNameRetrieved(furnitureName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("getFurnitureNameById", "Failed to retrieve furniture name", error.toException());
                }
            });
        }

        private void getFurnitureIdByName(String roomId, String furnitureName, OnFurnitureIdRetrievedListener listener) {
            DatabaseReference fRef = FirebaseDatabase.getInstance().getReference().
                    child("HomeDB").child(familyId).child("roomList").
                    child(roomId).child("furnitureList");
            fRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot furnitureSnapshot : snapshot.getChildren()) {
                        if (furnitureSnapshot.child("name").getValue(String.class).equals(furnitureName)) {
                            listener.onFurnitureIdRetrieved(furnitureSnapshot.getKey());
                            return;
                        }
                    }
                    listener.onFurnitureIdRetrieved(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SearchDialogFragment", "Failed to retrieve furniture ID", error.toException());
                }
            });
        }

        private String getRoomNameById(String id){
            for(Map.Entry<String, String> entry : roomMap.entrySet()){
                if(entry.getValue().equals(id))
                    return entry.getKey();
            }
            return null;
        }

        private String getRoomIdByName(String name){
            for(Map.Entry<String, String> entry : roomMap.entrySet()){
                if(entry.getKey().equals(name))
                    return entry.getValue();
            }
            return null;
        }

        private void openFurnitureActivity(Product product) {
            String roomId = getRoomIdByName(product.getRoomName());
            getFurnitureIdByName(roomId, product.getFurnitureName(), new OnFurnitureIdRetrievedListener() {
                @Override
                public void onFurnitureIdRetrieved(String furnitureId) {
                    String furnitureName = product.getFurnitureName();

                    Intent intent = new Intent(requireActivity(), FurnitureActivity.class);
                    intent.putExtra("familyId", familyId);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("furnitureId", furnitureId);
                    intent.putExtra("furnitureName", furnitureName);

                    startActivity(intent);
                    dismiss();
                }
            });
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            adapter.getFilter().filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            adapter.getFilter().filter(query);
            return false;
        }
    }
}