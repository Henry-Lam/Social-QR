package com.example.profileapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendProfileActivity extends AppCompatActivity {
    private ArrayList<ExampleItem> mExampleList;
    private RecyclerView mRecyclerView;
    private ExampleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    DatabaseReference friendSocialMediaRef;
    DatabaseReference friendDatabaseRef;
    String friendId;
    Map<String,Integer> mediaToPic;
    ArrayList<String> socialMediaInfoLst;
    CircleImageView friendProfilePic;
    TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        friendProfilePic = (CircleImageView) findViewById(R.id.friendProfilePic);
        titleTextView = (TextView) findViewById(R.id.titleTextView);

        Intent intent = getIntent();
        friendId = intent.getStringExtra("friendId");

        friendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(friendId);
        friendSocialMediaRef = friendDatabaseRef.child("socialmedia");

        socialMediaInfoLst = new ArrayList<String>();

        mediaToPic = new HashMap<String,Integer>();
        mediaToPic.put("Facebook", R.drawable.facebook);
        mediaToPic.put("Snapchat", R.drawable.snapchat);
        mediaToPic.put("Instagram", R.drawable.instagram);
        mediaToPic.put("LinkedIn", R.drawable.linkedin);
        mediaToPic.put("Discord", R.drawable.discord);
        mediaToPic.put("Twitter", R.drawable.twitter);


        setupTitle();
        createExampleList();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms

                buildRecyclerView();
            }
        }, 200);
    }

    public String checkLength(String s){
        if (s.length() > 32){
            //substring(start index, end index)
            String s2 = s.substring(0,32);
            s2 += "-\n";

            // if end index not included, goes to end
            s2 += s.substring(32);
            return s2;
        }else{
            return s;
        }
    }

    public void setupTitle(){

        friendDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                if (name == null){
                    name = "Name not available";
                }

                titleTextView.setText(name);

                String profilePicUriString = (String) dataSnapshot.child("profilePicUri").getValue();

                if (profilePicUriString != null){
                    Uri profilePicUri = Uri.parse(profilePicUriString);
                    friendProfilePic.setImageURI(profilePicUri);

                    Glide.with(getApplicationContext())
                            .load(profilePicUri)
                            .into(friendProfilePic);
                }



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });





    }

    public void createExampleList(){

        mExampleList = new ArrayList<>();

        friendSocialMediaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String,String> friendSocialMediaMap = (Map<String,String>) dataSnapshot.getValue();
                if (friendSocialMediaMap == null){
                    //Got no social media listed
                    mExampleList.add(new ExampleItem(R.drawable.sadface, "No user profiles available", ""));
                    socialMediaInfoLst.add("No user profiles available");
                }else{
                    for (Map.Entry<String, String> sMediaEntry : friendSocialMediaMap.entrySet()){
                        String mediaName = sMediaEntry.getKey();
                        System.out.println(mediaName + "MARKER 2");
                        int mediaDrawablePic = mediaToPic.get(mediaName);
                        String mediaInfo = sMediaEntry.getValue();
                        socialMediaInfoLst.add(mediaInfo);

                        mExampleList.add(new ExampleItem(mediaDrawablePic, mediaName, mediaInfo));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ExampleAdapter(mExampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener_1() {
            @Override
            public void onItemClick(int position) {
                ItemClicked(position);
            }
        });
    }

    public void ItemClicked(final int position){
        // Copy name / link to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Label", socialMediaInfoLst.get(position));
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getApplicationContext(), socialMediaInfoLst.get(position) +" copied", Toast.LENGTH_SHORT).show();
    }
}
