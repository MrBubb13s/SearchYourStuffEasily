package com.example.searchyourstuffeasily;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.DialogInterface;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoodActivity extends AppCompatActivity {
    private Dialog dialog01;
    private AlarmManager alarmManager;
    Button dateset;
    DatePickerDialog datePickerDialog;
    Refrigerator Fridge;
    private String fridgeId;
    private DatabaseReference fridgeRef;
    private ListView listView;
    private ArrayAdapter<String> listViewAdapter;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri imageUri, currentImageUri;
    private StorageReference storageRef;
    private ImageView imageViewFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        requestPermissions();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        fridgeId = getIntent().getStringExtra("fridgeId");
        String fridgeName = getIntent().getStringExtra("fridgeName");
        String familyId = getIntent().getStringExtra("familyId");

        getSupportActionBar().setTitle(fridgeName);
        Fridge = new Refrigerator(fridgeName);

        dialog01 = new Dialog(this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_register_food);

        fridgeRef = FirebaseDatabase.getInstance().getReference().child("HomeDB").child(familyId).child("fridgeList").child(fridgeId);
        storageRef = FirebaseStorage.getInstance().getReference();

        listView = findViewById(R.id.FoodListView);
        listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listViewAdapter);

        //dialog상의 내용을 onCreate에서 먼저 처리함.
        imageViewFood = dialog01.findViewById(R.id.imageViewFood);
        Button Btn_ImgUpload = dialog01.findViewById(R.id.buttonUploadFood);
        Button Btn_Camera = dialog01.findViewById(R.id.buttonCameraFood);

        Btn_ImgUpload.setOnClickListener(new View.OnClickListener() {
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

        findViewById(R.id.AddFoodButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRegister();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDialogDelete(position);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialogAdjustByIndex(position);
            }
        });

        fridgeRef.child("foodList").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Food food = snapshot.getValue(Food.class);
                if (food != null) {
                    food.setId(snapshot.getKey());
                    food.setImageUrl(snapshot.child("imageUrl").getValue(String.class));
                    Fridge.addFood(food);

                    listViewAdapter.add(food.getName());
                    listViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String foodId = snapshot.getKey();
                Food updatedFood = snapshot.getValue(Food.class);

                if (updatedFood != null) {
                    updatedFood.setId(foodId);
                    updatedFood.setImageUrl(snapshot.child("imageUrl").getValue(String.class));
                    for (int i = 0; i < Fridge.getFlistSize(); i++) {
                        Food food = Fridge.getFoodByIndex(i);
                        if (food != null && food.getId() != null && food.getId().equals(foodId)) {
                            String oldName = food.getName();
                            Fridge.updateFood(i, updatedFood);

                            listViewAdapter.remove(oldName);
                            listViewAdapter.insert(updatedFood.getName(), i);
                            listViewAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String foodId = snapshot.getKey();
                for (int i = 0; i < Fridge.getFlistSize(); i++) {
                    Food food = Fridge.getFoodByIndex(i);
                    if (food != null && food.getId() != null && food.getId().equals(foodId)) {
                        Fridge.deleteFood(i);

                        listViewAdapter.remove(food.getName());
                        listViewAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_delete_fridge) {
            showDialogDeleteFridge();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogDeleteFridge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("냉장고 삭제")
                .setMessage("정말 이 냉장고를 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFridge();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public void showDialogRegister() {
        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog01.show();

        initDatePicker();
        dateset = (Button) dialog01.findViewById(R.id.expirationDate1);
        dateset.setText(getDate());

        Button Btn_Register = dialog01.findViewById(R.id.ActiveButton3);
        Button Btn_Close = dialog01.findViewById(R.id.CloseButton3);
        Btn_Register.setText("등록");

        EditText nameInput = dialog01.findViewById(R.id.nameInput1);
        EditText InfoInput = dialog01.findViewById(R.id.infoInput1);
        EditText countInput = dialog01.findViewById(R.id.countInput1);
        nameInput.getText().clear();
        InfoInput.getText().clear();
        countInput.getText().clear();
        imageViewFood.setImageResource(R.drawable.add_image_512);
        currentImageUri = null;

        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String foodName = nameInput.getText().toString().trim();
                String foodLocation = InfoInput.getText().toString().trim();
                String countInputStr = countInput.getText().toString().trim();
                String expirationDate = dateset.getText().toString();
                int count;

                if (foodName.isEmpty()) {                   // 이름 입력 체크
                    Toast.makeText(FoodActivity.this, "음식 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (foodLocation.isEmpty()) {                // 상세정보 입력 체크
                    Toast.makeText(FoodActivity.this, "상세 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (countInputStr.isEmpty()) {              // 수량 입력 체크
                    Toast.makeText(FoodActivity.this, "수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        count = Integer.parseInt(countInputStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(FoodActivity.this, "수량은 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Food food = new Food(UUID.randomUUID().toString(), foodName, foodLocation, count, expirationDate);

                //새로 생성한 food의 id 값으로 (이미지 업로드나 카메라를 클릭한 경우) 현재 저장된 이미지의 Uri를 storage에 저장함.
                if(currentImageUri != null)
                    uploadFoodImage(currentImageUri, food.getId());

                Map<String, Object> foodData = new HashMap<>();
                foodData.put("name", foodName);
                foodData.put("count", count);
                foodData.put("info", foodLocation);
                foodData.put("date", expirationDate);

                Query query = fridgeRef.child("foodList").orderByChild("name").equalTo(foodName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                            Toast.makeText(FoodActivity.this, "이미 존재하는 음식입니다.", Toast.LENGTH_SHORT).show();
                        else {
                            fridgeRef.child("foodList").child(food.getId()).setValue(foodData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Firebase", "음식 정보가 성공적으로 추가되었습니다.");
                                            setAlarm(expirationDate);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Firebase", "음식 정보 추가에 실패했습니다.", e);
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "음식 중복 확인에 실패했습니다.", error.toException());
                    }
                });

                nameInput.getText().clear();
                InfoInput.getText().clear();
                countInput.getText().clear();
                imageViewFood.setImageResource(0);
                dialog01.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.getText().clear();
                InfoInput.getText().clear();
                countInput.getText().clear();
                imageViewFood.setImageResource(0);
                dialog01.dismiss();
            }
        });
    }

    public void showDialogAdjustByIndex(int index) {
        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog01.show();

        if (index < 0 || index >= Fridge.getFlistSize()) {
            Log.e("FoodActivity", "Invalid index");
            return;
        }

        initDatePicker();
        Food food = Fridge.getFoodByIndex(index);
        final String itemId = food.getId();
        if(itemId == null) {
            Log.e("FoodActivity", "검색한 음식 또는 음식의 아이디가 null입니다.");
            return;
        }

        EditText NameInput = dialog01.findViewById(R.id.nameInput1);
        EditText PosInput = dialog01.findViewById(R.id.infoInput1);
        EditText CountInput = dialog01.findViewById(R.id.countInput1);
        dateset = dialog01.findViewById(R.id.expirationDate1);

        NameInput.getText().clear();
        PosInput.getText().clear();
        CountInput.getText().clear();
        currentImageUri = null;

        NameInput.setText(food.getName());
        PosInput.setText(food.getLocationInfo());
        CountInput.setText(String.valueOf(food.getCount()));
        dateset.setText(food.getExpirationDate());

        // 최신 이미지 URL을 가져와서 사용
        StorageReference imageRef = storageRef.child("foodImages/" + itemId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageUrl = uri.toString();
                Glide.with(FoodActivity.this).load(imageUrl).into(imageViewFood);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageViewFood.setImageResource(R.drawable.add_image_512); // 디폴트 이미지 설정
            }
        });

        Button Btn_Change = dialog01.findViewById(R.id.ActiveButton3);
        Button Btn_Delete = dialog01.findViewById(R.id.CloseButton3);
        Btn_Change.setText("변경");
        Btn_Delete.setText("삭제");

        Btn_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changedName = NameInput.getText().toString();
                String changedInfo = PosInput.getText().toString();

                int changedCount = Integer.parseInt(CountInput.getText().toString());
                Map<String, Object> foodUpdates = new HashMap<String, Object>();

                foodUpdates.put("name", changedName);
                foodUpdates.put("count", changedCount);
                foodUpdates.put("info", changedInfo);
                foodUpdates.put("date", dateset.getText().toString());

                if(currentImageUri != null)
                    uploadFoodImage(currentImageUri, itemId);

                fridgeRef.child("foodList").child(itemId).updateChildren(foodUpdates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FoodActivity", "Food updated successfully");

                                int index = Fridge.getIndexByFood(food);
                                if (index != -1) {
                                    food.setName(changedName);
                                    food.setLocationInfo(changedInfo);
                                    food.setCount(changedCount);
                                    food.setExpirationDate(dateset.getText().toString());
                                    listViewAdapter.notifyDataSetChanged();
                                }
                                onResume(); // 리스트뷰 갱신을 위해 onResume() 호출
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FoodActivity", "Failed to update food", e);
                            }
                        });

                NameInput.getText().clear();
                PosInput.getText().clear();
                CountInput.getText().clear();
                imageViewFood.setImageResource(0);
                dialog01.dismiss();
            }
        });
        Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fridgeRef.child("foodList").child(itemId).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FoodActivity", "Food deleted successfully");
                                Fridge.getFlist().remove(food);
                                listViewAdapter.notifyDataSetChanged();
                                onResume(); // 리스트뷰 갱신을 위해 onResume() 호출
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FoodActivity", "Failed to delete food", e);
                            }
                        });

                NameInput.getText().clear();
                PosInput.getText().clear();
                CountInput.getText().clear();
                imageViewFood.setImageResource(0);
                dialog01.dismiss();
            }
        });
    }

    private void showDialogDelete(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제 확인");
        builder.setMessage("선택한 음식을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (index >= 0 && index < Fridge.getFlistSize()) {
                    Food food = Fridge.getFoodByIndex(index);
                    fridgeRef.child("foodList").child(food.getId()).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("FoodActivity", "Food deleted successfully");
                                    Fridge.getFlist().remove(food);
                                    listViewAdapter.notifyDataSetChanged();
                                    imageViewFood.setImageResource(0);
                                    onResume(); // 리스트뷰 갱신을 위해 onResume() 호출
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("FoodActivity", "Failed to delete food", e);
                                }
                            });
                } else
                    Log.e("FoodActivity", "Invalid index");
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
    }

    private void startCameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File imgFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "food_image.jpg");
            imageUri =  FileProvider.getUriForFile(this, "com.example.searchyourstuffeasily.fileprovider", imgFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void deleteFridge() {
        String familyId = getIntent().getStringExtra("familyId");
        DatabaseReference fridgeRef = FirebaseDatabase.getInstance().getReference()
                .child("HomeDB")
                .child(familyId)
                .child("fridgeList")
                .child(fridgeId);
        fridgeRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FoodActivity.this, "냉장고가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent("action.FRIDGE_DELETED");
                        intent.putExtra("fridgeId", fridgeId);
                        LocalBroadcastManager.getInstance(FoodActivity.this).sendBroadcast(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FoodActivity.this, "냉장고 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setAlarm(String expirationDate) {
        Intent receiverIntent = new Intent(FoodActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(FoodActivity.this, 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE);
        String from = expirationDate + " 07:00:00";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년MM월dd일 HH:mm:ss");
        Date datetime = null;
        try {
            datetime = dateFormat.parse(from);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(datetime);

        alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);
    }

    private void uploadFoodImage(Uri imageUri, String foodId) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("foodImages/" + foodId);
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // 이미지 업로드 성공   //imageUrl 값을 fridgeRef에 저장하지 않아도 customAdapter 검색을 제외하곤 이상없음.
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        fridgeRef.child("foodList").child(foodId).child("imageUrl").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                            Toast.makeText(FoodActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(FoodActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(FoodActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Firebase에서 최신 정보 가져오기
        fridgeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 냉장고 정보가 존재하는 경우
                    Fridge.getFlist().clear();       // 기존 음식 목록 초기화
                    listViewAdapter.clear();    // 기존 리스트뷰 어댑터 초기화

                    for (DataSnapshot foodSnapshot : snapshot.child("foodList").getChildren()) {
                        String foodId = foodSnapshot.getKey();
                        String foodName = foodSnapshot.child("name").getValue(String.class);
                        String foodPlaceDetail = foodSnapshot.child("info").getValue(String.class);
                        int foodCount = foodSnapshot.child("count").getValue(Integer.class);
                        String foodExpirationDate = foodSnapshot.child("date").getValue(String.class);

                        Food food = new Food(foodId, foodName, foodPlaceDetail, foodCount, foodExpirationDate);
                        Fridge.addFood(food);
                        listViewAdapter.add(foodName);
                    }
                    listViewAdapter.notifyDataSetChanged(); // 리스트뷰 갱신
                } else
                    Log.e("FoodActivity", "Fridge data does not exist");        // 냉장고 정보가 존재하지 않는 경우
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FoodActivity", "Database error: " + error.getMessage());
            }
        });
    }

    @NonNull
    private String getDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return getDateToString(year, month, day);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String date = getDateToString(year, (month + 1), day);
                dateset.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    @NonNull
    private String getDateToString(int year, int month, int day) {
        Log.d("날짜 확인", year + "년" + month + "월" + day + "일");
        return year + "년" + month + "월" + day + "일";
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                imageViewFood.setImageURI(selectedImageUri);
                
                //이미지 업로드 성공 후 업로드한 이미지가 올바른 음식에 저장하기 위해 음식 등록/변경이 호출될 때만 uploadImageFood를 호출하도록 변경함.
                //대신 현재 업로드 된 이미지의 Uri값을 가지고 있도록 currentImageUri에 입력.(일자:24/06/22)
                currentImageUri = selectedImageUri;
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap imageBitmap = null;
                ImageDecoder.Source source = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    source = ImageDecoder.createSource(getContentResolver(), imageUri);
                    try {
                        imageBitmap = ImageDecoder.decodeBitmap(source);
                        imageViewFood.setImageBitmap(imageBitmap);
                        imageViewFood.setImageURI(imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try{
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imageViewFood.setImageBitmap(imageBitmap);
                        imageViewFood.setImageURI(imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                //이미지 업로드 성공 후 업로드한 이미지가 올바른 음식에 저장하기 위해 음식 등록/변경이 호출될 때만 uploadImageFood를 호출하도록 변경함.
                //대신 현재 업로드 된 이미지의 Uri값을 가지고 있도록 currentImageUri에 입력
                currentImageUri = imageUri;
            }
        }
    }
}