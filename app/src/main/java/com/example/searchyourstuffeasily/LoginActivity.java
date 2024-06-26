package com.example.searchyourstuffeasily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    static public Context context_login;
    static public String usersFamilyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        context_login = this;
        CreateRequest();

        // 자동 로그인 체크
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 이미 로그인된 사용자라면 메인 액티비티로 이동
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "자동 로그인 \n" + user.getEmail(), Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.bt_Login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void CreateRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mGoogleSignInClient.signOut();
        Log.d("LoginActivity", "signIn() called");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("LoginActivity", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                //db내에 저장된 사용자 정보와 가져온 Uid를 비교
                                usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            //FirebaseFirestore 내에 사용자의 Uid가 이미 존재하는 경우
                                            Toast.makeText(LoginActivity.this, "Firebase에 등록된 사용자 Uid로 로그인했습니다.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else{
                                            //FirebaseFirestore 내에 사용자의 Uid가 존재하지 않는 경우
                                            Random rand = new Random();
                                            String familyId = "familyId" + rand.nextInt();

                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                                            DatabaseReference familyRef = FirebaseDatabase.getInstance().getReference("HomeDB").child(familyId);

                                            // 사용자 정보를 저장합니다.
                                            userRef.child(userId).setValue(familyId);

                                            // 가족 정보에 사용자를 추가합니다.
                                            familyRef.child("members").child("userId").setValue(userId);
                                            usersFamilyId = familyId;

                                            Toast.makeText(LoginActivity.this, "Firebase에 사용자가 새로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("LoginActivity", "Firebase Database Search Failed", task.getException());
                                            Toast.makeText(LoginActivity.this, "Database 검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Log.e("LoginActivity", "Firebase authentication failed", task.getException());
                            Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
