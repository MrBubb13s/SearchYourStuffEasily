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
    String famId = LoginActivity.usersFamilyId;
    DatabaseReference HomeDbRef = FirebaseDatabase.getInstance().getReference("HomeDB");
    DatabaseReference homesRef = FirebaseDatabase.getInstance().getReference("homes");
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("회원탈퇴 하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //사용자의 familyId와 연결된 내용 삭제 후 로그아웃 처리
                        deleteUser();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }

    public void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HomeDbRef.child(famId).removeValue();
                                homesRef.child(famId).removeValue();
                                usersRef.child(user.getUid()).removeValue();
                            }
                        }
                    });
        }
    }
}