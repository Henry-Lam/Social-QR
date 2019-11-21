package com.example.profileapp;

public class FriendProfileItem {

    private int mMediaImage;
    private String mText1;
    private String mText2;
    private String mfriendId;

    public FriendProfileItem(int mediaImage, String text1, String text2, String friendId){
        mMediaImage = mediaImage;
        mText1 = text1;
        mText2 = text2;
        mfriendId = friendId;
    }

    public int getmMediaImage() {
        return mMediaImage;
    }

    public void setmMediaImage(int mMediaImage) {
        this.mMediaImage = mMediaImage;
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
