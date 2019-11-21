package com.example.profileapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

public class FriendItem {

    private Uri mImageUri;
    private String mText1;
    private String mText2;
    private String mfriendId;
    private Context mContext;

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public FriendItem(Uri imageUri, String text1, String text2, String friendId, Context Context){
        mImageUri = imageUri;
        mText1 = text1;
        mText2 = text2;
        mfriendId = friendId;
        mContext = Context;
    }

    public Uri getmImageUri() {
        return mImageUri;
    }

    public void setmImageUri(Uri mImageUri) {
        this.mImageUri = mImageUri;
    }

    public String getmfriendId() {
        return mfriendId;
    }

    public void setmfriendId(String mfriendId) {
        this.mfriendId = mfriendId;
    }

    public String getmText1() {
        return mText1;
    }

    public void setmText1(String mText1) {
        this.mText1 = mText1;
    }

    public String getmText2() {
        return mText2;
    }

    public void setmText2(String mText2) {
        this.mText2 = mText2;
    }


}
