package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.MyObjects.MyUser;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "Login Activity: ";

    //Decleration of Firebase Objects
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    MyUser currentUser = new MyUser();

    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialization of Firebase Objects
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //Initialization of Email and Password EditTexts
        mEmailField = findViewById(R.id.emailEditText);
        mPasswordField = findViewById(R.id.passwordEditText);


        mEmailField.addTextChangedListener(emailWatcher);
        mPasswordField.addTextChangedListener(passWatcher);


        //Buttons for login options
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.googleSignInButton).setOnClickListener(this);
        findViewById(R.id.facebookSignInButton).setOnClickListener(this);
        findViewById(R.id.signUpIntentButton).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response == null) {
                    return;
                }
                //TODO exceptionHandle();
                // ...
            }
        }
    }

    //Detect which login option has chosen
    //Detect if sign up function is chosen
    @Override
    public void onClick(View v) {
        int logInType = v.getId();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        switch (logInType) {
            case R.id.loginButton:
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                    goToProfilePage();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Giriş başarısız",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
                break;
            case R.id.googleSignInButton:
                //ToDo googleSignIn();
                break;
            case R.id.facebookSignInButton:
                //ToDo facebookSignIn();
                break;
            case R.id.signUpIntentButton:
                mPasswordField.removeTextChangedListener(passWatcher);
                mEmailField.removeTextChangedListener(emailWatcher);
                Intent intentToSignUp = new Intent(LoginActivity.this, SignUpActivity.class);
                finish();
                startActivity(intentToSignUp);
        }
    }

    //Update profile information for the logged in user
    protected void updateUI(FirebaseUser user) {
        if (user != null) {
            String userUid = user.getUid();
            //Get user info from db
            db.collection("users").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot user = task.getResult();
                        Map<String, Object> currentUserInfo = user.getData();

                        currentUser.setEmail(currentUserInfo.get("email").toString());
                        currentUser.setCompany(currentUserInfo.get("company").toString());
                        currentUser.setName(currentUserInfo.get("name").toString());
                        currentUser.setPhoneNumber(currentUserInfo.get("phoneNumber").toString());
                        currentUser.setTitel(currentUserInfo.get("titel").toString());
                        currentUser.setProfileImageUri(currentUserInfo.get("profileImage").toString());
                    }
                }
            });
        } else {
            return;
        }
    }

    public void goToProfilePage() {
        Intent goToProfile = new Intent(this, MainActivity.class);
        goToProfile.putExtra("currentUser", currentUser);
        finish();
        startActivity(goToProfile);
    }

    //Check and warn if input fields are empty
    TextWatcher emailWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEmailField.getText().toString().length() <= 0) {
                mEmailField.setError("Bu alan doldurulmalı");
            } else {
                mEmailField.setError(null);
            }
        }
    };

    //Check and warn if input fields are empty
    TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mPasswordField.getText().toString().length() <= 0) {
                mPasswordField.setError("Bu alan doldurulmalı");
            } else {
                mPasswordField.setError(null);
            }
        }
    };

}
