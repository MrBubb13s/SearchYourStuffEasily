package com.example.searchyourstuffeasily.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.searchyourstuffeasily.CustomAdapterFood;
import com.example.searchyourstuffeasily.Food;
import com.example.searchyourstuffeasily.FoodActivity;
import com.example.searchyourstuffeasily.R;
import com.example.searchyourstuffeasily.GlobalVariable;
import com.example.searchyourstuffeasily.ui.home.DialogFragment;
import com.example.searchyourstuffeasily.ui.home.RoomButtonAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DashboardFragment extends Fragment {
    private String familyId;
    private Dialog dialog01, dialog02, dialog03;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef;
    HashMap<String, String> fridgeMap = new HashMap<String, String>();
    GlobalVariable familyData;
    private RoomButtonAdapter roomButtonAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final Button Btn_AddFood = root.findViewById(R.id.AddFoodButton);
        final Button Btn_Search = root.findViewById(R.id.searchFoodButton);

        dialog01 = new Dialog(getActivity());
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_register_refrigerator);

        dialog02 = new Dialog(getActivity());
        dialog02.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog02.setContentView(R.layout.dialog_confirm_delete);

        dialog03 = new Dialog(getActivity());
        dialog03.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog03.setContentView(R.layout.dialog_all_purpose);

        familyData = (GlobalVariable) getActivity().getApplicationContext();
        String userId;
        List<String> foodNames = new ArrayList<>(fridgeMap.keySet());

        ListView listview = root.findViewById(R.id.listview_food);
        roomButtonAdapter = new RoomButtonAdapter(getActivity(), foodNames, fridgeMap, new RoomButtonAdapter.onRoomButtonClickListener() {
            @Override
            public void onRoomButtonClick(String fridgeName, String fridgeId) {
                // 버튼 클릭 이벤트 처리
                Intent intent = new Intent(requireActivity(), FoodActivity.class);
                intent.putExtra("fridgeId", fridgeId);
                intent.putExtra("fridgeName", fridgeName);
                intent.putExtra("familyId", familyId);

                startActivity(intent);
            }
        });
        listview.setAdapter(roomButtonAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    familyId = dataSnapshot.getValue(String.class);
                    if (familyId != null) {
                        conditionRef = mDatabase.child("HomeDB").child(familyId).child("fridgeList");
                        attachDatabaseReadListener();
                    } else
                        Log.e("DashboardFragment", "Family ID is null");
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("DashboardFragment", "Error getting family ID", databaseError.toException());
                }
            });
        } else
            Log.e("DashboardFragment", "User is not logged in");

        Btn_AddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("확인", "냉장고 프래그먼트 확인 버튼 눌림");
                showDialogRegister();
            }
        });
        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogSearch();
            }
        });
        return root;
    }

    private final BroadcastReceiver fridgeDeletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (Objects.equals(intent.getAction(), "action.FRIDGE_DELETED")) {       //intent.getAction().equals("action.FRIDGE_DELETED")
                String deletedFridgeId = intent.getStringExtra("fridgeId");
                removeFridgeFromListView(deletedFridgeId);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(fridgeDeletedReceiver,
                new IntentFilter("action.FRIDGE_DELETED"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(fridgeDeletedReceiver);
    }

    private void removeFridgeFromListView(String fridgeId) {
        String fridgeName = getKeyByValue(fridgeMap, fridgeId);
        if (fridgeName != null) {
            roomButtonAdapter.remove(fridgeName);
            fridgeMap.remove(fridgeName);
        }
    }

    private String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                return entry.getKey();
        }
        return null;
    }

    private void attachDatabaseReadListener() {
        conditionRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String fridgeName = dataSnapshot.child("fridgename").getValue(String.class);
                roomButtonAdapter.add(fridgeName);
                fridgeMap.put(fridgeName, dataSnapshot.getKey());

                Log.d("MainActivity", "ChildEventListener - onChildChanged : ");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("MainActivity", "ChildEventListener - onChildChanged : " + dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String fridgeName = dataSnapshot.child("fridgename").getValue(String.class);
                roomButtonAdapter.remove(fridgeName);
                fridgeMap.remove(fridgeName);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("MainActivity", "ChildEventListener - onChildMoved" + s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", "ChildEventListener - onCancelled" + databaseError.getMessage());
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String newRoomName = data.getStringExtra("roomname");
            String fridgeId = UUID.randomUUID().toString();
            Map<String, Object> taskMap = new HashMap<String, Object>();
            taskMap.put("fridgename", newRoomName);

            conditionRef.child(fridgeId).setValue(taskMap);
        }
    }

    public void showDialogRegister() {
        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog01.show();

        EditText et_SearchName = dialog01.findViewById(R.id.EditTextInput1);

        Button Btn_Register = dialog01.findViewById(R.id.ActiveButton1);
        Button Btn_Close = dialog01.findViewById(R.id.CloseButton1);

        Btn_Register.setText("추가");
        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fridgeName = et_SearchName.getText().toString().trim();
                if (fridgeName.isEmpty()) {
                    Toast.makeText(getActivity(), "냉장고 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fridgeId = UUID.randomUUID().toString();

                Map<String, Object> fridgeData = new HashMap<>();
                fridgeData.put("fridgename", fridgeName);

                Query query = conditionRef.orderByChild("fridgename").equalTo(fridgeName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                            Toast.makeText(familyData, "이미 존재하는 냉장고 이름입니다.", Toast.LENGTH_SHORT).show();
                        else {
                            conditionRef.child(fridgeId).setValue(fridgeData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("DashboardFragment", "냉장고 추가 성공");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("DashboardFragment", "냉장고 추가 실패", e);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "냉장고 추가에 실패했습니다.", error.toException());
                    }
                });

                et_SearchName.getText().clear();
                dialog01.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_SearchName.getText().clear();
                dialog01.dismiss();
            }
        });
    }

    private void showDialogSearch() {
        SearchDialogFragment fragment = new SearchDialogFragment(familyId);
        fragment.show(getParentFragmentManager(), "SearchDialogFragment");
    }

    public static class SearchDialogFragment extends DialogFragment implements SearchView.OnQueryTextListener {
        private final String familyId;
        private CustomAdapterFood adapter;
        private ArrayList<Food> foodList;

        public SearchDialogFragment(String familyId) {
            this.familyId = familyId;
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

            foodList = new ArrayList<>();
            adapter = new CustomAdapterFood(getContext(), R.layout.list_item, foodList);
            resultListView.setAdapter(adapter);

            getAllFoodNames();
            searchView.setOnQueryTextListener(this);
            resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Food selectedFood = (Food) parent.getItemAtPosition(position);
                    openFoodActivity(selectedFood);
                }
            });
            return builder.create();
        }

        private void getAllFoodNames() {
            DatabaseReference foodsRef = FirebaseDatabase.getInstance().getReference().
                    child("HomeDB").child(familyId).child("fridgeList");
            foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot fridge : snapshot.getChildren()) {
                        String fridgeId = fridge.getKey();
                        String fridgeName = fridge.child("fridgename").getValue(String.class);

                        DataSnapshot fSnapshot = fridge.child("foodList");
                        for (DataSnapshot foodSnapshot : fSnapshot.getChildren()) {
                            Food food = foodSnapshot.getValue(Food.class);
                            if(food != null){
                                food.setFridgeId(fridgeId);
                                food.setFridgeName(fridgeName);
                                foodList.add(food);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SearchDialogFragment", "Failed to retrieve item names", error.toException());
                }
            });
        }

        private void openFoodActivity(Food food) {
            Intent intent = new Intent(requireActivity(), FoodActivity.class);
            intent.putExtra("familyId", familyId);
            intent.putExtra("fridgeName", food.getFridgeName());
            intent.putExtra("foodId", food.getId());
            intent.putExtra("fridgeId", food.getFridgeId());

            startActivity(intent);
            dismiss();
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            adapter.getFilter().filter(query);
            return false;       //Override 필수 사항. 삭제 불가능
        }

        @Override
        public boolean onQueryTextChange(String query) {
            adapter.getFilter().filter(query);
            return false;
        }
    }
}