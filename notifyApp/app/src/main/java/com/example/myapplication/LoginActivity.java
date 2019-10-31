package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    /*
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    */

    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        */
        //Email and Password input
        mEmailField = findViewById(R.id.emailEditText);
        mPasswordField = findViewById(R.id.passwordEditText);

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
        //Buttons, LoginVerfahren
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
    public void onClick(View v) {
        int logInType = v.getId();
        switch (logInType){
            case R.id.loginButton:
                //ToDo loginWithEmailAndPassword();
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

    /*
    //Getters and Setters
    public FirebaseAuth getmAuth() {
        return mAuth;
    }
   */
}
