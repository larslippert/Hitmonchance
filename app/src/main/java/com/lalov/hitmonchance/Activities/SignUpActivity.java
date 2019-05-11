package com.lalov.hitmonchance.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lalov.hitmonchance.R;

import static com.lalov.hitmonchance.Globals.REQUEST_CODE_SIGNUP;

public class SignUpActivity extends AppCompatActivity {

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

    /** ########################################################################################
     *  ######### Private methods ##############################################################
     *  ######################################################################################## */

    private void SignUp() {
        if (TextUtils.isEmpty(txtUsername.getText())) {
            txtUsername.setError("Pls enter a username"); //TODO Externalize
            return;
        }
        if (radioGroupStarter.getCheckedRadioButtonId() == -1) {
            radioButtonSquirtle.setError("Pls choose a starter pokemon"); //TODO Externalize
            return;
        }

        String pokemonName;
        if (radioButtonBulbasaur.isChecked())
            pokemonName = "bulbasaur";
        else if (radioButtonCharmander.isChecked())
            pokemonName = "charmander";
        else if (radioButtonSquirtle.isChecked())
            pokemonName = "squirtle";
        else
            pokemonName = "";

        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.putExtra("Username", txtUsername.getText().toString());
        intent.putExtra("PokemonName", pokemonName);

        startActivityForResult(intent, REQUEST_CODE_SIGNUP);
        finish();
    }
}
