package com.cmnd97.booklistingapp;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

class Entry implements Parcelable {


    private String entryTitle;
    private String entryAuthors;
    private Integer entryNumber;
    private String entryUrl;
    private Bitmap entryThumbnail;
    private String price;

    Entry(String entryTitle, String entryAuthors, String entryUrl, Bitmap entryThumbnail, String price, Integer entryNumber) {
        this.entryTitle = entryTitle;
        this.entryAuthors = entryAuthors;
        this.entryUrl = entryUrl;
        this.entryThumbnail = entryThumbnail;
        this.entryNumber = entryNumber;
        this.price = price;
    }

    private Entry(Parcel in) {
        entryTitle = in.readString();
        entryAuthors = in.readString();
        entryUrl = in.readString();
        entryThumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        price = in.readString();
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    String getEntryTitle() {
        return entryTitle;
    }

    String getEntryAuthors() {
        return entryAuthors;
    }

    String getEntryNumber() {
        return entryNumber.toString();
    }

    String getEntryUrl() {
        return entryUrl;
    }

    Bitmap getEntryThumbnail() {
        return entryThumbnail;
    }

    String getPrice() {
        return price;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entryTitle);
        dest.writeString(entryAuthors);
        dest.writeString(entryUrl);
        dest.writeParcelable(entryThumbnail, 0);
        dest.writeString(price);
        dest.writeInt(entryNumber);

    }

}
