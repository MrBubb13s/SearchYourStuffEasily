package com.example.searchyourstuffeasily;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ModifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        EditText et_ModifyName = (EditText)findViewById(R.id.editTextModifyname);
        EditText et_ModifyCount = (EditText)findViewById(R.id.editTextModifycount);

        et_ModifyName.setText("원래 물품 이름 DB에서 받아오기");
        et_ModifyCount.setText("원래 물품 수량 DB에서 받아오기");

        Button Btn_Confirm = (Button)findViewById(R.id.btn_modify_yes);
        Button Btn_Reject = (Button) findViewById(R.id.btn_modify_no);
        Btn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //확인버튼 클릭시
                Intent intent = new Intent(getApplicationContext(), FurnitureActivity.class);
                startActivity(intent);
            }
        });
        Btn_Reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //취소버튼 클릭시
                Intent intent = new Intent(getApplicationContext(), FurnitureActivity.class);
                startActivity(intent);
            }
        });
   }
}