package com.example.profileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class myInfoActivity extends AppCompatActivity {
    private ArrayList<ExampleItem> mExampleList;

    SharedPreferences sharedPreferences;
    String loggedUserId;

    private RecyclerView mRecyclerView;
    private ExampleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button buttonInsert;
    private Button buttonRemove;
    private EditText editTextInsert;
    private EditText editTextRemove;

    String facebookLink;
    String snapchatName;
    String instagramName;
    String linkedinLink;
    String discordName;
    String twitterName;
    DatabaseReference userDataBaseRef;

    String clickedOn;
    String currItem;

    ArrayList<String> allSocialMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.profileapp", Context.MODE_PRIVATE);
        loggedUserId = (String) sharedPreferences.getString("loggedUserId",null);
        userDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(loggedUserId);

        getUserData();

        // Need to set a delay for these because the function getUserData()
        // Takes some time to retrieve the data
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                createExampleList();
                buildRecyclerView();
            }
        }, 200);
    }


    public void getUserData(){
        // Get Social media Account links / names
        userDataBaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object facebookData = dataSnapshot.child("socialmedia").child("Facebook").getValue();
                Object snapchatData = dataSnapshot.child("socialmedia").child("Snapchat").getValue();
                Object instagramData = dataSnapshot.child("socialmedia").child("Instagram").getValue();
                Object linkedinData = dataSnapshot.child("socialmedia").child("LinkedIn").getValue();
                Object discordData = dataSnapshot.child("socialmedia").child("Discord").getValue();
                Object twitterData = dataSnapshot.child("socialmedia").child("Twitter").getValue();

                if (facebookData != null){
                    facebookLink = facebookData.toString();
                }

                if (snapchatData != null){
                    snapchatName = snapchatData.toString();
                }

                if (instagramData != null){
                    instagramName = instagramData.toString();
                }

                if (linkedinData != null){
                    linkedinLink = linkedinData.toString();
                }

                if (discordData != null){
                    discordName = discordData.toString();
                }

                if (twitterData != null){
                    twitterName = twitterData.toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void insertItem(int position){
        mExampleList.add(position, new ExampleItem (R.drawable.facebook, "Text 1", "Text 2"));
        mAdapter.notifyItemInserted(position); //Adds animation when add
    }

    public void removeItem(int position){
        mExampleList.remove(position);
        mAdapter.notifyItemRemoved(position); //Adds animation when remove
    }

    public void changeItem(int position, String text){
        mExampleList.get(position).setmText2(text);
        mAdapter.notifyItemChanged(position);
    }

    public void createExampleList(){

        mExampleList = new ArrayList<>();

        if (facebookLink == null){
            facebookLink = "Add Facebook link";
        }

        if (snapchatName == null){
            snapchatName = "Add Snapchat Name";
        }

        if (instagramName == null){
            instagramName = "Add Instagram Name";
        }

        if (linkedinLink == null){
            linkedinLink = "Add LinkedIn link";
        }

        if (discordName == null){
            discordName = "Add Discord Name";
        }

        if (twitterName == null){
            twitterName = "Add Twitter Name";
        }

        mExampleList.add(new ExampleItem(R.drawable.facebook, "Facebook", facebookLink));
        mExampleList.add(new ExampleItem(R.drawable.snapchat, "Snapchat", snapchatName));
        mExampleList.add(new ExampleItem(R.drawable.instagram, "Instagram", instagramName));
        mExampleList.add(new ExampleItem(R.drawable.linkedin, "LinkedIn", linkedinLink));
        mExampleList.add(new ExampleItem(R.drawable.discord, "Discord", discordName));
        mExampleList.add(new ExampleItem(R.drawable.twitter, "Twitter", twitterName));
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
                alertItemClicked(position);
            }


        });
    }

    public void alertItemClicked(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(myInfoActivity.this);
        if (position == 0){
            builder.setTitle("Facebook Profile Link");
            clickedOn = "Facebook";
            currItem = facebookLink;
        }else if(position == 1){
            builder.setTitle("Snapchat name");
            clickedOn = "Snapchat";
            currItem = snapchatName;
        }else if (position == 2){
            builder.setTitle("Edit Instagram name");
            clickedOn = "Instagram";
            currItem = instagramName;
        }else if (position == 3){
            builder.setTitle("LinkedIn Profile Link");
            clickedOn = "LinkedIn";
            currItem = linkedinLink;
        }else if (position == 4){
            builder.setTitle("Discord name");
            clickedOn = "Discord";
            currItem = discordName;
        }else if (position == 5){
            builder.setTitle("Twitter name");
            clickedOn = "Twitter";
            currItem = twitterName;
        }

        // Set up the input
        final EditText input = new EditText(getApplicationContext());
        // Specify the type of input expected (This means regular text)
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        input.setText(currItem);
        input.selectAll();

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (position == 0){
                    facebookLink = input.getText().toString();
                }else if(position == 1){
                    snapchatName = input.getText().toString();
                }else if (position == 2){
                    instagramName = input.getText().toString();
                }else if (position == 3){
                    linkedinLink = input.getText().toString();
                }else if (position == 4){
                    discordName = input.getText().toString();
                }else if (position == 5){
                    twitterName = input.getText().toString();
                }

                userDataBaseRef.child("socialmedia").child(clickedOn).setValue(input.getText().toString());
                changeItem(position,checkLength(input.getText().toString()));

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
}
