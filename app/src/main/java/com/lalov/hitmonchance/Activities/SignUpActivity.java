package com.lalov.hitmonchance.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.R;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private final static String TAG = "TAG123";

    RadioGroup radioGroupStarter;
    RadioButton radioButtonBulbasaur, radioButtonCharmander, radioButtonSquirtle;
    Button btnSignUp;
    EditText txtUsername;
    ImageView imgBulbasaur, imgCharmander, imgSquirtle;

    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        radioGroupStarter = findViewById(R.id.radioGroupStarter);
        radioButtonBulbasaur = findViewById(R.id.radioBtnBulbasaur);
        radioButtonCharmander = findViewById(R.id.radioBtnCharmander);
        radioButtonSquirtle = findViewById(R.id.radioBtnSquirtle);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtUsername = findViewById(R.id.txtUsername);
        imgBulbasaur = findViewById(R.id.imgBulbasaur);
        imgCharmander = findViewById(R.id.imgCharmander);
        imgSquirtle = findViewById(R.id.imgSquirtle);

        imgBulbasaur.setImageResource(R.drawable.bulbasaur);
        imgCharmander.setImageResource(R.drawable.charmander);
        imgSquirtle.setImageResource(R.drawable.squirtle);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
    }

    private void SignUp() {
        if (TextUtils.isEmpty(txtUsername.getText())) {
            txtUsername.setError("Pls enter a username"); //TODO Externalize
            return;
        }
        if (radioGroupStarter.getCheckedRadioButtonId() == -1) {
            radioButtonSquirtle.setError("Pls choose a starter pokemon"); //TODO Externalize
            return;
        }

        Map<String, Object> pokemon = new HashMap<>();
        pokemon.put("#", 1);
        pokemon.put("Name", "Charmander"); //TODO Change pokemon to selected

        Map<String, Object> user = new HashMap<>();
        user.put("Username", txtUsername.getText().toString());
        user.put("uid", currentUser.getUid());
        //user.put("Pokemon", pokemon);

        Log.d(TAG, currentUser.getUid());

        db.collection("Users").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User was added! Success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "User was not added! Error!");
                    }
                });


        db.collection("Users").document(currentUser.getUid()).collection("Pokemon").document()
                .set(pokemon)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Pokemon was added! Success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Pokemon was not added! Error!");
                    }
                });


        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    //TODO Create method that gets the selected pokemon from the api

}
