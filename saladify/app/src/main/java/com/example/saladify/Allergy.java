package com.example.saladify;


import android.os.Parcel;
import android.os.Parcelable;

public class Allergy implements Parcelable {
    private String gen;

    public Allergy(String name){
        this.gen = name;
    }
    protected Allergy(Parcel in) {
        gen = in.readString();
    }

    public static final Creator<Allergy> CREATOR = new Creator<Allergy>() {
        @Override
        public Allergy createFromParcel(Parcel in) {
            return new Allergy(in);
        }

        @Override
        public Allergy[] newArray(int size) {
            return new Allergy[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gen);
    }

    public String getName(){
        return gen;
    }
}
