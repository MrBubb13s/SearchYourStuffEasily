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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnitureActivity extends AppCompatActivity {
    private Dialog dialog01, dialog02;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private StorageReference storageRef;
    private ImageView imageViewFurniture;
    private Uri imageUri, currentImageUri;
    Furniture furniture;
    String furnitureId;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference itemsRef, furnitureRef;
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
        }

        getSupportActionBar().setTitle(furnitureName);
        furniture = new Furniture(furnitureName);

        dialog01 = new Dialog(FurnitureActivity.this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_confirm_delete);

        dialog02 = new Dialog(FurnitureActivity.this);
        dialog02.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog02.setContentView(R.layout.dialog_register_product);

        //dialog상의 내용을 onCreate에서 우선 처리
        imageViewFurniture = dialog02.findViewById(R.id.imageViewFurniture);
        Button Btn_Upload = dialog02.findViewById(R.id.buttonUpload);
        Button Btn_Camera = dialog02.findViewById(R.id.buttonCamera);

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

        // Firebase Realtime Database 경로 설정     //가구와 물건은 별개의 db 경로를 가짐.
        itemsRef = mDatabase.child("homes").child(familyId).child("rooms").child(roomId)
                .child("furnitures").child(furnitureId).child("items");
        furnitureRef = mDatabase.child("HomeDB").child(familyId).child("roomList").child(roomId)
                .child("furnitureList").child(furnitureId);
        storageRef = FirebaseStorage.getInstance().getReference();

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
                        dataSnapshot.child("info").getValue(String.class), dataSnapshot.child("count").getValue(Integer.class));
                furniture.addProduct(p);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String oldName = furniture.getProductById(dataSnapshot.getKey()).getName();
                furniture.updateProduct(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class),
                        dataSnapshot.child("info").getValue(String.class), dataSnapshot.child("count").getValue(Integer.class));

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
        dialog02.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog02.show();

        EditText nameInput = dialog02.findViewById(R.id.nameInput2);
        EditText positionInput = dialog02.findViewById(R.id.infoInput2);
        EditText countInput = dialog02.findViewById(R.id.countInput2);

        Button Btn_Register = dialog02.findViewById(R.id.ActiveButton2);
        Button Btn_Close = dialog02.findViewById(R.id.CloseButton2);
        Btn_Register.setText("추가");

        nameInput.getText().clear();
        positionInput.getText().clear();
        countInput.getText().clear();
        imageViewFurniture.setImageResource(R.drawable.add_image_512);
        currentImageUri = null;

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
                String productId = product.getId();

                //이미지가 업로드되어 있으면 해당 이미지를 product의 id값으로 storage에 저장
                if(currentImageUri != null)
                    uploadProductImage(currentImageUri, productId);

                Map<String, Object> itemData = new HashMap<>();
                itemData.put("name", itemName);
                itemData.put("info", itemPosition);
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
                imageViewFurniture.setImageResource(0);
                dialog02.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput.getText().clear();
                positionInput.getText().clear();
                countInput.getText().clear();
                imageViewFurniture.setImageResource(0);
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
                itemsRef.removeValue()
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
                imageViewFurniture.setImageResource(0);
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
        currentImageUri = null;

        nameInput.setText(product.getName());
        positionInput.setText(product.getLocationInfo());
        countInput.setText(String.valueOf(product.getCount()));
        String itemId = product.getId();

        //FoodActivity의 사진 가져오기 로직과 통일
        StorageReference imageRef = storageRef.child("productImages/" + itemId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageUrl = uri.toString();
                Glide.with(FurnitureActivity.this).load(imageUrl).into(imageViewFurniture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageViewFurniture.setImageResource(R.drawable.add_image_512); // 디폴트 이미지 설정
            }
        });
        
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
                itemMap.put("info", changedInfo);

                if(currentImageUri != null)     //currentImageUri는 이미지 업로드나 카메라 버튼으로 이미지를 새로 등록한 경우에만 값을 갖게끔 수정함.(일자:24/06/23)
                    uploadProductImage(currentImageUri, itemId);        //이미지가 변경된 경우 해당 이미지를 itemId 명칭으로 storage에 재등록

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

                nameInput.getText().clear();
                positionInput.getText().clear();
                countInput.getText().clear();
                imageViewFurniture.setImageResource(0);
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
                
                nameInput.getText().clear();
                positionInput.getText().clear();
                countInput.getText().clear();
                imageViewFurniture.setImageResource(0);
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

                currentImageUri = selectedImageUri;
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
                currentImageUri = imageUri;
            }
        }
    }

    private void uploadProductImage(Uri imageUri, String productId) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("productImages/" + productId);
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // 이미지 업로드 성공       //customAdapter의 검색 기능에서 이미지를 표시하기 위해 itemsRef에 imageUrl 저장
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                   String downloadUrl = uri.toString();
                                   itemsRef.child(productId).child("imageUrl").setValue(downloadUrl)
                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if(task.isSuccessful())
                                                       Toast.makeText(FurnitureActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                                                   else
                                                       Toast.makeText(FurnitureActivity.this, "이미지 URL 저장 실패", Toast.LENGTH_SHORT).show();
                                               }
                                           });
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