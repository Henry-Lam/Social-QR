package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

public class QRScannerActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    int MY_CAMERA_REQUEST_CODE;
    Button btnBack;
    DatabaseReference usersDataBaseRef;
    DatabaseReference otherDataBaseRef;
    //SparseArray<Barcode> codes;
    SharedPreferences sharedPreferences;
    String loggedUserId;
    String friendUserId;
    TextView textView;
    String myFriends;
    String detectedUser;
    ArrayList<String> sad ;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        textView = (TextView) findViewById(R.id.titleTextView);

        btnBack = (Button) findViewById(R.id.btnBack);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();

        usersDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640,480).build();

        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);
        loggedUserId = (String) sharedPreferences.getString("loggedUserId",null);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                MY_CAMERA_REQUEST_CODE = 100;
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},MY_CAMERA_REQUEST_CODE);
                }

                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> codes = detections.getDetectedItems();
                if (codes.size() != 0){
                    textView.post(new Runnable(){
                        @Override
                        public void run() {
                            usersDataBaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    friendUserId = codes.valueAt(0).displayValue;
                                    myFriends = (String) dataSnapshot.child(loggedUserId).child("friends").getValue();

                                    // Add Friend to User's friend list
                                    if (myFriends == null){
                                        usersDataBaseRef.child(loggedUserId).child("friends").setValue(friendUserId);
                                    }else{
                                        if (!myFriends.contains(friendUserId)){
                                            String commaFriendId = "," + friendUserId;
                                            usersDataBaseRef.child(loggedUserId).child("friends").setValue(myFriends + commaFriendId);
                                        }
                                    }

                                    // Add User to Friend's friend list
                                    if (dataSnapshot.child(friendUserId).child("friends").getValue() == null){
                                        usersDataBaseRef.child(friendUserId).child("friends").setValue(loggedUserId);
                                    }else{
                                        String f_allFriends = dataSnapshot.child(friendUserId).child("friends").getValue().toString();
                                        if (!f_allFriends.contains(loggedUserId)){
                                            String commaMyUserId = "," + loggedUserId;
                                            usersDataBaseRef.child(friendUserId).child("friends").setValue(f_allFriends + commaMyUserId);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            textView.setText("User Detected");
                        }
                    });


                }
            }
        });
    }
}
