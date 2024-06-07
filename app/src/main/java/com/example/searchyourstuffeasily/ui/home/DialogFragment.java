package com.example.searchyourstuffeasily.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.example.searchyourstuffeasily.R;

public class DialogFragment extends androidx.fragment.app.DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_register_refrigerator, null);

        builder.setView(view).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText)getDialog().findViewById(R.id.EditTextInput1);
                        String roomName = editText.getText().toString();     //roomName ---> 추가할 방 이름
                        // roomName 이용해서 이곳에 코드 추가하면 DB에 추가 가능 -> DB 추가 코드는 새로 작성해야함(일자:24/05/10)

                        Intent data = new Intent();
                        data.putExtra("roomName", roomName);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

                        dismiss();
                    }
                });
        return builder.create();
    }
}