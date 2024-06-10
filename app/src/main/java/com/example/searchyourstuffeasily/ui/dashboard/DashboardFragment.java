package com.example.searchyourstuffeasily.ui.dashboard;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.searchyourstuffeasily.FoodActivity;
import com.example.searchyourstuffeasily.R;
import com.example.searchyourstuffeasily.GlobalVariable;
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
import java.util.UUID;

public class DashboardFragment extends Fragment {
    private DashboardViewModel dashboardViewModel;
    private String category, userId, familyId;
    private Dialog dialog01, dialog02, dialog03;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef;
    HashMap<String, String> fridgeMap = new HashMap<String, String>();
    GlobalVariable familyData;
    private RoomButtonAdapter roomButtonAdapter;
    private ListView listview;
    private List<String> foodNames;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final Button Btn_AddFood = root.findViewById(R.id.AddFoodButton);

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
        userId = familyData.getuId();
        foodNames = new ArrayList<>(fridgeMap.keySet());

        listview = root.findViewById(R.id.listview_food);
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

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }

    private BroadcastReceiver fridgeDeletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals("action.FRIDGE_DELETED")) {
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
                addToListView(fridgeName);
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
                removeFromListView(fridgeName);
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

    private void removeFromListView(String fridgeName) {
        roomButtonAdapter.remove(fridgeName);
    }

    private void addToListView(String fridgeName) {
        roomButtonAdapter.add(fridgeName);
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

                //DatabaseReference fridgeRef = mDatabase.child("HomeDB").child(familyId).child("fridgeList");      //하단의 query의 conditionRef와 같은 내용
                Query query = conditionRef.orderByChild("fridgename").equalTo(fridgeName);

                //방식 통일을 위한 테스트 코드, 정상 작동 할 시 아래 주석처리된 conditionRef 조건문은 삭제할것
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
                
/*                if(conditionRef != null){
                    conditionRef.child(fridgeId).setValue(fridgeData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("DashboardFragment", "냉장고 추가 성공");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("DashboardFragment", "냉장고 추가 실패", e);
                                }
                            });
                } else
                    Log.d("conditionRef", "conditionRef is null");      */

                dialog01.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog01.dismiss();
            }
        });
    }
    //사용처 없음 확인 후 삭제 예정.(일자:24/06/05)
    public void showDialogAdjust() {
        dialog03.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog03.show();

        EditText et_Name = dialog03.findViewById(R.id.editText_all_purpose);
        Button Btn_Change = dialog03.findViewById(R.id.button_all_purpose);
        Button Btn_Close = dialog03.findViewById(R.id.button_cancel);

        Btn_Change.setText("변경");
        Btn_Close.setText("삭제");
        et_Name.setHint(category);

        Btn_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DB변경 로직 작성
                dialog03.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogDelete();
                dialog03.dismiss();
            }
        });
    }

    public void showDialogDelete() {
        dialog02.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog02.show();

        TextView textView = dialog02.findViewById(R.id.Confirm_Del_Text);
        Button Btn_Delete = dialog02.findViewById(R.id.Confirm_Del_Button);

        textView.setText(category + "와\n" + category + "안의 목록을 정말 삭제하시겠습니까? ");
        textView.setTextSize(20);

        Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DB 삭제 로직 작성
                dialog02.dismiss();
            }
        });
    }
}