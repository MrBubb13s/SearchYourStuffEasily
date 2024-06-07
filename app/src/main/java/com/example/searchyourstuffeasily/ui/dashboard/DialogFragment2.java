package com.example.searchyourstuffeasily.ui.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.searchyourstuffeasily.R;
import androidx.fragment.app.DialogFragment;

public class DialogFragment2 extends DialogFragment {
    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_register_refrigerator, null);

        builder.setView(view).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText rfname = (EditText)getDialog().findViewById(R.id.EditTextInput1);
                        String Rfname = rfname.getText().toString();
                        //Rfname ---> 추가할 냉장고 이름
                        // Rfname이용해서 이곳에 코드 추가하면 DB에 추가 가능 ->구현이 안되있다는 뜻, DB추가 기능은 추후에 제작해야함(일자:24/05/10)
                    }
                });
        return builder.create();
    }
}