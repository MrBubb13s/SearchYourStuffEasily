package com.example.searchyourstuffeasily;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ImageDraw extends AppCompatActivity {
    DrawablePainter dPainter;
    ConstraintLayout cLayout;
    public FloatingActionButton fbDraw;
    public FloatingActionButton fbSave;
    public FloatingActionButton fbOpen;

    final static int LINE = 0;
    final static int RECT = 1;
    final static int ERAS = 2;
    static int CurrentShape = LINE;
    private String name = "FlatImage";;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        setTitle("단면도를 그려주세요.");

        cLayout = findViewById(R.id.canvas);
        dPainter = new DrawablePainter(this);
        fbDraw = findViewById(R.id.fl_Draw);
        fbSave = findViewById(R.id.fl_Save);
        fbOpen = findViewById(R.id.fl_Open);

        cLayout.addView(dPainter);
        fbDraw.setOnClickListener(this::onClickFloatingButton);
        fbSave.setOnClickListener(this::onClickFloatingButton);
        fbOpen.setOnClickListener(this::onClickFloatingButton);
    }

    private void onClickFloatingButton(View view) {
        switch(view.getId()){
            case R.id.fl_Draw:
                CurrentShape = LINE;
                break;
            case R.id.fl_Save:
                Toast.makeText(this, "Bitmap Saved!", Toast.LENGTH_SHORT).show();
                CurrentShape = ERAS;
                saveSequence(name);
                break;
            case R.id.fl_Open:
                openSequence(name);
        }
    }
    
    //캐시에 저장된 파일을 불러오는 함수, 아직 기능이 동작하지 않음 (일자:24/04/11)
    private void openSequence(String name) {
        ArrayList<String> FlatImages = new ArrayList<>();
        File file = new File(getCacheDir().toString());
        File[] list = file.listFiles();

        for(File f : list){
            Log.d("Tag", f.getName());

            if(f.getName().contains(name))
                FlatImages.add(f.getName());
        }

        Log.e("Tag", "FlatImage's size = "+ FlatImages.size());
        if(FlatImages.size() > 0){
            int randomPosition = new Random().nextInt(FlatImages.size());
            String filePath = getCacheDir() + "/" + FlatImages.get(randomPosition);

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        }
    }

    //캐시에 임시로 비트맵을 저장하는 함수, 형식은 png파일이며 추후 데이터베이스에 저장하는 코드로 수정해야함 (일자:24/04/10)
    private void saveSequence(String fileName) {
        File storage = getCacheDir();
        Bitmap saveBitmap = dPainter.getCurrentCanvas();
        String saveName = fileName + ".png";

        saveBitmap.eraseColor(Color.WHITE);
        dPainter.invalidate();

        File saveFile = new File(storage, saveName);
        try{
            saveFile.createNewFile();

            FileOutputStream out = new FileOutputStream(saveFile);
            saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (FileNotFoundException e){
            Log.e("Tag", "FileNotFoundException" + e.getMessage());
        }
        catch (IOException e) {
            Log.e("Tag", "IOException" + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0,"선 그리기");
        menu.add(0,1,0,"직사각형 그리기");
        menu.add(0,2,0,"초기화하기");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case LINE:
                CurrentShape = LINE;
                return true;
            case RECT:
                CurrentShape = RECT;
                return true;
            case ERAS:
                CurrentShape = ERAS;
                return true;
        }

        return false;
    }
}