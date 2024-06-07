package com.example.searchyourstuffeasily.ui.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.searchyourstuffeasily.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RevokeFragment extends DialogFragment {
    public FirebaseAuth mAuth;
    DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference(); // 22
    DatabaseReference userID = mDatabase.child("UserDB");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("회원탈퇴 하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        deleteUser();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog -> 기능 구현이 필요함(일자:24/05/10)
                    }
                });
        return builder.create();
    }

    public void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //병합 후 오류 처리를 위해 임시 비활성화(일자:24/04/18)
                            //활성화 시 LoginActivity에 context_login이 없어 오류가 발생하니 브랜치 병합 과정 중 다시 확인 필요(일자:24/05/10)
                            //String cuid = ((LoginActivity)LoginActivity.context_login).cu;
                            //userID.child(cuid).removeValue();
                        }
                    }
                });
    }
}


