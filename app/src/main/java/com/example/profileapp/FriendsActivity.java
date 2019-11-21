package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {
    private ArrayList<FriendItem> mExampleList;

    SharedPreferences sharedPreferences;
    String loggedUserId;

    private RecyclerView mRecyclerView;
    private FriendAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button buttonInsert;
    private Button buttonRemove;
    private EditText editTextInsert;
    private EditText editTextRemove;

    String friendId;
    String friendName;
    String friendProfilePicUri;


    private StorageReference userStorageRef;
    private DatabaseReference userDataBaseRef;
    private StorageReference friendStorageRef;

    File localFile;

    Bitmap bMap;

    ArrayList<Bitmap> allFriendBitmap;
    ArrayList<String> allFriendName;
    ArrayList<String> allFriendId;

    ArrayList<Uri> allFriendUri;
    Context FriendsActivityContext;
    String myFriendsString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);
        loggedUserId = (String) sharedPreferences.getString("loggedUserId",null);
        userStorageRef = FirebaseStorage.getInstance().getReference();
        userDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allFriendBitmap = new ArrayList<>();
        allFriendName = new ArrayList<>();
        allFriendId = new ArrayList<>();
        allFriendUri = new ArrayList<>();
        mExampleList = new ArrayList<>();



        combined();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms

                createExampleList();
                buildRecyclerView();
            }
        }, 500);

    }

    public void removeItem(int position){
        mExampleList.remove(position);
        mAdapter.notifyItemRemoved(position); //Adds animation when remove

        allFriendId.remove(position);
        String stringAllFriendId = allFriendId.get(0);
        for (int i=1; i < allFriendId.size(); i++){
            stringAllFriendId += "," + allFriendId.get(i);
        }
        // do this so if only 1 item won't have "item1," will have "item1"

        userDataBaseRef.child(loggedUserId).child("friends").setValue(stringAllFriendId);
    }

    public void goFriendProfile(int position){
        friendId = mExampleList.get(position).getmfriendId();
        Intent goFriendPro = new Intent(getApplicationContext(), FriendProfileActivity.class);
        goFriendPro.putExtra("friendId", friendId);
        startActivity(goFriendPro);
    }

    public void createExampleList(){
        for (int i=0;i < allFriendUri.size();i++){
            Uri currUri = allFriendUri.get(i);
            String currName = allFriendName.get(i);
            String currId = allFriendId.get(i);

            mExampleList.add(new FriendItem(currUri, currName,"",currId, getApplicationContext()));
        }
    }


    public void combined(){
        userDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFriendsString = (String) dataSnapshot.child(loggedUserId).child("friends").getValue();
                if (myFriendsString != null){
                    String myFriendsLst[] = myFriendsString.split(",");
                    for (int i = 0; i < myFriendsLst.length; i++){ ;
                        friendId = myFriendsLst[i];
                        allFriendId.add(friendId);

                        friendName = (String) dataSnapshot.child(friendId).child("name").getValue();
                        if (friendName == null){
                            friendName = "Name not available";
                        }
                        allFriendName.add(friendName);

                        friendProfilePicUri = (String) dataSnapshot.child(friendId).child("profilePicUri").getValue();
                        allFriendUri.add(Uri.parse(friendProfilePicUri));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getAllFriendInfo(){
        mExampleList = new ArrayList<>();

        userDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String myFriendsString = (String) dataSnapshot.child(loggedUserId).child("friends").getValue();
                if (myFriendsString != null){
                    String myFriendsLst[] = myFriendsString.split(",");
//                    myFriendsLst = (ArrayList<String>) Arrays.asList(temp);

                    for (int i = 0; i < myFriendsLst.length; i++){
                        friendId = myFriendsLst[i];
                        allFriendId.add(friendId);

                        friendName = (String) dataSnapshot.child(friendId).child("name").getValue();
                        if (friendName == null){
                            friendName = "Name not available";
                        }
                        allFriendName.add(friendName);

                        friendStorageRef = userStorageRef.child(friendId).child("profilePic");

                        try {
                            localFile = File.createTempFile("tempFriendPic"+i, "jpg");
                            localFile.deleteOnExit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (friendStorageRef != null){
                            friendStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    if (localFile != null){
                                        bMap = BitmapFactory.decodeFile(String.valueOf(localFile));
                                    }else{
                                        bMap = BitmapFactory.decodeResource(getResources(), R.drawable.noprofilepic);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    bMap = BitmapFactory.decodeResource(getResources(), R.drawable.noprofilepic);
                                }
                            });
                        }else{
                            bMap = BitmapFactory.decodeResource(getResources(), R.drawable.noprofilepic);
                        }
                        allFriendBitmap.add(bMap);
                    }//
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new FriendAdapter(mExampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new FriendAdapter.OnItemClickListener_1() {
            @Override
            public void onItemClick(int position) {
                goFriendProfile(position);
            }

            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }
        });
    }
}
