package com.example.searchyourstuffeasily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnitureActivity extends AppCompatActivity {
    private Dialog dialog01, dialog02;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private StorageReference storageReference;
    private ImageView imageViewFurniture;
    private Uri imageUri;
    Furniture furniture;
    String furnitureId;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference itemsRef,furnitureRef;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_furniture);
        requestPermissions();

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

        Intent refnameIntent = getIntent();
        String furnitureName = refnameIntent.getStringExtra("furnitureName");
        if (furnitureName == null || furnitureName.trim().isEmpty()) {
            Toast.makeText(this, "물건 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            finish(); // 가구 이름이 없으면 액티비티를 종료합니다.
            return;
        } else if(furnitureName.equals("검색 결과가 속한 가구")){
            //db 경로를 통해 furnitureId와 동일한 id를 갖는 가구에서 이름을 받아오는 코드를 작성해야 함.
        }

        getSupportActionBar().setTitle(furnitureName);
        furniture = new Furniture(furnitureName);

        dialog01 = new Dialog(FurnitureActivity.this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_confirm_delete);

        dialog02 = new Dialog(FurnitureActivity.this);
        dialog02.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog02.setContentView(R.layout.dialog_register_product);

        // Firebase Realtime Database 경로 설정
        //가구와 물건이 별개의 db 경로를 가짐. 가구 내에 물건을 저장하도록 db를 수정하려면 itemsRef로 지정된 경로를 모두 furnitureRef로 변경해야함.
        itemsRef = mDatabase.child("homes").child(familyId).child("rooms").child(roomId)
                .child("furnitures").child(furnitureId).child("items");
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
                furniture.manageProduct(dataSnapshot.child("name").getValue(String.class), 1);

                adapter.remove(dataSnapshot.child("name").getValue(String.class));
                adapter.notifyDataSetChanged();
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
                startCameraIntent();
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

                Product product = new Product(UUID.randomUUID().toString(), itemName, itemPosition, itemCount);

                Map<String, Object> itemData = new HashMap<>();
                itemData.put("name", itemName);
                itemData.put("placeDetail", itemPosition);
                itemData.put("count", itemCount);

                Query query = itemsRef.orderByChild("name").equalTo(itemName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                            Toast.makeText(FurnitureActivity.this, "이미 존재하는 가구 이름입니다.", Toast.LENGTH_SHORT).show();
                        else {
                            itemsRef.child(product.getId()).setValue(itemData)
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

                nameInput.getText().clear();
                positionInput.getText().clear();
                countInput.getText().clear();
                dialog02.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput.getText().clear();
                positionInput.getText().clear();
                countInput.getText().clear();
                dialog02.dismiss();
            }
        });
    }

    public void showDialogDelete() {
        dialog01.show();
        TextView Txt_Message = dialog01.findViewById(R.id.Confirm_Del_Text);
        Button Btn_Delete = dialog01.findViewById(R.id.Confirm_Del_Button);

        Txt_Message.setTextSize(20);
        Txt_Message.setText("해당 가구를 삭제하시겠습니까?");

        Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemsRef.removeValue()          //원래는 itemsRef와 furnituresRef 둘 다 removeValue를 호출하고 아래 리스너들은 furnitureRef에 연결되어있었음.
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

    private void requestPermissions(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
    }

    private void startCameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File imgFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "furniture_image.jpg");
            imageUri =  FileProvider.getUriForFile(this, "com.example.searchyourstuffeasily.fileprovider", imgFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
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
                Bitmap imageBitmap = null;
                ImageDecoder.Source source = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    source = ImageDecoder.createSource(getContentResolver(), imageUri);
                    try {
                        imageBitmap = ImageDecoder.decodeBitmap(source);
                        imageViewFurniture.setImageBitmap(imageBitmap);
                        imageViewFurniture.setImageURI(imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try{
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageViewFurniture.setImageBitmap(imageBitmap);
                        imageViewFurniture.setImageURI(imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                uploadFurnitureImage(imageUri);
            }
        }
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