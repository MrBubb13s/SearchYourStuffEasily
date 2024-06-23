package com.example.searchyourstuffeasily.ui.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.searchyourstuffeasily.GlobalVariable;
import com.example.searchyourstuffeasily.LoginActivity;
import com.example.searchyourstuffeasily.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.CLIPBOARD_SERVICE;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class NotificationsFragment extends Fragment {
    private Button Btn_PartIn;
    TextView name, email;
    ImageView profile;
    Button signOut, revoke, familyCode;
    private NotificationsViewModel notificationsViewModel;
    private Dialog dialog01;
    private String uid, familyId;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    GlobalVariable familyData;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        final TextView textView = root.findViewById(R.id.family_view);
        Btn_PartIn = root.findViewById(R.id.bt_Partin);

        profile = root.findViewById(R.id.image_Google);
        name = root.findViewById(R.id.id_Nname);
        email = root.findViewById(R.id.id_Email);

        signOut = root.findViewById(R.id.bt_Logout);
        revoke = root.findViewById(R.id.bt_Revoke);
        familyCode = root.findViewById(R.id.bt_FamilyCode);
        familyData = (GlobalVariable) getActivity().getApplicationContext();

        dialog01 = new Dialog(getActivity());
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog_all_purpose);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            // 사용자 정보 가져오기
            String userName = user.getDisplayName();
            String userEmail = user.getEmail();
            Uri userPhoto = user.getPhotoUrl(); //사용자의 이미지 Uri를 저장

            // 사용자 정보 설정
            if (userName != null)
                name.setText(userName);
            else
                name.setText("이름 없음");

            if (userEmail != null)
                email.setText(userEmail);
            else
                email.setText("이메일 없음");

            //if(userPhoto != null)
            //    profile.setImageURI(userPhoto);
            //else
                profile.setImageResource(R.drawable.ic_launcher_new);

            DatabaseReference userRef = mDatabase.child("users").child(uid);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        familyId = snapshot.getValue(String.class);
                        familyData.setfamilyId(familyId);
                        if (familyId != null) {
                            textView.setText(familyId);
                        } else {
                            Log.e("NotificationsFragment", "Family ID is null");
                            textView.setText("가족 코드가 없습니다.");
                        }
                    } else {
                        familyId = null;
                        familyData.setfamilyId(null);
                        textView.setText("가족 코드가 없습니다.");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 데이터베이스 에러 처리
                }
            });
        } else
            Log.e("NotificationsFragment", "User is not logged in");  // 사용자가 로그인하지 않은 경우 처리할 로직 작성

        Btn_PartIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPartIn();
            }
        });

        familyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("가족 코드", familyId);
                clipboard.setPrimaryClip(clip);
                openFamilyCodeFrag();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRevokeFrag();
            }
        });

        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    public void showDialogPartIn() {
        Button Btn_Search = dialog01.findViewById(R.id.button_all_purpose);
        Button Btn_Cancel = dialog01.findViewById(R.id.button_cancel);
        Btn_Search.setText("참여");

        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog01.show();
        EditText et_SearchName = dialog01.findViewById(R.id.editText_all_purpose);

        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFamilyCode = et_SearchName.getText().toString();

                // 입력한 가족 코드가 유효한지 확인
                DatabaseReference familyRef = mDatabase.child("HomeDB").child(newFamilyCode);
                familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // 유효한 가족 코드인 경우
                            joinFamily(newFamilyCode);
                        } else {
                            // 유효하지 않은 가족 코드인 경우
                            showInvalidFamilyCodeDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 데이터베이스 에러 처리
                    }
                });
            }
        });

        Btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("확인", "취소클릭됨");
                dialog01.dismiss();
            }
        });
    }
    private void joinFamily(String familyCode) {
        // 현재 사용자를 해당 가족 코드의 members 노드에 추가
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            DatabaseReference membersRef = mDatabase.child("HomeDB").child(familyCode).child("members");
            membersRef.child(uid).setValue(true);

            // 현재 사용자의 family 필드 업데이트
            DatabaseReference userRef = mDatabase.child("users").child(uid);
            userRef.setValue(familyCode);

            familyData.setfamilyId(familyCode);
            dialog01.dismiss();
        }
    }

    private void showNoFamilyCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("현재 가족 코드가 없습니다. 가족 참여 후에 코드를 복사할 수 있습니다.")
                .setPositiveButton("확인", null);
        builder.create().show();
    }

    private void showInvalidFamilyCodeDialog() {
        // 유효하지 않은 가족 코드 입력 시 보여줄 대화상자 구현
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("유효하지 않은 가족 코드입니다. 다시 확인해주세요.")
                .setPositiveButton("확인", null);
        builder.create().show();
    }

    private void openRevokeFrag() {
        RevokeFragment rvk = new RevokeFragment();
        rvk.setTargetFragment(this, 0);
        rvk.show(getFragmentManager(), "revoke");
    }

    private void openFamilyCodeFrag() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String familyCode = snapshot.getValue(String.class);
                        if (familyCode != null) {
                            FamilyCodeFragment fc = new FamilyCodeFragment();
                            fc.setFamilyCode(familyCode);
                            fc.setTargetFragment(NotificationsFragment.this, 0);
                            fc.show(getFragmentManager(), "familyCode");
                        } else
                            showNoFamilyCodeDialog();
                    } else
                        showNoFamilyCodeDialog();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 데이터베이스 에러 처리
                }
            });
        }
    }
}