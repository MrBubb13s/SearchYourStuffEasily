package com.example.searchyourstuffeasily;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class RoomActivity extends AppCompatActivity {
    private ImageView Img_Origin;
    private Dialog dialog01;
    private String familyId, roomId, roomName;
    private DatabaseReference mDatabase, roomRef;
    //private List<FurnitureInfo> furnitureList;        //Room.java의 리스트로 대체 시도 중, 이상 없을 시 주석 처리된 furnitureList 내용은 모두 삭제할 것(일자:24/06/11)
    Room room;
    private FrameLayout bot_frameLayout;
    private Map<String, ImageView> furnitureImageMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        bot_frameLayout = findViewById(R.id.bottom_container);
        Img_Origin = findViewById(R.id.original_image);

        Intent intent = getIntent();
        familyId = intent.getStringExtra("familyId");
        roomId = intent.getStringExtra("roomId");
        roomName = intent.getStringExtra("roomName");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        roomRef = mDatabase.child("HomeDB").child(familyId).child("roomList").child(roomId);

        room = new Room(roomId, roomName);
        //furnitureList = new ArrayList<>();
        furnitureImageMap = new HashMap<>();
        MyTouchListener touchListener = new MyTouchListener();
        MyDragListener dragListener = new MyDragListener();

        dialog01 = new Dialog(RoomActivity.this);       // 등록 기능 다이얼로그
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_register_furniture);

        if (((ViewGroup) Img_Origin.getParent()).getId() == R.id.top_container)     //원본 이미지가 top_container 내부에 있어야 터치리스너 사용 가능
            Img_Origin.setOnTouchListener(touchListener);
        bot_frameLayout.setOnDragListener(dragListener);

        Objects.requireNonNull(getSupportActionBar()).setTitle(roomName);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 방 정보가 존재하는 경우
                    FrameLayout bottomContainer = findViewById(R.id.bottom_container);
                    bottomContainer.removeAllViews();       // 기존의 뷰들을 제거

                    room.getFurnitureList().clear();
                    //furnitureList.clear();                  // 기존 가구 목록 초기화
                    furnitureImageMap.clear();              // 기존 가구 이미지 맵 초기화

                    for (DataSnapshot furnitureSnapshot : snapshot.child("furnitureList").getChildren()) {
                        String furnitureId = furnitureSnapshot.getKey();
                        String furnitureName = furnitureSnapshot.child("name").getValue(String.class);
                        String furnitureType = furnitureSnapshot.child("type").getValue(String.class);
                        float posX = furnitureSnapshot.child("posX").getValue(float.class);
                        float posY = furnitureSnapshot.child("posY").getValue(float.class);

                        FurnitureInfo furniture = new FurnitureInfo(furnitureId, furnitureName, posX, posY, furnitureType);
                        room.addRoom(furniture);
                        //furnitureList.add(furniture);

                        int resourceId = getResources().getIdentifier(furnitureType, "drawable", getPackageName());
                        ImageView furnitureImage = new ImageView(RoomActivity.this);
                        furnitureImage.setImageResource(resourceId);

                        int imageSize = getResources().getDimensionPixelSize(R.dimen.furniture_image_size);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);
                        params.leftMargin = (int) posX;
                        params.topMargin = (int) posY;
                        furnitureImage.setLayoutParams(params);

                        furnitureImage.setTag(furnitureId);
                        furnitureImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startFurnitureActivity(furniture);
                            }
                        });

                        bottomContainer.addView(furnitureImage);
                        furnitureImageMap.put(furnitureId, furnitureImage);
                    }
                } else {
                    // 방 정보가 존재하지 않는 경우
                    Room room = new Room(roomId, roomName);
                    roomRef.setValue(room);
                    //furnitureList = room.getFurnitureList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RoomActivity", "Database error: " + error.getMessage());
            }
        });
    }
    // 실시간 가구 이미지 삭제 매서드
    @Override
    protected void onResume() {
        super.onResume();

        // Firebase에서 최신 정보 가져오기
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 방 정보가 존재하는 경우
                    FrameLayout bottomContainer = findViewById(R.id.bottom_container);
                    bottomContainer.removeAllViews(); // 기존의 뷰들을 제거

                    room.getFurnitureList().clear();
                    //furnitureList.clear(); // 기존 가구 리스트 초기화
                    furnitureImageMap.clear(); // 기존 가구 이미지 맵 초기화

                    for (DataSnapshot furnitureSnapshot : snapshot.child("furnitureList").getChildren()) {
                        String furnitureId = furnitureSnapshot.getKey();
                        String furnitureName = furnitureSnapshot.child("name").getValue(String.class);
                        String furnitureType = furnitureSnapshot.child("type").getValue(String.class);
                        float posX = furnitureSnapshot.child("posX").getValue(float.class);
                        float posY = furnitureSnapshot.child("posY").getValue(float.class);

                        FurnitureInfo furniture = new FurnitureInfo(furnitureId, furnitureName, posX, posY, furnitureType);
                        room.addRoom(furniture);
                        //furnitureList.add(furniture);

                        int resourceId = getResources().getIdentifier(furnitureType, "drawable", getPackageName());
                        ImageView furnitureImage = new ImageView(RoomActivity.this);
                        furnitureImage.setImageResource(resourceId);

                        int imageSize = getResources().getDimensionPixelSize(R.dimen.furniture_image_size);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);
                        params.leftMargin = (int) posX;
                        params.topMargin = (int) posY;
                        furnitureImage.setLayoutParams(params);

                        furnitureImage.setTag(furnitureId);
                        furnitureImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startFurnitureActivity(furniture);
                            }
                        });

                        bottomContainer.addView(furnitureImage);
                    }
                } else {
                    // 방 정보가 존재하지 않는 경우
                    Room room = new Room(roomId, roomName);
                    roomRef.setValue(room);
                    //furnitureList = room.getFurnitureList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RoomActivity", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.furniture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.menu_drawer:
                Img_Origin.setImageResource(R.drawable.drawers);
                Img_Origin.setTag("drawers");
                return true;
            case R.id.menu_closet:
                Img_Origin.setImageResource(R.drawable.closet);
                Img_Origin.setTag("closet");
                return true;
            case R.id.menu_bed:
                Img_Origin.setImageResource(R.drawable.bed);
                Img_Origin.setTag("bed");
                return true;
            case R.id.menu_shelf:
                Img_Origin.setImageResource(R.drawable.shelf);
                Img_Origin.setTag("shelf");
                return true;
            case R.id.menu_bookshelf:
                Img_Origin.setImageResource(R.drawable.bookshelf);
                Img_Origin.setTag("bookshelf");
                return true;
            case R.id.menu_cabinet:
                Img_Origin.setImageResource(R.drawable.small_cabinet);
                Img_Origin.setTag("small_cabinet");
                return true;
            case R.id.menu_cupboard:
                Img_Origin.setImageResource(R.drawable.cupboard);
                Img_Origin.setTag("cupboard");
                return true;
            case R.id.menu_television:
                Img_Origin.setImageResource(R.drawable.television);
                Img_Origin.setTag("television");
                return true;
            case R.id.menu_wardrobe:
                Img_Origin.setImageResource(R.drawable.wardrobe);
                Img_Origin.setTag("wardrobe");
                return true;
            case R.id.menu_dressing:
                Img_Origin.setImageResource(R.drawable.dressing_table);
                Img_Origin.setTag("dressing_table");
                return true;
            case R.id.menu_glass:
                Img_Origin.setImageResource(R.drawable.glass_cabinet);
                Img_Origin.setTag("glass_cabinet");
                return true;
            case R.id.menu_desk:
                Img_Origin.setImageResource(R.drawable.desk);
                Img_Origin.setTag("desk");
                return true;
            case R.id.action_delete_room:
                showDeleteRoomDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteRoom() {
        roomRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RoomActivity.this, "방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RoomActivity.this, "방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startFurnitureActivity(@NonNull FurnitureInfo furniture) {
        Intent intent = new Intent(RoomActivity.this, FurnitureActivity.class);
        intent.putExtra("furnitureId", furniture.getId());
        intent.putExtra("furnitureName", furniture.getName());
        intent.putExtra("familyId", familyId);
        intent.putExtra("roomId", roomId);

        startActivity(intent);
    }

    private void addFurniture(String name, float posX, float posY, String type, View view) {
        String furnitureId = UUID.randomUUID().toString();
        FurnitureInfo furniture = new FurnitureInfo(furnitureId, name, posX, posY, type);

        room.addRoom(furniture);
        //furnitureList.add(furniture);
        furnitureImageMap.put(furnitureId, (ImageView) view);

        view.setTag(furnitureId);  // 가구의 ID를 ImageView의 태그로 설정
        Map<String, Object> furnitureData = new HashMap<>();
        furnitureData.put("name", name);
        furnitureData.put("posX", posX);
        furnitureData.put("posY", posY);
        furnitureData.put("type", type);

        DatabaseReference furnitureRef = roomRef.child("furnitureList").child(furnitureId);
        Query query = roomRef.child("furnitureList").orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                    Toast.makeText(RoomActivity.this, "이미 존재하는 가구 이름입니다.", Toast.LENGTH_SHORT).show();
                else {
                    furnitureRef.setValue(furnitureData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("RoomActivity", "Furniture added to database");
                                    Toast.makeText(RoomActivity.this, "가구가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("RoomActivity", "Failed to add furniture to database", e);
                                    Toast.makeText(RoomActivity.this, "가구 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomActivity.this, "가구 생성을 취소했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showDialogRegister(float posX, float posY, @NonNull View view) {
        Log.d("DialogRegister", "다이얼로그 호출됨");
        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog01.show();

        EditText editText_SearchName = dialog01.findViewById(R.id.edit_furniture_name);
        Button Btn_Register = dialog01.findViewById(R.id.ActiveButton1);
        Button Btn_Close = dialog01.findViewById(R.id.CloseButton1);
        String type = (String) view.getTag(); // 가구 타입 설정

        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onClick", "등록 버튼 클릭됨");
                String name = editText_SearchName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(RoomActivity.this, "가구 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type != null)
                    addFurniture(name, posX, posY, type, view);
                else
                    Log.e("RoomActivity", "가구 타입이 null입니다.");

                editText_SearchName.setText("");
                dialog01.dismiss();
                }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup owner = (ViewGroup) view.getParent();
                if (owner != null)
                    owner.removeView(view);

                editText_SearchName.setText("");
                dialog01.dismiss();
            }
        });
    }
    private void showDeleteRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("방 삭제")
                .setMessage("정말 이 방을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRoom();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View v, @NonNull MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if(v instanceof ImageView){
                    Log.d("MotionEvent", "이미지 최초 클릭됨.");
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

                    //ImageView를 터치하는 순간 ImageView를 복사하고 드래그 이벤트를 연결해 드롭할 수 있도록 조정(일자:24/05/17)
                    ImageView newImageView = new ImageView(RoomActivity.this);
                    String resName = (String) v.getTag();
                    int newId = View.generateViewId();

                    FrameLayout.LayoutParams originParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(originParams.width, originParams.height);

                    newImageView.setId(newId);
                    newImageView.setTag(resName);
                    newImageView.setImageResource(getResources().getIdentifier(resName, "drawable", getPackageName()));
                    newImageView.setVisibility(View.INVISIBLE);
                    bot_frameLayout.addView(newImageView, newParams);
                    //드롭이 끝나기 전, 이미지의 좌표가 존재하지 않을 때 bot_frameLayout의 좌측 상단에 뜨는 것을 방지하기 위해 투명화한 뒤 bot_frameLayout에 view 추가

                    newImageView.startDrag(data, shadowBuilder, newImageView, 0);
                }
                return true;
            } else
                return false;
        }
    }

    class MyDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, @NonNull DragEvent event) {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("DragEvent", "이미지 드래그 시작됨");
                    break;
                case DragEvent.ACTION_DROP:
                    //드래그한 이미지를 지역안에서 드롭했을떄
                    Log.d("DragEvent", "이미지 드롭됨");
                    if(v.getId() == R.id.bottom_container){
                        ImageView view = (ImageView) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        if(owner != null)
                            owner.removeView(view);

                        int dropX = (int) event.getX();
                        int dropY = (int) event.getY();
                        int parentX = v.getWidth();
                        int parentY = v.getHeight();

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        if((dropX + (view.getWidth() / 2)) > parentX)               //이미지가 우측으로 벗어났을 때
                            params.leftMargin = parentX - view.getWidth();
                        else if((dropX - (view.getWidth() / 2)) < (int)view.getX()) //이미지가 좌측으로 벗어났을 때
                            params.leftMargin = 0;
                        else                                                        //이미지가 온전히 부모 뷰 내에서 표시가 가능한 경우
                            params.leftMargin = dropX - (view.getWidth() / 2);

                        if((dropY + (view.getHeight() / 2)) > parentY)                  //이미지가 하단으로 벗어났을 때
                            params.topMargin = parentY - view.getWidth();
                        else if((dropY - (view.getHeight() / 2)) < (int)view.getY())    //이미지가 상단으로 벗어났을 때
                            params.topMargin = 0;
                        else                                                            //이미지가 온전히 부모 뷰 내에서 표시가 가능한 경우
                            params.topMargin = dropY - (view.getHeight() / 2);

                        view.setLayoutParams(params);
                        view.setTag(((ImageView) event.getLocalState()).getTag());

                        FrameLayout container = (FrameLayout) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);       //addView가 되기 전 투명화되었던 view가 배치가 완료될 때 다시 화면에 보이도록 설정
                        container.setVisibility(View.VISIBLE);

                        showDialogRegister(dropX, dropY, view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("OnClickListener", "배치된 이미지 클릭됨");
                                FurnitureInfo fInfo = null;
                                String furnitureId = (String) view.getTag();

                                for(FurnitureInfo furniture : room.getFurnitureList()){         //room.getFurnitureList()는 원래 furnitureList였음. 이상없으면 주석 제거할 것(일자:24/06/11)
                                    if(furniture.getId().equals(furnitureId))
                                        fInfo = furniture;
                                }

                                if(fInfo != null)
                                    startFurnitureActivity(fInfo);
                                else
                                    Log.e("RoomActivity", "클릭된 가구의 정보를 찾을 수 없습니다.");
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}