package com.example.notify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends LoginActivity implements View.OnClickListener {


    private static final String TAG = "Sign Up Activity: ";

    private EditText userName;
    private EditText email;
    private EditText phoneNum;
    private EditText titel;
    private EditText company;
    private EditText password;
    private EditText passwordCont;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userName = findViewById(R.id.signUpName);
        email = findViewById(R.id.signUpEmail);
        phoneNum = findViewById(R.id.signUpPhoneNumber);
        titel = findViewById(R.id.signUpTitel);
        company = findViewById(R.id.signUpCompany);
        password = findViewById(R.id.signUpPassword);
        passwordCont = findViewById(R.id.signUpCheckPassword);
        findViewById(R.id.signUpButton).setOnClickListener(this);
        findViewById(R.id.signUpBackArrow).setOnClickListener(this);

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userName.getText().toString().length() <= 0) {
                    userName.setError("Bu alan doldurulmalı");
                } else {
                    userName.setError(null);
                }
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (email.getText().toString().length() <= 0) {
                    email.setError("Bu alan doldurulmalı");
                } else {
                    email.setError(null);
                }
            }
        });

        phoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneNum.getText().toString().length() <= 0) {
                    phoneNum.setError("Bu alan doldurulmalı");
                } else {
                    phoneNum.setError(null);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (password.getText().toString().length() <= 0) {
                    password.setError("Bu alan doldurulmalı");
                } else {
                    password.setError(null);
                }
            }
        });

        passwordCont.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordCont.getText().toString().length() <= 0) {
                    passwordCont.setError("Bu alan doldurulmalı");
                } else {
                    passwordCont.setError(null);
                }
            }
        });

    }

    private boolean validateForm() {
        boolean validForm = false;
        if (TextUtils.equals(password.toString(),passwordCont.toString())) {
            validForm = false;
            passwordCont.setError("Parolalar eşleşmiyor");
        } else if(TextUtils.isEmpty(userName.toString())||TextUtils.isEmpty(email.toString())||TextUtils.isEmpty(phoneNum.toString())
                || TextUtils.isEmpty(titel.toString())|| TextUtils.isEmpty(company.toString())||TextUtils.isEmpty(password.toString())){
            validForm = false;
            Toast.makeText(this, "Gerekli tüm alanlar doldurulmalı.", Toast.LENGTH_LONG).show();
        }else{ validForm = true;}
        return validForm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpButton:
                if (!validateForm()) {
                    return;
                }
                signUp();
                //TODO goToProfile();
                break;
            case R.id.signUpBackArrow:
                Intent goToLoginPage = new Intent(SignUpActivity.this, LoginActivity.class);
                finish();
                startActivity(goToLoginPage);
                break;
        }
    }


    private void signUp() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            user = mAuth.getCurrentUser();
                            Map<String, Object> userProfileData = new HashMap<>();
                            userProfileData.put("email", email.toString());
                            userProfileData.put("company", company.toString());
                            userProfileData.put("name", userName.toString());
                            userProfileData.put("phoneNumber", phoneNum.toString());
                            userProfileData.put("titel", titel.toString());
                            db.collection("users").document(user.getUid()).set(userProfileData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                            updateUI(user);
                        } else {
                            try {
                                throw task.getException();
                                //Invalid E-mail
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                email.setError("Geçerli bir mail adresi girin");
                                email.requestFocus();
                                //Email is already in use
                            } catch(FirebaseAuthUserCollisionException e) {
                                email.setError("Bu mail zaten kullanımda");
                                email.requestFocus();
                                //Other exceptions are handled
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Kayıt başarısız !",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

}
