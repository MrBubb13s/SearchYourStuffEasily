package com.example.searchyourstuffeasily;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;

//manifests에 activity로 추가됨(일자:24/04/01)
public class ImageUpload extends AppCompatActivity{
    ImageView imageView;
    Button btnUpload;
    Button btnRegister;
    Button btnDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        imageView = (ImageView) findViewById(R.id.Img_HouseFlat);
        btnUpload = (Button) findViewById(R.id.Btn_Upload);
        btnDraw = (Button) findViewById(R.id.Btn_Draw);
        btnRegister = (Button) findViewById(R.id.Btn_Register);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        btnUpload.setOnClickListener(this::onClickButton);
        btnDraw.setOnClickListener(this::onClickButton);
        btnRegister.setOnClickListener(this::onClickButton); //등록 창 미구현으로(일자:24/04/01) 버튼 클릭 시 메인화면으로 나가도록 지정함.
    }

    public void onClickButton(View view) {
        if(view.getId() == R.id.Btn_Upload)
            openGallery();
        //드로잉 페이지로 넘어가는 버튼 조건문 추가됨(일자:24/04/03)
        else if(view.getId() == R.id.Btn_Draw) {
            Intent nextPage = new Intent(getApplicationContext(), ImageDraw.class);
            startActivity(nextPage);
        }
        else{
            Intent nextPage = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(nextPage);
        }
    }

    private void openGallery() {
        Intent selectImage = new Intent(Intent.ACTION_PICK);
        selectImage.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");    //구글 드라이브(로그인 필요)와 포토에 저장된 이미지를 선택하는 설정
        selectImage.setAction(Intent.ACTION_GET_CONTENT);
        //selectImage.setType("image/*");   //구글 포토에 저장된 사진들만 선택 가능한 설정

        activityResultLauncher.launch(selectImage);
    }

    //이미지 파일을 저장소에서 가져오는 액티비티
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        Intent intent = result.getData();
                        Uri uri = intent.getData();

                        try{
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            });
}

