package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;

    //Decleration of Firebase Objects
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuthException firebaseAuthException;

    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialization of Firebase Objects
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Initialization of Email and Password EditTexts
        mEmailField = findViewById(R.id.emailEditText);
        mPasswordField = findViewById(R.id.passwordEditText);


        //Check and warn if input fields are empty
        mEmailField.addTextChangedListener(new TextWatcher() {
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
        });

        //Check and warn if input fields are empty
        mPasswordField.addTextChangedListener(new TextWatcher() {
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
        });

        //Buttons for login options
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.googleSignInButton).setOnClickListener(this);
        findViewById(R.id.facebookSignInButton).setOnClickListener(this);
        findViewById(R.id.signUpIntentButton).setOnClickListener(this);



        /*loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mEmailField != null || mPasswordField != null) {
                        SignInWithEmailAndPassword loginwEandP = new SignInWithEmailAndPassword(mEmailField, mPasswordField);
                        loginwEandP.signIn();
                    }
                } catch (Exception e) {
                    System.out.println("SignInWithEmailAndPassword failed.");
                }
            }
        });
        */
    }
    /*
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){return;}
    }

    */

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
                if(response==null){return;}
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
        switch (logInType) {
            case R.id.loginButton:
                //TODO login()
                break;
            case R.id.googleSignInButton:
                //ToDo googleSignIn();
                break;
            case R.id.facebookSignInButton:
                //ToDo facebookSignIn();
                break;
            case R.id.signUpIntentButton:
                Intent intentToSignUp = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intentToSignUp);
        }
    }

    //Update profile information for the logged in user
    protected void updateUI(FirebaseUser user) {
        if (user != null) {
            String userUid = user.getUid();
            //Get user info from db
            db.collection("users").document(userUid).get();
            //TODO setProfile();
        }else{return;}
    }
    /*
    //Getters and Setters
    public FirebaseAuth getmAuth() {
        return mAuth;
    }
   */
}
