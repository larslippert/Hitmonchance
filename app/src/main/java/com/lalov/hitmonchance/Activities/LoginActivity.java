package com.lalov.hitmonchance.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lalov.hitmonchance.R;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    EditText txtEmail, txtPassword;
    TextView txtLogin;
    Button btnSignUp, btnSignIn;

    private FirebaseAuth auth;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.editTextEmail);
        txtPassword = findViewById(R.id.editTextPassword);
        txtLogin = findViewById(R.id.txtLogin);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        auth = FirebaseAuth.getInstance();

        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
    }

    private void SignUp() {
        String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this,getResources().getString(R.string.email),Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,getResources().getString(R.string.password), Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (password.length() < 6) {
                                txtPassword.setError(getResources().getString(R.string.kortkodeord));
                            }
                            else {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.signupfailed), Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void Login() {
        String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this,getResources().getString(R.string.email),Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,getResources().getString(R.string.password), Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            txtPassword.setError(getResources().getString(R.string.emailpasswordnotgood));
                        }
                        else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
