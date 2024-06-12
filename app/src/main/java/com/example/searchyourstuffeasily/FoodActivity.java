package com.example.searchyourstuffeasily;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.DialogInterface;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private static final int REQUEST_IMAGE_PICK_FOOD = 3;
    private static final int REQUEST_IMAGE_CAPTURE_FOOD = 4;
    private StorageReference storageReference;
    private ImageView imageViewFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        fridgeId = getIntent().getStringExtra("fridgeId");
        String fridgeName = getIntent().getStringExtra("fridgeName");

        Fridge = new Refrigerator(fridgeName);
        String familyId = getIntent().getStringExtra("familyId");

        dialog01 = new Dialog(this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_register_food);

        fridgeRef = FirebaseDatabase.getInstance().getReference().child("HomeDB").child(familyId).child("fridgeList").child(fridgeId);
        storageReference = FirebaseStorage.getInstance().getReference();

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
                startActivityForResult(intent, REQUEST_IMAGE_PICK_FOOD);
            }
        });
        Btn_Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_FOOD);
                }
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

    private void showDialogSearch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_all_purpose, null);
        builder.setView(view);

        final EditText edit_Search = view.findViewById(R.id.editText_all_purpose);
        Button Btn_Search = view.findViewById(R.id.button_all_purpose);
        Button Btn_Close = view.findViewById(R.id.button_cancel);

        final AlertDialog dialog = builder.create();

        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = edit_Search.getText().toString().trim();
                if(query.isEmpty())             //내용이 길지 않아 searchFood 함수는 삭제하고 내부에 로직 구현
                    listView.clearTextFilter();
                else
                    listViewAdapter.getFilter().filter(query);
                
                edit_Search.getText().clear();
                dialog.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_Search.getText().clear();
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void showDialogRegister() {
        if (dialog01 == null) {
            Log.e("FoodActivity", "dialog01 is null");
            return;
        }
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

                Map<String, Object> foodData = new HashMap<>();
                foodData.put("name", foodName);
                foodData.put("count", count);
                foodData.put("placeDetail", foodLocation);
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

                            /*{             //이미 onChildAdd에서 Fridge에 음식을 추가함. 확인 후 이상 없으면 삭제할 것(일자:24/06/13)
                                    if (Fridge != null)
                                    Fridge.addFood(food);     // 로컬 Refrigerator 객체에 Food 추가
                                else
                                    Log.e("FoodActivity", "Fridge is null");
                            }*/
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
                dialog01.dismiss();
            }
        });
        Btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.getText().clear();
                InfoInput.getText().clear();
                countInput.getText().clear();
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

        NameInput.setText(food.getName());
        PosInput.setText(food.getLocationInfo());
        CountInput.setText(String.valueOf(food.getCount()));
        dateset.setText(food.getExpirationDate());

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
                foodUpdates.put("placeDetail", changedInfo);
                foodUpdates.put("date", dateset.getText().toString());

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
                                dialog01.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FoodActivity", "Failed to update food", e);
                            }
                        });
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
                                dialog01.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FoodActivity", "Failed to delete food", e);
                            }
                        });
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

    private Uri getImageUri(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadFoodImage(Uri imageUri) {
        if (imageUri != null) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("foodImages/" + imageName);

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
                            Toast.makeText(FoodActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle("Food");

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
                        String foodPlaceDetail = foodSnapshot.child("placeDetail").getValue(String.class);
                        int foodCount = foodSnapshot.child("count").getValue(Integer.class);
                        String foodExpirationDate = foodSnapshot.child("expirationDate").getValue(String.class);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_search:
                showDialogSearch();
                return true;
            case R.id.action_delete_fridge:
                showDialogDeleteFridge();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK_FOOD) {
                Uri selectedImageUri = data.getData();
                imageViewFood.setImageURI(selectedImageUri);

                uploadFoodImage(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE_FOOD) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewFood.setImageBitmap(imageBitmap);
                Uri imageUri = getImageUri(imageBitmap);

                uploadFoodImage(imageUri);
            }
        }
    }
}
