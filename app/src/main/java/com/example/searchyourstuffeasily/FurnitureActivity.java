package com.example.searchyourstuffeasily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnitureActivity extends AppCompatActivity {
    private Dialog dialog01, dialog02;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private StorageReference storageReference;
    private ImageView imageViewFurniture;
    Furniture furniture;
    String furnitureId;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference itemsRef,furnitureRef;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_furniture);
        getSupportActionBar().setTitle("Room");

        Intent intent = getIntent();
        String familyId = intent.getStringExtra("familyId");
        String roomId = intent.getStringExtra("roomId");
        furnitureId = intent.getStringExtra("furnitureId");

        Log.d("FurnitureActivity", "familyId: " + familyId);
        Log.d("FurnitureActivity", "roomId: " + roomId);
        Log.d("FurnitureActivity", "furnitureId: " + furnitureId);
        ListView listView_item = (ListView) findViewById(R.id.listview_item);
        Button btn_add_item = (Button) findViewById(R.id.btn_add_item);
        btn_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRegister();
            }
        });

        dialog01 = new Dialog(FurnitureActivity.this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_confirm_delete);

        dialog02 = new Dialog(FurnitureActivity.this);
        dialog02.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog02.setContentView(R.layout.dialog_register_product);

        //dialogView, dialog_add_item 내의 editText들의 사용처도 없어 삭제 처리함. 동일하게 inflater도 삭제함.(일자:24/06/10)

        Intent refnameIntent = getIntent();
        String furnitureName = refnameIntent.getStringExtra("furnitureName");
        if (furnitureName == null || furnitureName.trim().isEmpty()) {
            Toast.makeText(this, "물건 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();      //homefragment에서 furnitureacitivty를 호출할 때 꺼지는 이유
            finish(); // 가구 이름이 없으면 액티비티를 종료합니다.
            return;
        }
        getSupportActionBar().setTitle(furnitureName);

        // Firebase Realtime Database 경로 설정
        itemsRef = mDatabase.child("homes").child(familyId).child("rooms").child(roomId)
                .child("furnitures").child(furnitureId).child("items");
        furniture = new Furniture(furnitureName);

        furnitureRef = mDatabase.child("HomeDB").child(familyId).child("roomList").child(roomId)
                .child("furnitureList").child(furnitureId);

        // ListView, Adapter 생성 및 연결
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        listView_item.setAdapter(adapter);
        listView_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialogAdjustByIndex(position);
            }
        });

        itemsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.add(dataSnapshot.child("name").getValue(String.class));
                Product p = new Product(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class),
                        dataSnapshot.child("placeDetail").getValue(String.class), dataSnapshot.child("count").getValue(Integer.class));
                furniture.addProduct(p);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String oldName = furniture.getProductById(dataSnapshot.getKey()).getName();
                furniture.updateProduct(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class),
                        dataSnapshot.child("placeDetail").getValue(String.class), dataSnapshot.child("count").getValue(Integer.class));

                int pos = adapter.getPosition(oldName);
                adapter.remove(oldName);
                adapter.insert(dataSnapshot.child("name").getValue(String.class), pos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove(dataSnapshot.child("name").getValue(String.class));
                adapter.notifyDataSetChanged();
                furniture.manageProduct(dataSnapshot.child("name").getValue(String.class), 1);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trash_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_trash) {
            showDialogDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialogRegister() {
        dialog02.setContentView(R.layout.dialog_register_product);
        dialog02.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog02.show();

        EditText nameInput = dialog02.findViewById(R.id.nameInput2);
        EditText positionInput = dialog02.findViewById(R.id.infoInput2);
        EditText countInput = dialog02.findViewById(R.id.countInput2);

        imageViewFurniture = dialog02.findViewById(R.id.imageViewFurniture);
        storageReference = FirebaseStorage.getInstance().getReference();

        Button Btn_Register = dialog02.findViewById(R.id.ActiveButton2);
        Button Btn_Close = dialog02.findViewById(R.id.CloseButton2);
        Button Btn_Upload = dialog02.findViewById(R.id.buttonUpload);
        Button Btn_Camera = dialog02.findViewById(R.id.buttonCamera);
        Btn_Register.setText("추가");
        Btn_Close.setText("취소");

        Btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });
        Btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //if (intent.resolveActivity(getPackageManager()) != null)
                    //startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);      //deprecated된 기능, 카메라 실행 시 다이얼로그가 강제로 종료됨.(일자:24/06/08)
            }
        });

        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = nameInput.getText().toString().trim();
                String itemPosition = positionInput.getText().toString().trim();
                String countText = countInput.getText().toString().trim();
                int itemCount;

                if (itemName.isEmpty()) {
                    Toast.makeText(FurnitureActivity.this, "물건 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (itemPosition.isEmpty()) {
                    Toast.makeText(FurnitureActivity.this, "세부 장소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (countText.isEmpty()) {
                    Toast.makeText(FurnitureActivity.this, "수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        itemCount = Integer.parseInt(countText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(FurnitureActivity.this, "수량은 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //DatabaseReference itemRef = itemsRef.push();  //대신 랜덤id를 사용하는 product 생성
                Product product = new Product(UUID.randomUUID().toString(), itemName, itemPosition, itemCount);

                Map<String, Object> itemData = new HashMap<>();
                itemData.put("name", itemName);
                itemData.put("placeDetail", itemPosition);
                itemData.put("count", itemCount);

                //Query quEry = itemsRef.orderByKey().orderByChild("name").equalTo(itemName);     //furnitureRef와 비교하여 사용할 것
                Query query = furnitureRef.orderByChild("items").orderByChild("name").equalTo(itemName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                            Toast.makeText(FurnitureActivity.this, "이미 존재하는 가구 이름입니다.", Toast.LENGTH_SHORT).show();
                        else {
                            furnitureRef.child("items").child(product.getId()).setValue(itemData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(FurnitureActivity.this, "물건 추가에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(FurnitureActivity.this, "물건 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FurnitureActivity.this, "물건 추가를 취소했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
/*
                itemRef.setValue(itemData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FurnitureActivity.this, "물건이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FurnitureActivity.this, "물건 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });     */
                nameInput.setText("");
                positionInput.setText("");
                countInput.setText("");
                dialog02.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput.setText("");
                positionInput.setText("");
                countInput.setText("");
                dialog02.dismiss();
            }
        });
    }

    public void showDialogDelete() {
        dialog01.show();
        TextView Txt_Result = dialog01.findViewById(R.id.Confirm_Del_Text);
        Button Btn_Search = dialog01.findViewById(R.id.Confirm_Del_Button);

        Txt_Result.setTextSize(20);
        Txt_Result.setText("해당 가구를 삭제하시겠습니까?");

        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemsRef.removeValue();
                furnitureRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FurnitureActivity.this, "가구가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FurnitureActivity.this, "가구 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog01.dismiss();
            }
        });
    }

    public void showDialogAdjustByIndex(int index) {
        dialog02.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog02.show();

        Product product = furniture.getProductByIndex(index);
        EditText nameInput = dialog02.findViewById(R.id.nameInput2);
        EditText positionInput = dialog02.findViewById(R.id.infoInput2);
        EditText countInput = dialog02.findViewById(R.id.countInput2);

        nameInput.setText(product.getName());
        positionInput.setText(product.getLocationInfo());
        countInput.setText(String.valueOf(product.getCount()));
        String itemId = product.getId();

        Button Btn_Change = dialog02.findViewById(R.id.ActiveButton2);
        Button Btn_Delete = dialog02.findViewById(R.id.CloseButton2);
        Btn_Change.setText("변경");
        Btn_Delete.setText("삭제");

        Btn_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedName = nameInput.getText().toString();
                String changedInfo = positionInput.getText().toString();

                int changedCount = Integer.parseInt(countInput.getText().toString());
                Map<String, Object> itemMap = new HashMap<String, Object>();

                itemMap.put("name", changedName);
                itemMap.put("count", changedCount);
                itemMap.put("placeDetail", changedInfo);

                itemsRef.child(itemId).updateChildren(itemMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FurnitureActivity.this, "물건 정보가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FurnitureActivity.this, "물건 정보 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog02.dismiss();
            }
        });
        Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsRef.child(itemId).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(FurnitureActivity.this, "물건이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FurnitureActivity.this, "물건 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog02.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                imageViewFurniture.setImageURI(selectedImageUri);
                uploadFurnitureImage(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewFurniture.setImageBitmap(imageBitmap);
                Uri imageUri = getImageUri(imageBitmap);
                uploadFurnitureImage(imageUri);
            }
        }
    }

    private Uri getImageUri(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadFurnitureImage(Uri imageUri) {
        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("furnitureImages/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // 이미지 업로드 성공
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // 업로드된 이미지의 다운로드 URL 획득
                                    String imageUrl = uri.toString();
                                    // TODO: 이미지 URL을 데이터베이스에 저장하거나 필요한 곳에 사용
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // 이미지 업로드 실패
                            Toast.makeText(FurnitureActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}