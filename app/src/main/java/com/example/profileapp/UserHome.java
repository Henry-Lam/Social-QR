package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserHome extends AppCompatActivity {

    DatabaseReference userDataBaseRef;
    private String nameInput = "";
    TextView profileName;
    String loggedUserId;
    SharedPreferences sharedPreferences;
    UploadTask uploadTask;
    File localFile;
    File localFile_qr;
    private StorageReference userStorageRef;
    private StorageReference profilePicStorageRef;
    CircleImageView profile_image;

    ImageView btnQRThumbnail;
    private Animator currentAnimator;
    private int shortAnimationDuration;

    Button btnQRCamera;
    Button btnMyInfo;
    Button btnFriends;

    private StorageReference myQRCodeStorageRef;




    public void alertCreateName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected (This means regular text)
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nameInput = input.getText().toString();
                userDataBaseRef.child("name").setValue(nameInput);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void changeProfilePic(){
        //See if have Permission to access phone's photos
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // don't have permission
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{
            getPhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    public void getPhoto (){
        // Used image cropping Library
        // Cropping library / steps https://github.com/ArthurHub/Android-Image-Cropper
        // the .set____ attributes can be found on
        // https://github.com/ArthurHub/Android-Image-Cropper/wiki/Visual-Customization
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setBorderLineColor(Color.RED)
                .setGuidelinesColor(Color.GREEN)
                .setFixAspectRatio(true)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //Took the cropped imageURI and set it to circle image view
                profile_image.setImageURI(resultUri);

                //Storing image to Firebase
                profilePicStorageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            storeProfilePicUri();

                        }else{
                            String message = task.getException().toString();
                            System.out.println(message);
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void getQRCode(){
        try {
            localFile_qr = File.createTempFile("tempProfilePic_qr", "jpg");
            localFile_qr.deleteOnExit();
            // delete on exit - deletes file when "App terminates"
        } catch (IOException e) {
            e.printStackTrace();
        }

        myQRCodeStorageRef.getFile(localFile_qr).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                if (localFile_qr != null){
                    Bitmap bMap = BitmapFactory.decodeFile(String.valueOf(localFile_qr));
                    btnQRThumbnail.setImageBitmap(bMap);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Generate a QR code and put into storage
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try{
                    BitMatrix bitMatrix = multiFormatWriter.encode(loggedUserId, BarcodeFormat.QR_CODE, 200, 200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    btnQRThumbnail.setImageBitmap(bitmap);

                    //Store QR Code in FireBase

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = myQRCodeStorageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                getQRCode();
            }
        });
    }

    public void storeDefaultProfilePic(){
        Uri uri = Uri.parse("android.resource://com.example.profileapp/drawable/noprofilepic");

        profilePicStorageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        storeProfilePicUri();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
    }

    public void storeProfilePicUri(){
        profilePicStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                userDataBaseRef.child("profilePicUri").setValue(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        ///////////PROFILE PIC (on Create) ///////////

        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);
        loggedUserId = (String) sharedPreferences.getString("loggedUserId",null);
        profile_image = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profileName);
        userStorageRef = FirebaseStorage.getInstance().getReference().child(loggedUserId);
        profilePicStorageRef = userStorageRef.child("profilePic");
        userDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(loggedUserId);
        btnQRThumbnail = (ImageView) findViewById(R.id.btnQRThumbnail);
        myQRCodeStorageRef = FirebaseStorage.getInstance().getReference().child(loggedUserId).child("myQRCode");

        userDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").getValue() == null){
                    alertCreateName();
                }else{
                    profileName.setText(dataSnapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertCreateName();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfilePic();
            }
        });

        try {
            localFile = File.createTempFile("tempProfilePic", "jpg");
            localFile.deleteOnExit();
            // delete on exit - deletes file when "App terminates"
        } catch (IOException e) {
            e.printStackTrace();
        }
        profilePicStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                if (localFile != null){
                    Bitmap bMap = BitmapFactory.decodeFile(String.valueOf(localFile));
                    profile_image.setImageBitmap(bMap);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                storeDefaultProfilePic();
            }
        });


        ////////////////QR Thumbnail (on Create) ///////////////////////////////


        btnQRThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goMyQRCode = new Intent(getApplicationContext(), MyQRCodeActivity.class);
                startActivity(goMyQRCode);
            }
        });

        /////////////////QR Camera (on Create) ///////////////////////////////////
        btnQRCamera = (Button) findViewById(R.id.btnQRCamera);
        btnQRCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goQRCamera = new Intent(getApplicationContext(), QRScannerActivity.class);
                startActivity(goQRCamera);
            }
        });

        ////////////// My Info (on Create) //////////////////////////////////////
        btnMyInfo = findViewById(R.id.btnMyInfo);
        btnMyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goMyInfo = new Intent(getApplicationContext(), myInfoActivity.class);
                startActivity(goMyInfo);
            }
        });

        ///////// My QR Collection (on Create) ////////////////////////////////////
        btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goFriends = new Intent(getApplicationContext(), FriendsActivity.class);
                startActivity(goFriends);

            }
        });






        getQRCode();




    }
}
