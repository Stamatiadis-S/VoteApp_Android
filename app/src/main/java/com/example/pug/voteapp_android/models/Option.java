package com.example.pug.voteapp_android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Option implements Parcelable {

    private String id;
    private String option;
    private Integer votes;

    public Option() {
    }

    public Option(String option) {
        super();
        this.option = option;
    }

    public Option(String id, String option, Integer votes) {
        super();
        this.id = id;
        this.option = option;
        this.votes = votes;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getOption() {
        return option;
    }
    public void setOption(String option) {
        this.option = option;
    }

    public Integer getVotes() {
        return votes;
    }
    public void setVotes(Integer votes) {
        this.votes = votes;
    }
    public void incrementVotes() {
        this.votes++;
    }

    //Parcelable implementation
    protected Option(Parcel in) {
        id = in.readString();
        option = in.readString();
        votes = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(option);
        dest.writeInt(votes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Option> CREATOR = new Creator<Option>() {
        @Override
        public Option createFromParcel(Parcel in) {
            return new Option(in);
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };
}
