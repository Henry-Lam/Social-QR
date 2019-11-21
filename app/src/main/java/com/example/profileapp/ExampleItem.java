package com.example.profileapp;

public class ExampleItem {

    private int mImageResource;
    // When get image from Drawable folder, its saved as int

    private String mText1;
    private String mText2;

    public ExampleItem(int ImageResource, String text1, String text2){
        mImageResource = ImageResource;
        mText1 = text1;
        mText2 = text2;
    }

    public void changeText1(String text){
        mText1 = text;
    }

    public int getmImageResource() {
        return mImageResource;
    }

    public void setmImageResource(int mImageResource) {
        this.mImageResource = mImageResource;
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
