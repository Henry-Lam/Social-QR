package com.example.profileapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    EditText textEmail, textPassword;
    Button btnLogin, btnSignUp;
    TextView passwordMsg;
    private FirebaseAuth mAuth;

    String emailInput;
    String passwordInput;

    DatabaseReference myDataBase;
    SharedPreferences sharedPreferences;

    public void register(){
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();

        if (password.isEmpty()){
            textPassword.requestFocus();
            passwordMsg.setText("Please enter a password");
            return;
        }else if (password.length() < 6){
            textPassword.requestFocus();
            passwordMsg.setText("Password must contain more than 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Create a Realtime storage node for this new user
                            String uid = user.getUid();
                            User userObj = new User();
                            myDataBase.child(uid).setValue(userObj);

                            updateUI(user);
                        } else {
                            // Sign up failed
                        }
                    }
                });
    }

    public void login (String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // Login failed
                            textEmail.requestFocus();
                            textPassword.requestFocus();
                            passwordMsg.setText("Incorrect Email or Password");
                        }
                    }
                });
    }

    public void updateUI(FirebaseUser user){
        Intent goUserHome = new Intent(getApplicationContext(), UserHome.class);
        sharedPreferences.edit().putString("loggedUserId", user.getUid()).apply();
        startActivity(goUserHome);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textEmail = (EditText) findViewById(R.id.textEmail);
        textPassword = (EditText) findViewById(R.id.textPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        passwordMsg = (TextView) findViewById(R.id.passwordMsg);
        mAuth = FirebaseAuth.getInstance();
        myDataBase = FirebaseDatabase.getInstance().getReference("Users");
        sharedPreferences = this.getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);

        // Check if a User is already logged in
        // Probably use Shared preference to keep someone logged in
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null){
//            updateUI(currentUser);
//        }


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailInput = textEmail.getText().toString();
                passwordInput = textPassword.getText().toString();

                login(emailInput,passwordInput);
            }
        });

    }
}
