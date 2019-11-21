package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MyQRCodeActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String loggedUserId;

    ImageView myQRCodeImage;
    private StorageReference myQRCodeStorageRef;
    File localFile;

    Button btnBack;




    public void getQRCode(){
        try {
            localFile = File.createTempFile("tempProfilePic", "jpg");
            localFile.deleteOnExit();
            // delete on exit - deletes file when "App terminates"
        } catch (IOException e) {
            e.printStackTrace();
        }

        myQRCodeStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                if (localFile != null){
                    Bitmap bMap = BitmapFactory.decodeFile(String.valueOf(localFile));
                    myQRCodeImage.setImageBitmap(bMap);
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
                    myQRCodeImage.setImageBitmap(bitmap);

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qrcode);

        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);
        loggedUserId = (String) sharedPreferences.getString("loggedUserId",null);
        myQRCodeImage = (ImageView) findViewById(R.id.myQRCodeImage);
        myQRCodeStorageRef = FirebaseStorage.getInstance().getReference().child(loggedUserId).child("myQRCode");
        btnBack = (Button) findViewById(R.id.btnBack);


        getQRCode();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });





    }
}
